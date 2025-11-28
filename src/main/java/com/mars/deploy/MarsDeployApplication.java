package com.mars.deploy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("com.mars.deploy.mapper")
public class MarsDeployApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarsDeployApplication.class, args);
    }
}
