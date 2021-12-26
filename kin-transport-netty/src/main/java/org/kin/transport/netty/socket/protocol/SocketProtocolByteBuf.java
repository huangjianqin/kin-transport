package org.kin.transport.netty.socket.protocol;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCounted;
import org.kin.transport.netty.utils.VarIntUtils;

import java.nio.charset.StandardCharsets;

/**
 * 协议buffer内容封装
 *
 * @author huangjianqin
 * @date 2019/6/4
 */
public class SocketProtocolByteBuf implements SocketRequestOprs, SocketResponseOprs, ReferenceCounted {
    /** 读模式 */
    private static final int READ_MODE = 0;
    /** 写模式 */
    private static final int WRITE_MODE = 1;
    /** 读写模式 */
    private static final int READ_WRITE_MODE = 2;

    /** 字节buffer */
    private ByteBuf byteBuf;
    /** 协议id */
    private int protocolId;
    /** 协议成都 */
    private int contentSize;
    /** 模式 */
    private final int mode;

    public SocketProtocolByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
        this.protocolId = readVarInt32();
        this.contentSize = byteBuf.readableBytes();
        this.mode = READ_MODE;
    }

    public SocketProtocolByteBuf(int protocolId) {
        byteBuf = Unpooled.buffer();
        this.protocolId = protocolId;
        this.mode = WRITE_MODE;
        writeVarInt32(protocolId);
    }

    //--------------------------------------------request----------------------------------------------------

    @Override
    public int getContentSize() {
        Preconditions.checkArgument(mode == READ_MODE);
        return contentSize;
    }

    @Override
    public byte readByte() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readUnsignedByte();
    }

    @Override
    public boolean readBoolean() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readBoolean();
    }

    @Override
    public byte[] readBytes(int length) {
        Preconditions.checkArgument(mode == READ_MODE);
        Preconditions.checkArgument(length > 0);
        Preconditions.checkArgument(length <= byteBuf.readableBytes());
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }

    @Override
    public byte[] readBytes() {
        Preconditions.checkArgument(mode == READ_MODE);
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return result;
    }

    @Override
    public short readShort() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readShort();
    }

    @Override
    public int readUnsignedShort() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readUnsignedShort();
    }

    @Override
    public int readInt() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readInt();
    }

    @Override
    public long readUnsignedInt() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readUnsignedInt();
    }

    @Override
    public int readVarInt32() {
        Preconditions.checkArgument(mode == READ_MODE);
        return VarIntUtils.readRawVarInt32(byteBuf);
    }

    @Override
    public float readFloat() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readFloat();
    }

    @Override
    public long readLong() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readLong();
    }

    @Override
    public long readVarLong64() {
        Preconditions.checkArgument(mode == READ_MODE);
        return VarIntUtils.readRawVarInt64(byteBuf);
    }

    @Override
    public double readDouble() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readDouble();
    }

    @Override
    public String readString() {
        Preconditions.checkArgument(mode == READ_MODE);
        int length = byteBuf.readShort();
        byte[] content = new byte[length];
        byteBuf.readBytes(content);
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public String readBigString() {
        Preconditions.checkArgument(mode == READ_MODE);
        int length = byteBuf.readUnsignedShort();
        byte[] content = new byte[length];
        byteBuf.readBytes(content);
        return new String(content, StandardCharsets.UTF_8);
    }


    @Override
    public int refCnt() {
//        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
//        Preconditions.checkArgument(mode == READ_MODE);
        byteBuf.retain();
        return this;
    }

    @Override
    public ReferenceCounted retain(int i) {
        byteBuf.retain(i);
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        byteBuf.touch();
        return this;
    }

    @Override
    public ReferenceCounted touch(Object o) {
        byteBuf.touch(o);
        return this;
    }

    @Override
    public boolean release() {
        return byteBuf.release();
    }

    @Override
    public boolean release(int i) {
        return byteBuf.release(i);
    }

    //--------------------------------------------response----------------------------------------------------

    @Override
    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    @Override
    public int getProtocolId() {
        return protocolId;
    }

    @Override
    public int getSize() {
        Preconditions.checkArgument(mode == WRITE_MODE);
        return byteBuf.readableBytes();
    }

    @Override
    public SocketResponseOprs writeByte(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE, "value: %s", value);
        byteBuf.writeByte(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeUnsignedByte(short value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Byte.MAX_VALUE - Byte.MIN_VALUE, "value: %s", value);
        byteBuf.writeByte(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeBoolean(boolean value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeByte(value ? 1 : 0);
        return this;
    }

    @Override
    public SocketResponseOprs writeBytes(byte[] value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value != null);
        byteBuf.writeBytes(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeShort(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE, "value: %s", value);
        byteBuf.writeShort(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeUnsignedShort(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Short.MAX_VALUE - Short.MIN_VALUE, "value: %s", value);
        byteBuf.writeShort(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeInt(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeInt(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeUnsignedInt(long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeInt((int) value);
        return this;
    }

    @Override
    public SocketResponseOprs writeVarInt32(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        VarIntUtils.writeRawVarInt32(byteBuf, value);
        return this;
    }

    @Override
    public SocketResponseOprs writeFloat(float value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Float.MIN_VALUE && value <= Float.MAX_VALUE, "value: %s", value);
        byteBuf.writeFloat(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeLong(long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeLong(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeVarLong64(long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        VarIntUtils.writeRawVarInt64(byteBuf, value);
        return this;
    }

    @Override
    public SocketResponseOprs writeDouble(double value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Double.MIN_VALUE && value <= Double.MAX_VALUE, "value: %s", value);
        byteBuf.writeDouble(value);
        return this;
    }

    private void writeString0(String value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value != null);
        byte[] content = value.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(content.length);
        byteBuf.writeBytes(content);
    }

    @Override
    public SocketResponseOprs writeString(String value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        writeString0(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeBigString(String value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        writeString0(value);
        return this;
    }

    @Override
    public SocketResponseOprs setBoolean(int index, boolean value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setBoolean(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setByte(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE, "value: %s", value);
        byteBuf.setByte(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setUnsignedByte(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Byte.MAX_VALUE - Byte.MIN_VALUE, "value: %s", value);
        byteBuf.setByte(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setShort(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE, "value: %s", value);
        byteBuf.setShort(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setUnsignedShort(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Short.MAX_VALUE - Short.MIN_VALUE, "value: %s", value);
        byteBuf.setShort(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setInt(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setInt(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setUnsignedInt(int index, long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setInt(index, (int) value);
        return this;
    }

    @Override
    public SocketResponseOprs setLong(int index, long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setLong(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setFloat(int index, float value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Float.MIN_VALUE && value <= Float.MAX_VALUE, "value: %s", value);
        byteBuf.setFloat(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setDouble(int index, double value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Double.MIN_VALUE && value <= Double.MAX_VALUE, "value: %s", value);
        byteBuf.setDouble(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setBytes(int index, byte[] value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value != null);
        byteBuf.setBytes(index, value);
        return this;
    }
}
