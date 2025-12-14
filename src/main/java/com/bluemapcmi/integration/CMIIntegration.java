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

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("CMIIntegration initialized successfully");
        }
    }

    /**
     * Get the server spawn location from CMI
     */
    public Location getSpawn() {
        try {
            // Get default world spawn (CMI manages spawn through vanilla mechanics)
            World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (defaultWorld != null) {
                Location spawn = defaultWorld.getSpawnLocation();
                // Get the safe spawn location above ground by finding highest block
                if (spawn != null) {
                    Location safeSpawn = spawn.clone();
                    // Get the highest solid block at spawn coordinates
                    safeSpawn.setY(defaultWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
                    return safeSpawn;
                }
                return spawn;
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
            if (cmiPlugin != null && cmiPlugin.getWarpManager() != null) {
                // CMI WarpManager doesn't expose direct methods, so we use reflection
                // to access the internal warps map
                java.lang.reflect.Field warpsField = cmiPlugin.getWarpManager().getClass().getDeclaredField("warps");
                warpsField.setAccessible(true);
                Object warpsMap = warpsField.get(cmiPlugin.getWarpManager());
                
                if (warpsMap instanceof Map) {
                    Map<?, ?> cmiWarps = (Map<?, ?>) warpsMap;
                    
                    for (Map.Entry<?, ?> entry : cmiWarps.entrySet()) {
                        if (entry.getKey() != null && entry.getValue() != null) {
                            String warpName = entry.getKey().toString();
                            Object warpObj = entry.getValue();
                            
                            try {
                                // Try to get location using reflection
                                java.lang.reflect.Method getLocMethod = warpObj.getClass().getMethod("getLoc");
                                Location loc = (Location) getLocMethod.invoke(warpObj);
                                if (loc != null) {
                                    warps.put(warpName, loc);
                                }
                            } catch (Exception e) {
                                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                    plugin.getLogger().warning("Could not get location for warp '" + warpName + "': " + e.getMessage());
                                }
                            }
                        }
                    }
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().info("Retrieved " + warps.size() + " warps from CMI");
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting warps: " + e.getMessage());
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
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
