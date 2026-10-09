package com.walustik.reportstaff.database;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.models.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Data access object for report database operations.
 * Provides asynchronous methods for CRUD operations using CompletableFuture.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class ReportDAO {

    private final ReportStaffPlugin plugin;
    private final DatabaseManager databaseManager;

    public ReportDAO(ReportStaffPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    /**
     * Asynchronously saves a report to the database.
     *
     * @param report the report to save
     * @return a CompletableFuture that completes when the save is done
     */
    public CompletableFuture<Void> saveReport(Report report) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO reports (id, reporter_uuid, target_uuid, reason, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, report.id().toString());
                statement.setString(2, report.reporterUUID().toString());
                statement.setString(3, report.targetUUID().toString());
                statement.setString(4, report.reason());
                statement.setLong(5, report.timestamp());
                statement.setString(6, "OPEN");
                statement.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save report: " + e.getMessage());
            }
        }, plugin.getAsyncExecutor());
    }

    /**
     * Asynchronously retrieves a report by ID from the database.
     *
     * @param id the report ID
     * @return a CompletableFuture containing the report, or empty if not found
     */
    public CompletableFuture<Report> getReportById(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT id, reporter_uuid, target_uuid, reason, timestamp FROM reports WHERE id = ? AND status = 'OPEN'";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, id.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new Report(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("reporter_uuid")),
                                UUID.fromString(resultSet.getString("target_uuid")),
                                resultSet.getString("reason"),
                                resultSet.getLong("timestamp")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to retrieve report: " + e.getMessage());
            }
            return null;
        }, plugin.getAsyncExecutor());
    }

    /**
     * Asynchronously retrieves all active reports from the database.
     *
     * @return a CompletableFuture containing a list of all active reports
     */
    public CompletableFuture<List<Report>> getAllReports() {
        return CompletableFuture.supplyAsync(() -> {
            List<Report> reports = new ArrayList<>();
            String sql = "SELECT id, reporter_uuid, target_uuid, reason, timestamp FROM reports WHERE status = 'OPEN' ORDER BY timestamp DESC";
            try (Connection connection = databaseManager.getConnection();
                 ResultSet resultSet = connection.createStatement().executeQuery(sql)) {

                while (resultSet.next()) {
                    reports.add(new Report(
                            UUID.fromString(resultSet.getString("id")),
                            UUID.fromString(resultSet.getString("reporter_uuid")),
                            UUID.fromString(resultSet.getString("target_uuid")),
                            resultSet.getString("reason"),
                            resultSet.getLong("timestamp")
                    ));
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to retrieve all reports: " + e.getMessage());
            }
            return reports;
        }, plugin.getAsyncExecutor());
    }

    /**
     * Asynchronously deletes a report from the database.
     *
     * @param id the report ID to delete
     * @return a CompletableFuture that completes when the delete is done
     */
    public CompletableFuture<Void> deleteReport(UUID id) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE reports SET status = 'RESOLVED' WHERE id = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, id.toString());
                statement.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete report: " + e.getMessage());
            }
        }, plugin.getAsyncExecutor());
    }

    /**
     * Asynchronously counts all active reports.
     *
     * @return a CompletableFuture containing the count of active reports
     */
    public CompletableFuture<Integer> getReportCount() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) as count FROM reports WHERE status = 'OPEN'";
            try (Connection connection = databaseManager.getConnection();
                 ResultSet resultSet = connection.createStatement().executeQuery(sql)) {

                if (resultSet.next()) {
                    return resultSet.getInt("count");
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to count reports: " + e.getMessage());
            }
            return 0;
        }, plugin.getAsyncExecutor());
    }

    /**
     * Asynchronously clears all reports from the database.
     * WARNING: This operation is irreversible.
     *
     * @return a CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> clearAllReports() {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM reports";
            try (Connection connection = databaseManager.getConnection()) {
                connection.createStatement().executeUpdate(sql);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to clear reports: " + e.getMessage());
            }
        }, plugin.getAsyncExecutor());
    }
}
