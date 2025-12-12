package com.bluemapcmi;

import com.bluemapcmi.integration.BluemapIntegration;
import com.bluemapcmi.integration.CMIIntegration;
import de.bluecolored.bluemap.api.BlueMapAPI;
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
            // Check if CMI is available
            if (Bukkit.getPluginManager().getPlugin("CMI") == null) {
                getLogger().severe("CMI plugin not found! Disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            // Initialize CMI integration
            this.cmiIntegration = new CMIIntegration(this);

            // Check if BlueMap is available
            if (Bukkit.getPluginManager().getPlugin("BlueMap") == null) {
                getLogger().warning("BlueMap plugin not found! Markers will not be displayed.");
                return;
            }

            // Initialize BlueMap integration asynchronously
            initializeBlueMapIntegration();

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

    private void initializeBlueMapIntegration() {
        // Register BlueMapAPI consumer to initialize when API is available
        BlueMapAPI.onEnable(api -> {
            try {
                getLogger().info("BlueMap API is now available, initializing integration...");
                this.bluemapIntegration = new BluemapIntegration(this);
                bluemapIntegration.initializeMarkers();
                getLogger().info("BlueMap integration initialized successfully!");
            } catch (Exception e) {
                getLogger().severe("Failed to initialize BlueMap integration: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Also register for disable event
        BlueMapAPI.onDisable(api -> {
            getLogger().info("BlueMap API disabled");
        });
    }

    @Override
    public void onDisable() {
        // Stop update task
        if (updateTask != null) {
            updateTask.shutdown();
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
