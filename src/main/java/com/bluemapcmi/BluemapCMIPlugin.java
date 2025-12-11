package com.bluemapcmi;

import com.bluemapcmi.integration.BluemapIntegration;
import com.bluemapcmi.integration.CMIIntegration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class BluemapCMIPlugin extends JavaPlugin {

    private BluemapIntegration bluemapIntegration;
    private CMIIntegration cmiIntegration;
    private UpdateTask updateTask;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║     Bluemap CMI Integration Enabled    ║");
        getLogger().info("║           v" + getDescription().getVersion() + "                      ║");
        getLogger().info("╚════════════════════════════════════════╝");

        try {
            // Initialize integrations
            this.bluemapIntegration = new BluemapIntegration(this);
            this.cmiIntegration = new CMIIntegration(this);

            // Check if dependencies are available
            if (Bukkit.getPluginManager().getPlugin("BlueMap") == null) {
                getLogger().severe("BlueMap plugin not found! Disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            if (Bukkit.getPluginManager().getPlugin("CMI") == null) {
                getLogger().severe("CMI plugin not found! Disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            // Initialize markers from CMI data
            bluemapIntegration.initializeMarkers();

            // Start update task if enabled
            int updateInterval = getConfig().getInt("settings.update-interval", 300);
            if (updateInterval > 0) {
                updateTask = new UpdateTask(this);
                updateTask.start();
                getLogger().info("Marker update task started (interval: " + updateInterval + " seconds)");
            }

            getLogger().info("All integrations initialized successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to initialize plugin: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Stop update task
        if (updateTask != null) {
            updateTask.stop();
        }

        // Cleanup
        if (bluemapIntegration != null) {
            bluemapIntegration.cleanup();
        }

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║    Bluemap CMI Integration Disabled    ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    public BluemapIntegration getBluemapIntegration() {
        return bluemapIntegration;
    }

    public CMIIntegration getCMIIntegration() {
        return cmiIntegration;
    }

}
