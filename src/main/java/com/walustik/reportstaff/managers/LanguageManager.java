package com.walustik.reportstaff.managers;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class LanguageManager {

    private final ReportStaffPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();

    public LanguageManager(ReportStaffPlugin plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        messages.clear();
        String selectedLanguage = plugin.getConfig().getString("language", "en_US");
        File langDirectory = new File(plugin.getDataFolder(), "lang");
        if (!langDirectory.exists()) {
            langDirectory.mkdirs();
        }

        File languageFile = new File(langDirectory, selectedLanguage + ".yml");
        if (!languageFile.exists()) {
            plugin.saveResource("lang/" + selectedLanguage + ".yml", false);
        }

        if (!languageFile.exists()) {
            plugin.saveResource("lang/en_US.yml", false);
            languageFile = new File(langDirectory, "en_US.yml");
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(languageFile);
        for (String key : configuration.getKeys(true)) {
            if (configuration.isString(key)) {
                messages.put(key, configuration.getString(key));
            }
        }
    }

    public String getMessage(String key) {
        String value = messages.get(key);
        if (value == null) {
            return "&cMissing translation: " + key;
        }
        return ColorUtils.colorize(value);
    }

    public String getMessage(String key, Map<String, String> replacements) {
        String value = getMessage(key);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", ColorUtils.colorize(entry.getValue()));
        }
        return value;
    }
}
