package com.walustik.reportstaff.managers;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.database.ReportDAO;
import com.walustik.reportstaff.models.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling reports.
 * Implements caching to balance between database access and memory efficiency.
 * All database operations are asynchronous using CompletableFuture.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class ReportManager {

    private final ReportStaffPlugin plugin;
    private final ReportDAO reportDAO;
    private final ConcurrentHashMap<UUID, Report> cache = new ConcurrentHashMap<>();
    private volatile long lastCacheRefresh = 0;
    private static final long CACHE_REFRESH_INTERVAL = 5000; // 5 seconds

    public ReportManager(ReportStaffPlugin plugin) {
        this.plugin = plugin;
        this.reportDAO = new ReportDAO(plugin);
    }

    /**
     * Asynchronously creates and saves a report.
     *
     * @param reporterUUID UUID of the reporting player
     * @param targetUUID UUID of the reported player
     * @param reason The reason for the report
     * @return CompletableFuture that completes when report is saved
     */
    public CompletableFuture<Report> createReport(UUID reporterUUID, UUID targetUUID, String reason) {
        if (reporterUUID == null || targetUUID == null || reason == null || reason.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid report parameters"));
        }

        return CompletableFuture.supplyAsync(() -> {
            Report report = new Report(UUID.randomUUID(), reporterUUID, targetUUID, reason.trim(), System.currentTimeMillis());
            return report;
        }, plugin.getAsyncExecutor()).thenCompose(report -> reportDAO.saveReport(report).thenApply(v -> {
            cache.put(report.id(), report);
            return report;
        }));
    }

    /**
     * Asynchronously retrieves all active reports.
     * Uses cache with refresh interval to reduce database queries.
     *
     * @return CompletableFuture containing list of active reports
     */
    public CompletableFuture<List<Report>> getReports() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheRefresh > CACHE_REFRESH_INTERVAL) {
            return reportDAO.getAllReports().thenApply(reports -> {
                cache.clear();
                reports.forEach(report -> cache.put(report.id(), report));
                lastCacheRefresh = currentTime;
                return new ArrayList<>(cache.values());
            });
        }
        return CompletableFuture.completedFuture(new ArrayList<>(cache.values()));
    }

    /**
     * Asynchronously retrieves a report by ID.
     *
     * @param id The report ID
     * @return CompletableFuture containing the report or null if not found
     */
    public CompletableFuture<Report> getReportById(UUID id) {
        Report cached = cache.get(id);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return reportDAO.getReportById(id).thenApply(report -> {
            if (report != null) {
                cache.put(report.id(), report);
            }
            return report;
        });
    }

    /**
     * Asynchronously removes a report from the database.
     *
     * @param id The report ID
     * @return CompletableFuture that completes when report is deleted
     */
    public CompletableFuture<Void> removeReport(UUID id) {
        return reportDAO.deleteReport(id).thenRun(() -> {
            cache.remove(id);
            lastCacheRefresh = 0; // Force cache refresh on next query
        });
    }

    /**
     * Asynchronously gets the count of active reports.
     *
     * @return CompletableFuture containing the count
     */
    public CompletableFuture<Integer> getReportCount() {
        return reportDAO.getReportCount();
    }

    /**
     * Synchronously loads all reports from database into cache.
     * Should be called during plugin startup.
     *
     * @return CompletableFuture that completes when loading is done
     */
    public CompletableFuture<Void> loadAllReports() {
        return reportDAO.getAllReports().thenAccept(reports -> {
            cache.clear();
            reports.forEach(report -> cache.put(report.id(), report));
            plugin.getLogger().info("Loaded " + reports.size() + " active reports from database");
        });
    }

    /**
     * Clears the cache and forces a database refresh.
     */
    public void invalidateCache() {
        cache.clear();
        lastCacheRefresh = 0;
    }

    /**
     * Gets the size of the current cache.
     * This is for debugging purposes.
     *
     * @return the number of reports in cache
     */
    public int getCacheSize() {
        return cache.size();
    }
}
