package com.walustik.reportstaff.models;

import java.util.UUID;

public record Report(UUID id, UUID reporterUUID, UUID targetUUID, String reason, long timestamp) {
}
