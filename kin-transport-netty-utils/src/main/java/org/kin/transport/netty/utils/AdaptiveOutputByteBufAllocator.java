/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.kin.framework.utils.Maths;
import org.kin.framework.utils.SysUtils;

import java.util.ArrayList;
import java.util.List;

import static org.kin.transport.netty.utils.NettySysProperties.*;


/**
 * 支持预测下次分配{@link ByteBuf}时大小, 减少编解码, 序列化过程中, 扩容而带来的额外性能开销
 *
 * @author huangjianqin
 * @date 2021/11/29
 * @see io.netty.channel.AdaptiveRecvByteBufAllocator
 */
public class AdaptiveOutputByteBufAllocator {
    /** 默认最小分配字节数, 64B */
    private static final int DEFAULT_MINIMUM = 64;
    /** 默认初始分配字节数, 512B */
    private static final int DEFAULT_INITIAL = 512;
    /** 默认最大分配字节数, 512KB */
    private static final int DEFAULT_MAXIMUM = 524288;

    /** index增量 */
    private static final int INDEX_INCREMENT = SysUtils.getIntSysProperty(KIN_NETTY_ADAPTIVE_ALLOCATOR_INDEX_INCREMENT, 4);
    /** index减量 */
    private static final int INDEX_DECREMENT = SysUtils.getIntSysProperty(KIN_NETTY_ADAPTIVE_ALLOCATOR_INDEX_DECREMENT, 1);

    /** bytes size表 */
    private static final int[] SIZE_TABLE;

    static {
        List<Integer> sizeTable = new ArrayList<>();
        //默认4KB, 刚好是netty page的一半
        int threshold = Maths.round2Power2(SysUtils.getIntSysProperty(KIN_NETTY_ADAPTIVE_ALLOCATOR_THRESHOLD, 4096));

        //参考SizeClasses
        //power2基数, 即16
        int base = 3;
        //初始16bit
        int size = 2 << base;
        //初始16bit
        int baseSize = 2 << base;

        //第0组组内元素相差16
        for (int i = 0; i < 3; i++) {
            sizeTable.add(size);
            size += baseSize;
        }
        sizeTable.add(size);
        size += baseSize;

        //第1组组内元素相差16, 第二组32.....
        while (size < threshold) {
            for (int i = 0; i < 3; i++) {
                sizeTable.add(size);
                size += baseSize;
            }
            sizeTable.add(size);

            base++;
            baseSize = 2 << base;
            size += baseSize;
        }

        //超过threshold之后, double增长
        // lgtm [java/constant-comparison]
        for (int i = threshold << 1; i > 0; i <<= 1) {
            sizeTable.add(i);
        }

        SIZE_TABLE = new int[sizeTable.size()];
        for (int i = 0; i < SIZE_TABLE.length; i++) {
            SIZE_TABLE[i] = sizeTable.get(i);
        }
    }

    /**
     * 二分搜索查找大于指定{@code size}大小的最小index
     */
    private static int getSizeTableIndex(int size) {
        for (int low = 0, high = SIZE_TABLE.length - 1; ; ) {
            if (high < low) {
                return low;
            }
            if (high == low) {
                return high;
            }

            int mid = low + high >>> 1;
            int a = SIZE_TABLE[mid];
            int b = SIZE_TABLE[mid + 1];
            if (size > b) {
                low = mid + 1;
            } else if (size < a) {
                high = mid - 1;
            } else if (size == a) {
                return mid;
            } else {
                return mid + 1;
            }
        }
    }

    /** 单例 */
    public static final AdaptiveOutputByteBufAllocator DEFAULT = new AdaptiveOutputByteBufAllocator();

    /** 大于最小分配字节数的最小index */
    private final int minIndex;
    /** 大于最大分配字节数的最小index */
    private final int maxIndex;
    /** 初始分配字节数 */
    private final int initial;

