package com.callhippo.integration;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CallLogService {

    public CallLogSummary summarizeCallLogs(List<CallLogResponse> callLogs) {
        int incomingCount = 0, outgoingCount = 0;
        int incomingDuration = 0, outgoingDuration = 0;

        for (CallLogResponse log : callLogs) {
            if ("Incoming".equalsIgnoreCase(log.getCallType())) {
                incomingCount++;
                incomingDuration += log.getTotalCallDuration();
            } else if ("Outgoing".equalsIgnoreCase(log.getCallType())) {
                outgoingCount++;
                outgoingDuration += log.getTotalCallDuration();
            }
        }

        return new CallLogSummary(incomingCount, outgoingCount, incomingDuration, outgoingDuration);
    }

    public List<CallLogResponse> filterLogsByUserPhone(List<CallLogResponse> callLogs, String userPhone) {
        return callLogs.stream()
                .filter(log -> userPhone.equals(log.getFrom()) || userPhone.equals(log.getTo()))
                .collect(Collectors.toList());
    }
}
