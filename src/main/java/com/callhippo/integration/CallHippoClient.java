package com.callhippo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class CallHippoClient {

    @Value("${callhippo.api.key}")
    private String apiKey;

    @Value("${callhippo.api.baseurl}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Helper to format date if needed (returns as is for now)
    private String formatDate(String date) {
        return date;
    }

    // Fetch call logs with query parameters
    public ResponseEntity<String> getCallLogs(String fromDate, String toDate, String msisdn) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apiToken", apiKey);

        // Prepare JSON body
        StringBuilder requestJson = new StringBuilder();
        requestJson.append("{")
                .append("\"skip\": \"0\", ")
                .append("\"limit\": \"100\", ")
                .append("\"startDate\": \"").append(formatDate(fromDate)).append("\", ")
                .append("\"endDate\": \"").append(formatDate(toDate)).append("\"");
        if (msisdn != null && !msisdn.isEmpty()) {
            requestJson.append(", \"msisdn\": \"").append(msisdn).append("\"");
        }
        requestJson.append("}");

        HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers);
        String url = baseUrl + "/activityfeed";

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    // Fetch and parse call logs into objects
    public List<CallLogResponse> getCallLogsAsObjects(String fromDate, String toDate, String msisdn) throws Exception {
        ResponseEntity<String> response = getCallLogs(fromDate, toDate, msisdn);

        System.out.println("Raw CallHippo response: " + response.getBody());

        if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data");
            JsonNode callLogsNode = dataNode.path("callLogs"); // <-- FIXED FIELD NAME

            if (callLogsNode == null || callLogsNode.isMissingNode() || callLogsNode.isNull() || !callLogsNode.isArray()
                    || callLogsNode.size() == 0) {
                return List.of(); // Return empty list if no logs
            }

            return mapper.readValue(
                    callLogsNode.toString(),
                    mapper.getTypeFactory().constructCollectionType(List.class, CallLogResponse.class));
        } else {
            throw new RuntimeException("Failed to fetch call logs");
        }
    }
}
