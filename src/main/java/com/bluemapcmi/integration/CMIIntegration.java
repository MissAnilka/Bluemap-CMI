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
            // Get default world spawn (CMI manages spawn through vanilla mechanics)
            World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (defaultWorld != null) {
                return defaultWorld.getSpawnLocation();
            }
            
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting spawn location: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the first spawn location (for new players) from CMI
     */
    public Location getFirstSpawn() {
        try {
            // CMI first spawn is typically same as spawn
            // If CMI has configured a different first spawn, it will be at the world spawn
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
            if (cmiPlugin != null) {
                // Get all warps from CMI's Warp Manager
                HashMap<String, com.Zrips.CMI.Modules.Warps.CuboidArea> cmiWarps = cmiPlugin.getWarpManager().getWarps();
                
                if (cmiWarps != null && !cmiWarps.isEmpty()) {
                    for (Map.Entry<String, com.Zrips.CMI.Modules.Warps.CuboidArea> entry : cmiWarps.entrySet()) {
                        com.Zrips.CMI.Modules.Warps.CuboidArea warpArea = entry.getValue();
                        if (warpArea != null && warpArea.getCenter() != null) {
                            warps.put(entry.getKey(), warpArea.getCenter());
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
