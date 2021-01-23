package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
@Controller
@RequestMapping("/anno")
public class PrintController {
    @GetMapping("/test")
    public String getHtml() {
        return "test.html";
    }

    @GetMapping("/data")
    public Data getData(@RequestParam("i1") int i1,
                        @RequestParam("i2") int i2,
                        ServletRequest request,
                        @RequestParam("i3") List<Integer> i3) {
        Map<String, Object> args = new HashMap<>();
        args.put("i1", i1);
        args.put("i2", i2);
        args.put("i3", i3);
        return new Data(1, args);
    }
}
