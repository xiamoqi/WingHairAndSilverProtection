package com.yiguardsilverfa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@MapperScan("com.yiguardsilverfa.dao")
@SpringBootApplication
@RestController
//@ComponentScan(basePackages = {"com.yifasilverguard.controller", "com.yifasilverguard.service"})
public class YiGuardSilverFaApplication {

    public static void main(String[] args) {
        SpringApplication.run(YiGuardSilverFaApplication.class, args);
    }

}
