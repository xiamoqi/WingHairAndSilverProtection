package com.yifasilverguard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

@MapperScan("com.yifasilverguard.dao")
@SpringBootApplication
@RestController
//@ComponentScan(basePackages = {"com.yifasilverguard.controller", "com.yifasilverguard.service"})
public class YifaSilverGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(YifaSilverGuardApplication.class, args);
    }

}
