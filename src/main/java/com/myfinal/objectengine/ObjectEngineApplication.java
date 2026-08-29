package com.myfinal.objectengine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.myfinal.objectengine.mapper")
public class ObjectEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjectEngineApplication.class, args);
    }

}
