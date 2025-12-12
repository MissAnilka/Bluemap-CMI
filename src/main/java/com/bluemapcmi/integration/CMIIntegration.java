package com.bluemapcmi.integration;

import com.bluemapcmi.BluemapCMIPlugin;
import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class CMIIntegration {

    private final BluemapCMIPlugin plugin;
    private final CMI cmiPlugin;

    public CMIIntegration(BluemapCMIPlugin plugin) throws Exception {
        this.plugin = plugin;
        this.cmiPlugin = (CMI) Bukkit.getPluginManager().getPlugin("CMI");

        if (cmiPlugin == null) {
            throw new Exception("CMI plugin is not available");
        }

        plugin.getLogger().info("CMIIntegration initialized successfully");
    }

    /**
     * Get the server spawn location from CMI
     */
    public Location getSpawn() {
        try {
            if (cmiPlugin != null) {
                // Get CMI's configured spawn location
                Location spawn = cmiPlugin.getSpawnManager().getSpawnLocation();
                if (spawn != null) {
                    return spawn;
                }
            }
            
            // Fallback to default world spawn
            World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (defaultWorld != null) {
                return defaultWorld.getSpawnLocation();
            }
            
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting spawn location: " + e.getMessage());
            // Fallback to default world spawn
            World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            return defaultWorld != null ? defaultWorld.getSpawnLocation() : null;
        }
    }

    /**
     * Get the first spawn location (for new players) from CMI
     */
    public Location getFirstSpawn() {
        try {
            if (cmiPlugin != null) {
                // Get CMI's configured first spawn location
                Location firstSpawn = cmiPlugin.getSpawnManager().getFirstSpawnLocation();
                if (firstSpawn != null) {
                    return firstSpawn;
                }
            }
            
            // Fallback to regular spawn
            return getSpawn();
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting first spawn location: " + e.getMessage());
            return getSpawn();
        }
    }

    /**
     * Get all configured warps from CMI
     */
    public Map<String, Location> getWarps() {
        Map<String, Location> warps = new HashMap<>();
        
        try {
            if (cmiPlugin != null && cmiPlugin.getWarpManager() != null) {
                // Get all warps from CMI's Warp Manager
                Map<String, com.Zrips.CMI.Modules.Warps.WarpInfo> cmiWarps = cmiPlugin.getWarpManager().getWarps();
                
                if (cmiWarps != null && !cmiWarps.isEmpty()) {
                    for (Map.Entry<String, com.Zrips.CMI.Modules.Warps.WarpInfo> entry : cmiWarps.entrySet()) {
                        com.Zrips.CMI.Modules.Warps.WarpInfo warpInfo = entry.getValue();
                        if (warpInfo != null && warpInfo.getLoc() != null) {
                            warps.put(entry.getKey(), warpInfo.getLoc());
                        }
                    }
                    plugin.getLogger().info("Retrieved " + warps.size() + " warps from CMI");
                } else {
                    plugin.getLogger().info("No warps found in CMI");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting warps: " + e.getMessage());
            e.printStackTrace();
        }
        
        return warps;
    }

    /**
     * Check if CMI is properly loaded and accessible
     */
    public boolean isCMIAvailable() {
        return cmiPlugin != null && cmiPlugin.isEnabled();
    }

    /**
     * Get CMI plugin instance
     */
    public CMI getCMIPlugin() {
        return cmiPlugin;
    }

}
