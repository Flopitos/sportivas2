package com.sportivas.sportivas.service;

import java.util.List;
import java.util.Map;

public interface ActivityService {
    List<Map<String, Object>> getAllActivities();
    Map<String, Object> getSummaryStats();
}