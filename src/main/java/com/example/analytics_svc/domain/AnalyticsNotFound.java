package com.example.analytics_svc.domain;

public class AnalyticsNotFound extends RuntimeException {

    public AnalyticsNotFound(String message) {
        super(message);
    }
}
