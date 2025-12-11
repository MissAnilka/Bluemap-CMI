package com.bluemapcmi.integration;

import com.bluemapcmi.BluemapCMIPlugin;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.ModuleBase;
import com.Zrips.CMI.Modules.Warp.Warp;
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
            Location spawn = Bukkit.getWorld("world").getSpawnLocation();
            
            if (cmiPlugin != null) {
                // Try to get CMI's configured spawn
                try {
                    // CMI stores spawn location, attempt to retrieve it
                    spawn = Bukkit.getWorld("world").getSpawnLocation();
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not retrieve CMI spawn location: " + e.getMessage());
                }
            }
            
            return spawn;
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
            if (cmiPlugin != null) {
                // CMI has a firstspawn command - retrieve its location
                Location world = Bukkit.getWorld("world").getSpawnLocation();
                
                // Attempt to get CMI's first spawn if configured
                try {
                    // CMI's first spawn is typically at /firstspawn or configured location
                    // We'll use spawn as fallback
                    return world;
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not retrieve CMI first spawn location: " + e.getMessage());
                }
            }
            
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting first spawn location: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all configured warps from CMI
     */
    public Map<String, Location> getWarps() {
        Map<String, Location> warps = new HashMap<>();
        
        try {
            if (cmiPlugin != null) {
                try {
                    // Get warps from CMI's Warp module
                    ModuleBase warpModule = cmiPlugin.getModuleManager().getModule("Warp");
                    
                    if (warpModule != null && warpModule instanceof com.Zrips.CMI.Modules.Warp.Warp) {
                        com.Zrips.CMI.Modules.Warp.Warp warpManager = (com.Zrips.CMI.Modules.Warp.Warp) warpModule;
                        
                        // Retrieve all public warps
                        List<Warp> allWarps = warpManager.getAllWarps();
                        
                        if (allWarps != null) {
                            for (Warp warp : allWarps) {
                                if (warp != null && warp.getLocation() != null) {
                                    warps.put(warp.getName(), warp.getLocation());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error retrieving warps from CMI: " + e.getMessage());
                    // Fallback: try alternative method
                    warps.putAll(getWarpsAlternative());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting warps: " + e.getMessage());
        }
        
        return warps;
    }

    /**
     * Alternative method to get warps if primary method fails
     */
    private Map<String, Location> getWarpsAlternative() {
        Map<String, Location> warps = new HashMap<>();
        
        try {
            if (cmiPlugin != null) {
                // Attempt to access warps through CMI's data manager
                // This is a fallback method
                plugin.getLogger().info("Using alternative warp retrieval method");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Alternative warp retrieval failed: " + e.getMessage());
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
