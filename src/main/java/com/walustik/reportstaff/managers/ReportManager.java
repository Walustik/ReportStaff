package com.walustik.reportstaff.managers;

import com.walustik.reportstaff.models.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ReportManager {

    private final List<Report> activeReports = new ArrayList<>();

    public void createReport(UUID reporterUUID, UUID targetUUID, String reason) {
        if (reporterUUID == null || targetUUID == null || reason == null || reason.isBlank()) {
            return;
        }

        activeReports.add(new Report(UUID.randomUUID(), reporterUUID, targetUUID, reason.trim(), System.currentTimeMillis()));
    }

    public List<Report> getReports() {
        return List.copyOf(activeReports);
    }

    public Report getReportById(UUID id) {
        return activeReports.stream()
                .filter(report -> report.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void removeReport(UUID id) {
        activeReports.removeIf(report -> report.id().equals(id));
    }
}
