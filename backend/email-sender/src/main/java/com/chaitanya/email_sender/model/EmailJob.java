
package com.chaitanya.email_sender.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmailJob {

    private final String jobId = UUID.randomUUID().toString();

    private final int total;

    private volatile int processed;
    private volatile int sent;
    private volatile int failed;

    private volatile String status = "QUEUED";

    private final List<Map<String, String>> results =
            new ArrayList<>();

    public EmailJob(int total) {
        this.total = total;
    }

    public String getJobId() {
        return jobId;
    }

    public int getTotal() {
        return total;
    }

    public int getProcessed() {
        return processed;
    }

    public int getSent() {
        return sent;
    }

    public int getFailed() {
        return failed;
    }

    public String getStatus() {
        return status;
    }

    public synchronized void addResult(
            String email,
            String resultStatus,
            String message) {

        results.add(Map.of(
                "email", email,
                "status", resultStatus,
                "message", message
        ));

        processed++;

        if ("SENT".equals(resultStatus)) {
            sent++;
        } else {
            failed++;
        }
    }

    public synchronized List<Map<String, String>> getResults() {
        return new ArrayList<>(results);
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
