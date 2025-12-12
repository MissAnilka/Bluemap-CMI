package com.bluemapcmi;

import org.bukkit.Bukkit;

public class UpdateTask extends Thread {

    private final BluemapCMIPlugin plugin;
    private volatile boolean running = true;

    public UpdateTask(BluemapCMIPlugin plugin) {
        this.plugin = plugin;
        setName("BluemapCMI-UpdateTask");
        setDaemon(true);
    }

    @Override
    public void run() {
        long updateInterval = plugin.getConfig().getInt("settings.update-interval", 300) * 1000L; // Convert to milliseconds

        while (running) {
            try {
                Thread.sleep(updateInterval);
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        plugin.getBluemapIntegration().updateMarkers();
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("Markers updated successfully");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error updating markers: " + e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                if (running) {
                    plugin.getLogger().warning("Update task interrupted: " + e.getMessage());
                }
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

}
