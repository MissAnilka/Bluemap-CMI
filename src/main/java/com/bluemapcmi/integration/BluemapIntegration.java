package com.bluemapcmi.integration;

import com.bluemapcmi.BluemapCMIPlugin;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class BluemapIntegration {

    private final BluemapCMIPlugin plugin;
    private BlueMapAPI bluemapAPI;
    private final Map<String, POIMarker> activeMarkers = new HashMap<>();
    private MarkerSet warpsMarkerSet;
    private MarkerSet spawnMarkerSet;
    private MarkerSet firstSpawnMarkerSet;

    public BluemapIntegration(BluemapCMIPlugin plugin) throws Exception {
        this.plugin = plugin;
        
        // Get API instance - should be available when called from API consumer
        if (!BlueMapAPI.getInstance().isPresent()) {
            throw new Exception("BlueMap API is not available");
        }
        
        this.bluemapAPI = BlueMapAPI.getInstance().get();

        plugin.getLogger().info("BluemapIntegration initialized successfully");
    }

    public void initializeMarkers() {
        try {
            // Get all maps
            Collection<BlueMapMap> maps = bluemapAPI.getMaps();
            
            if (maps.isEmpty()) {
                plugin.getLogger().warning("No BlueMap maps found!");
                return;
            }

            // Remove old marker sets from all maps to prevent duplicates
            for (BlueMapMap map : maps) {
                map.getMarkerSets().remove("cmi-locations");  // Old marker set ID
                map.getMarkerSets().remove("cmi-spawns");     // Old marker set ID
                map.getMarkerSets().remove("cmi-warps");      // Clear current to recreate
                map.getMarkerSets().remove("cmi-spawn");
                map.getMarkerSets().remove("cmi-firstspawn");
            }

            // Create separate marker sets for warps, spawn, and first spawn
            warpsMarkerSet = MarkerSet.builder()
                .label("CMI-Warps")
                .toggleable(true)
                .defaultHidden(false)
                .build();
            
            spawnMarkerSet = MarkerSet.builder()
                .label("CMI-Spawn")
                .toggleable(true)
                .defaultHidden(false)
                .build();
            
            firstSpawnMarkerSet = MarkerSet.builder()
                .label("CMI-FirstSpawn")
                .toggleable(true)
                .defaultHidden(false)
                .build();
            
            // Add markers to their respective sets
            addSpawnMarkers();
            addWarpMarkers();
            addFirstSpawnMarker();
            
            // Add marker sets to all maps
            for (BlueMapMap map : maps) {
                map.getMarkerSets().put("cmi-warps", warpsMarkerSet);
                map.getMarkerSets().put("cmi-spawn", spawnMarkerSet);
                map.getMarkerSets().put("cmi-firstspawn", firstSpawnMarkerSet);
                plugin.getLogger().info("Markers initialized for map: " + map.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error initializing markers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSpawnMarkers() {
        if (!plugin.getConfig().getBoolean("spawn-marker.enabled", true)) {
            return;
        }

        CMIIntegration cmiIntegration = plugin.getCMIIntegration();
        Location spawnLocation = cmiIntegration.getSpawn();

        if (spawnLocation != null && spawnLocation.getWorld() != null) {
            // Check if world is blacklisted
            if (isWorldBlacklisted(spawnLocation.getWorld().getName())) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("Skipping spawn marker - world " + spawnLocation.getWorld().getName() + " is blacklisted");
                }
                return;
            }
            
            addMarker(
                "spawn",
                spawnLocation,
                plugin.getConfig().getString("spawn-marker.label", "Spawn"),
                plugin.getConfig().getString("spawn-marker.description", "Server spawn location"),
                "spawn"
            );
            if (plugin.getConfig().getBoolean("settings.log-marker-additions", true)) {
                plugin.getLogger().info("Spawn marker added at " + formatLocation(spawnLocation));
            }
        }
    }

    private void addFirstSpawnMarker() {
        if (!plugin.getConfig().getBoolean("first-spawn-marker.enabled", true)) {
            return;
        }

        CMIIntegration cmiIntegration = plugin.getCMIIntegration();
        Location firstSpawnLocation = cmiIntegration.getFirstSpawn();

        if (firstSpawnLocation != null && firstSpawnLocation.getWorld() != null) {
            // Check if world is blacklisted
            if (isWorldBlacklisted(firstSpawnLocation.getWorld().getName())) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("Skipping first spawn marker - world " + firstSpawnLocation.getWorld().getName() + " is blacklisted");
                }
                return;
            }
            
            addMarker(
                "first-spawn",
                firstSpawnLocation,
                plugin.getConfig().getString("first-spawn-marker.label", "First Spawn"),
                plugin.getConfig().getString("first-spawn-marker.description", "First spawn location for new players"),
                "firstspawn"
            );
            if (plugin.getConfig().getBoolean("settings.log-marker-additions", true)) {
                plugin.getLogger().info("First Spawn marker added at " + formatLocation(firstSpawnLocation));
            }
        }
    }

    private void addWarpMarkers() {
        if (!plugin.getConfig().getBoolean("warps-marker.enabled", true)) {
            return;
        }

        CMIIntegration cmiIntegration = plugin.getCMIIntegration();
        Map<String, Location> warps = cmiIntegration.getWarps();
        int maxWarps = plugin.getConfig().getInt("warps-marker.max-warps", 0);
        int count = 0;
        int skipped = 0;

        for (Map.Entry<String, Location> warp : warps.entrySet()) {
            if (maxWarps > 0 && count >= maxWarps) {
                break;
            }

            Location warpLocation = warp.getValue();
            if (warpLocation != null && warpLocation.getWorld() != null) {
                // Check if world is blacklisted
                if (isWorldBlacklisted(warpLocation.getWorld().getName())) {
                    skipped++;
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().info("Skipping warp '" + warp.getKey() + "' - world " + warpLocation.getWorld().getName() + " is blacklisted");
                    }
                    continue;
                }
                
                addMarker(
                    "warp-" + warp.getKey(),
                    warpLocation,
                    "Warp: " + warp.getKey(),
                    "Warp point: " + warp.getKey(),
                    "warp"
                );
                count++;
            }
        }

        if (plugin.getConfig().getBoolean("settings.log-marker-additions", true)) {
            plugin.getLogger().info(count + " warp markers added" + (skipped > 0 ? " (" + skipped + " skipped from blacklisted worlds)" : ""));
        }
    }

    private void addMarker(String markerId, Location location, String label, String description, String markerType) {
        try {
            POIMarker marker = POIMarker.builder()
                .label(label)
                .position(location.getX(), location.getY(), location.getZ())
                .build();

            MarkerSet targetSet = null;
            switch (markerType) {
                case "warp":
                    targetSet = warpsMarkerSet;
                    break;
                case "spawn":
                    targetSet = spawnMarkerSet;
                    break;
                case "firstspawn":
                    targetSet = firstSpawnMarkerSet;
                    break;
            }
            
            if (targetSet != null) {
                targetSet.getMarkers().put(markerId, marker);
                activeMarkers.put(markerId, marker);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add marker " + markerId + ": " + e.getMessage());
        }
    }

    public void updateMarkers() {
        try {
            // Clear old markers
            if (warpsMarkerSet != null) {
                warpsMarkerSet.getMarkers().clear();
            }
            if (spawnMarkerSet != null) {
                spawnMarkerSet.getMarkers().clear();
            }
            if (firstSpawnMarkerSet != null) {
                firstSpawnMarkerSet.getMarkers().clear();
            }
            activeMarkers.clear();

            // Re-add all markers
            addSpawnMarkers();
            addFirstSpawnMarker();
            addWarpMarkers();
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating markers: " + e.getMessage());
        }
    }

    public void cleanup() {
        try {
            if (warpsMarkerSet != null) {
                warpsMarkerSet.getMarkers().clear();
            }
            if (spawnMarkerSet != null) {
                spawnMarkerSet.getMarkers().clear();
            }
            if (firstSpawnMarkerSet != null) {
                firstSpawnMarkerSet.getMarkers().clear();
            }
            activeMarkers.clear();
        } catch (Exception e) {
            plugin.getLogger().warning("Error cleaning up markers: " + e.getMessage());
        }
    }

    private String formatLocation(Location location) {
        if (location == null) return "null";
        if (location.getWorld() == null) return "unknown-world";
        return String.format("%s [%.0f, %.0f, %.0f]", 
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }

    /**
     * Check if a world is blacklisted in the config
     */
    private boolean isWorldBlacklisted(String worldName) {
        if (worldName == null) return false;
        
        java.util.List<String> blacklist = plugin.getConfig().getStringList("world-blacklist");
        return blacklist != null && blacklist.contains(worldName);
    }

}
