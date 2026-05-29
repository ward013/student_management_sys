package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @SpringBootApplication 是 Spring Boot 项目的启动注解。
// 它会开启自动配置，并从当前包 com.example.demo 开始向下扫描 Controller、Service、Mapper 等组件。
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        // 启动 Spring Boot 应用，内置 Tomcat 会随应用一起启动。
        SpringApplication.run(DemoApplication.class, args);
    }
}

// 这是一个最简单的测试接口，用来快速验证项目是否启动成功。
@RestController
class HelloController {

    // 浏览器访问 http://localhost:8080/hello 时，会返回下面的字符串。
    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Boot";
    }
}
