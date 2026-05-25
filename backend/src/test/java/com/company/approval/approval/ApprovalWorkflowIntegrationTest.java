package com.company.approval.approval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("postgres")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApprovalWorkflowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void employeeSubmitManagerApproveAndNotificationsAreCreated() {
        String employeeToken = login("employee01");
        String managerToken = login("manager01");
        String hrToken = login("hr01");

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("title", "integration approval " + System.currentTimeMillis());
        payload.put("type", "leave");
        payload.put("urgent", false);
        payload.put("startTime", "2026-06-01T09:00:00+08:00");
        payload.put("endTime", "2026-06-01T18:00:00+08:00");
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("leaveType", "annual");
        form.put("reason", "integration");
        payload.put("formData", form);

        Map<String, Object> created = post("/api/approvals", employeeToken, payload);
        Map<String, Object> createdData = data(created);
        Number requestId = (Number) createdData.get("id");
        assertNotNull(requestId);

        List<Map<String, Object>> managerTodo = listData(get("/api/approvals/todo", managerToken));
        Map<String, Object> managerTask = findByRequestId(managerTodo, requestId.longValue());
        assertNotNull(managerTask, "manager01 should receive leave approval todo");

        List<Map<String, Object>> managerNotifications = listData(get("/api/notifications", managerToken));
        assertTrue(managerNotifications.stream().anyMatch(item -> requestId.longValue() == ((Number) item.get("relatedRequestId")).longValue()));

        Map<String, Object> approveBody = new HashMap<String, Object>();
        approveBody.put("comment", "approved by integration test - manager");
        Map<String, Object> managerApproved = post("/api/approvals/tasks/" + managerTask.get("id") + "/approve", managerToken, approveBody);
        // 主管同意后应进入 HR 确认节点，整体请求仍处于 in_progress
        assertEquals("in_progress", data(managerApproved).get("requestStatus"));

        List<Map<String, Object>> hrTodo = listData(get("/api/approvals/todo", hrToken));
        Map<String, Object> hrTask = findByRequestId(hrTodo, requestId.longValue());
        assertNotNull(hrTask, "hr01 should receive HR confirmation todo after manager approve");

        Map<String, Object> hrApproveBody = new HashMap<String, Object>();
        hrApproveBody.put("comment", "approved by integration test - hr");
        Map<String, Object> finalApproved = post("/api/approvals/tasks/" + hrTask.get("id") + "/approve", hrToken, hrApproveBody);
        assertEquals("approved", data(finalApproved).get("requestStatus"));

        List<Map<String, Object>> employeeNotifications = listData(get("/api/notifications", employeeToken));
        assertTrue(employeeNotifications.stream().anyMatch(item -> "approved".equals(item.get("type"))
                && requestId.longValue() == ((Number) item.get("relatedRequestId")).longValue()));
    }

    private String login(String username) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("username", username);
        body.put("password", "admin123");
        Map<String, Object> response = post("/api/auth/login", null, body);
        return String.valueOf(data(response).get("accessToken"));
    }

    private Map<String, Object> get(String path, String token) {
        ResponseEntity<Map> response = restTemplate.exchange(path, org.springframework.http.HttpMethod.GET, new HttpEntity<Object>(headers(token)), Map.class);
        return response.getBody();
    }

    private Map<String, Object> post(String path, String token, Map<String, Object> body) {
        ResponseEntity<Map> response = restTemplate.postForEntity(path, new HttpEntity<Map<String, Object>>(body, headers(token)), Map.class);
        return response.getBody();
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> data(Map<String, Object> response) {
        assertEquals(Boolean.TRUE, response.get("success"));
        return (Map<String, Object>) response.get("data");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listData(Map<String, Object> response) {
        assertEquals(Boolean.TRUE, response.get("success"));
        return (List<Map<String, Object>>) response.get("data");
    }

    private Map<String, Object> findByRequestId(List<Map<String, Object>> items, long requestId) {
        for (Map<String, Object> item : items) {
            if (requestId == ((Number) item.get("requestId")).longValue()) {
                return item;
            }
        }
        return null;
    }
}