    /**
     * Creates a new predictor with the default parameters.  With the default
     * parameters, the expected buffer size starts from {@code 512}, does not
     * go down below {@code 64}, and does not go up above {@code 524288}.
     */
    private AdaptiveOutputByteBufAllocator() {
        this(DEFAULT_MINIMUM, DEFAULT_INITIAL, DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new predictor with the specified parameters.
     *
     * @param minimum the inclusive lower bound of the expected buffer size
     * @param initial the initial buffer size when no feedback was received
     * @param maximum the inclusive upper bound of the expected buffer size
     */
    public AdaptiveOutputByteBufAllocator(int minimum, int initial, int maximum) {
        if (minimum <= 0) {
            throw new IllegalArgumentException("minimum: " + minimum);
        }
        if (initial < minimum) {
            throw new IllegalArgumentException("initial: " + initial);
        }
        if (maximum < initial) {
            throw new IllegalArgumentException("maximum: " + maximum);
        }

        int minIndex = getSizeTableIndex(minimum);
        if (SIZE_TABLE[minIndex] < minimum) {
            this.minIndex = minIndex + 1;
        } else {
            this.minIndex = minIndex;
        }

        int maxIndex = getSizeTableIndex(maximum);
        if (SIZE_TABLE[maxIndex] > maximum) {
            this.maxIndex = maxIndex - 1;
        } else {
            this.maxIndex = maxIndex;
        }

        this.initial = initial;
    }

    public Handle newHandle() {
        return new HandleImpl(minIndex, maxIndex, initial);
    }

    //-----------------
    public interface Handle {

        /**
         * Creates a new buffer whose capacity is probably large enough to write all outbound data and small
         * enough not to waste its space.
         */
        ByteBuf allocate(ByteBufAllocator alloc);

        /**
         * Similar to {@link #allocate(ByteBufAllocator)} except that it does not allocate anything but just tells the
         * capacity.
         */
        int guess();

        /**
         * Records the actual number of wrote bytes in the previous write operation so that the allocator allocates
         * the buffer with potentially more correct capacity.
         *
         * @param actualWroteBytes the actual number of wrote bytes in the previous allocate operation
         */
        void record(int actualWroteBytes);
    }

    private static final class HandleImpl implements Handle {
        /** 最小字节数index */
        private final int minIndex;
        /** 最大字节数index */
        private final int maxIndex;
        /**
         * 当前index, 线程本地读写
         */
        private int index;
        /**
         * 下次分配字节数, 线程本地读写, 其他线程只能读
         */
        private volatile int nextAllocateBufSize;
        /**
         * 本次是否需要马上减少字节数index, 线程本地读写
         */
        private boolean decreaseNow;

        HandleImpl(int minIndex, int maxIndex, int initial) {
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;

            //初始分配字节数index
            index = getSizeTableIndex(initial);
            nextAllocateBufSize = SIZE_TABLE[index];
        }

        @Override
        public ByteBuf allocate(ByteBufAllocator alloc) {
            //自适应分配heap or direct ByteBuf
            return alloc.buffer(guess());
        }

        @Override
        public int guess() {
            return nextAllocateBufSize;
        }

        @Override
        public void record(int actualWroteBytes) {
            if (actualWroteBytes <= SIZE_TABLE[Math.max(0, index - INDEX_DECREMENT)]) {
                if (decreaseNow) {
                    //马上减少index, 下次分配更少字节数
                    index = Math.max(index - INDEX_DECREMENT, minIndex);
                    nextAllocateBufSize = SIZE_TABLE[index];
                    decreaseNow = false;
                } else {
                    //不减少index, 给机会看看下次字节数是不是真的更少了, 如果是, 则减少, 否则不减少
                    decreaseNow = true;
                }
            } else if (actualWroteBytes >= nextAllocateBufSize) {
                //增加index, 下次分配更多字节数
                index = Math.min(index + INDEX_INCREMENT, maxIndex);
                nextAllocateBufSize = SIZE_TABLE[index];
                decreaseNow = false;
            }
        }
    }
}
