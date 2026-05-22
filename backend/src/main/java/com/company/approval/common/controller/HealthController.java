package com.company.approval.common.controller;

import com.company.approval.common.response.ApiResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("status", "UP");
        data.put("service", "enterprise-approval");
        data.put("time", LocalDateTime.now().toString());
        return ApiResponse.success(data);
    }
}

