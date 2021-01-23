package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.Controller;
import org.kin.transport.netty.http.server.GetMapping;
import org.kin.transport.netty.http.server.RequestMapping;

import java.util.HashMap;
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
    public Data getData() {
        Map<String, String> args = new HashMap<>();
        args.put("2", "3");
        args.put("3", "4");
        return new Data(1, args);
    }
}
