package com.walustik.reportstaff.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownManager {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public boolean isCoolingDown(UUID playerId) {
        Long expiresAt = cooldowns.get(playerId);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expiresAt) {
            cooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    public void setCooldown(UUID playerId, long seconds) {
        cooldowns.put(playerId, System.currentTimeMillis() + (seconds * 1000L));
    }
}
