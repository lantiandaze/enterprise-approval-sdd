package com.company.approval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EnterpriseApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApprovalApplication.class, args);
    }
}

