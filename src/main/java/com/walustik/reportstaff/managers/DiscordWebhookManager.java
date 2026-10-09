package com.walustik.reportstaff.managers;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.models.Report;
import org.bukkit.Bukkit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class DiscordWebhookManager {

    private final ReportStaffPlugin plugin;

    public DiscordWebhookManager(ReportStaffPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendReport(Report report) {
        String url = plugin.getConfig().getString("discord.webhook-url", "");
        if (url == null || url.isBlank()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String reporterName = Bukkit.getOfflinePlayer(report.reporterUUID()).getName();
                String targetName = Bukkit.getOfflinePlayer(report.targetUUID()).getName();
                String payload = "{"
                        + "\"username\":\"ReportStaff\","
                        + "\"embeds\":[{"
                        + "\"title\":\"New report received\","
                        + "\"color\":16711680,"
                        + "\"fields\":["
                        + "{\"name\":\"Reporter\",\"value\":\"" + reporterName + "\",\"inline\":true},"
                        + "{\"name\":\"Target\",\"value\":\"" + targetName + "\",\"inline\":true},"
                        + "{\"name\":\"Reason\",\"value\":\"" + report.reason().replace("\n", " ").replace("\"", "\\\"") + "\",\"inline\":false}"
                        + "]"
                        + "}]"
                        + "}";

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    plugin.getLogger().warning("Discord webhook failed: " + response.statusCode() + " :: " + response.body());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }
}
