package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Arrays;
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
    @GetMapping("/html")
    public Mono<String> getHtml() {
        System.out.println(Thread.currentThread().getName());
        return Mono.just("test.html");
    }

    @GetMapping("/list")
    public Flux<String> getList() {
        System.out.println(Thread.currentThread().getName());
        return Flux.fromIterable(Arrays.asList("1", "2", "3", "4"));
    }

    @GetMapping("/query")
    public Data query(@RequestParam("i1") int i1,
                      @RequestParam("i2") int i2,
                      HttpServerRequest request,
                      @RequestParam("i3") List<Integer> i3) {
        Map<String, Object> args = new HashMap<>();
        args.put("i1", i1);
        args.put("i2", i2);
        args.put("i3", i3);
        return new Data(1, args);
    }

    @GetMapping("/body/{param1}")
    public Data getBody(@RequestParam("param1") String param1, @RequestHeader("user") String user, @RequestBody Data data) {
        data.getArgs().put("param1", param1);
        data.getArgs().put("user", user);
        return data;
    }
}
