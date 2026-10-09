package com.walustik.reportstaff.models;

import java.util.UUID;

/**
 * Represents a report record in the database.
 * Uses Java 21 record feature for immutability and concise definition.
 *
 * @param id The unique identifier of the report
 * @param reporterUUID The UUID of the player who filed the report
 * @param targetUUID The UUID of the player being reported
 * @param reason The reason for the report
 * @param timestamp The time when the report was created (milliseconds since epoch)
 *
 * @author Walustik
 * @version 1.1.0
 */
public record Report(UUID id, UUID reporterUUID, UUID targetUUID, String reason, long timestamp) {

    /**
     * Validates the report record.
     * Throws IllegalArgumentException if any field is invalid.
     */
    public Report {
        if (id == null) {
            throw new IllegalArgumentException("Report ID cannot be null");
        }
        if (reporterUUID == null) {
            throw new IllegalArgumentException("Reporter UUID cannot be null");
        }
        if (targetUUID == null) {
            throw new IllegalArgumentException("Target UUID cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }

    /**
     * Gets the age of this report in seconds.
     *
     * @return the age of the report in seconds
     */
    public long getAgeInSeconds() {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }

    /**
     * Gets the age of this report in a human-readable format.
     *
     * @return a formatted string representing the age (e.g., "5 minutes ago")
     */
    public String getFormattedAge() {
        long ageInSeconds = getAgeInSeconds();
        if (ageInSeconds < 60) {
            return ageInSeconds + "s ago";
        }
        long ageInMinutes = ageInSeconds / 60;
        if (ageInMinutes < 60) {
            return ageInMinutes + "m ago";
        }
        long ageInHours = ageInMinutes / 60;
        if (ageInHours < 24) {
            return ageInHours + "h ago";
        }
        long ageInDays = ageInHours / 24;
        return ageInDays + "d ago";
    }
}
