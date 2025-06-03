package com.callhippo.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallLogController {

    @Autowired
    private CallHippoClient callHippoClient;

    @Autowired
    private CallLogService callLogService;

    @GetMapping("/logs")
    public ResponseEntity<?> getCallLogs(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) String number) {

        if (fromDate.isEmpty() || toDate.isEmpty()) {
            return ResponseEntity.badRequest().body("fromDate and toDate are required.");
        }

        try {
            List<CallLogResponse> logs = callHippoClient.getCallLogsAsObjects(fromDate, toDate, number);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching call logs: " + e.getMessage());
        }
    }

    @GetMapping("/logs/userId")
    public ResponseEntity<?> getLogsByUserId(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String userId) {

        try {
            String userPhone = getPhoneNumberByUserId(userId);
            if (userPhone == null) {
                return ResponseEntity.badRequest().body("Invalid userId.");
            }
            List<CallLogResponse> callLogs = callHippoClient.getCallLogsAsObjects(fromDate, toDate, null);
            List<CallLogResponse> userLogs = callLogService.filterLogsByUserPhone(callLogs, userPhone);

            return ResponseEntity.ok(userLogs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching user call logs: " + e.getMessage());
        }
    }

    private String getPhoneNumberByUserId(String userId) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("U001", "+917004573138");
        userMap.put("U002", "+911234567890");
        return userMap.get(userId);
    }
}
