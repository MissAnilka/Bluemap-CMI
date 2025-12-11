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
    private final BlueMapAPI bluemapAPI;
    private final Map<String, POIMarker> activeMarkers = new HashMap<>();
    private MarkerSet markerSet;

    public BluemapIntegration(BluemapCMIPlugin plugin) throws Exception {
        this.plugin = plugin;
        
        // Initialize BlueMap API
        BlueMapAPI.onEnable((api) -> {
            this.bluemapAPI = api;
            plugin.getLogger().info("BlueMap API initialized");
        });

        // Get API instance
        if (!BlueMapAPI.isLoaded()) {
            Thread.sleep(1000); // Wait for API to load
        }
        this.bluemapAPI = BlueMapAPI.getInstance().orElse(null);

        if (bluemapAPI == null) {
            throw new Exception("BlueMap API is not available");
        }

        plugin.getLogger().info("BluemapIntegration initialized successfully");
    }

    public void initializeMarkers() {
        try {
            // Create marker set
            Collection<BlueMapMap> maps = bluemapAPI.getMaps();
            
            if (maps.isEmpty()) {
                plugin.getLogger().warning("No BlueMap maps found!");
                return;
            }

            for (BlueMapMap map : maps) {
                markerSet = map.getMarkerSets()
                    .stream()
                    .filter(ms -> ms.getLabel().equals("CMI Locations"))
                    .findFirst()
                    .orElseGet(() -> map.createMarkerSet("cmi-locations"));

                markerSet.setLabel("CMI Locations");
                
                addSpawnMarkers();
                addWarpMarkers();
                addFirstSpawnMarker();

                plugin.getLogger().info("Markers initialized for map: " + map.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error initializing markers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSpawnMarkers() {
        if (!plugin.getConfig().getBoolean("integrations.spawn", true)) {
            return;
        }

        CMIIntegration cmiIntegration = plugin.getCMIIntegration();
        Location spawnLocation = cmiIntegration.getSpawn();

        if (spawnLocation != null && spawnLocation.getWorld() != null) {
            addMarker(
                "spawn",
                spawnLocation,
                plugin.getConfig().getString("spawn-marker.label", "Spawn"),
                plugin.getConfig().getString("spawn-marker.description", "Server spawn location")
            );
            if (plugin.getConfig().getBoolean("settings.log-marker-additions", true)) {
                plugin.getLogger().info("Spawn marker added at " + formatLocation(spawnLocation));
            }
        }
    }

    private void addFirstSpawnMarker() {
        if (!plugin.getConfig().getBoolean("integrations.first-spawn", true)) {
            return;
        }

        CMIIntegration cmiIntegration = plugin.getCMIIntegration();
        Location firstSpawnLocation = cmiIntegration.getFirstSpawn();

        if (firstSpawnLocation != null && firstSpawnLocation.getWorld() != null) {
            addMarker(
                "first-spawn",
                firstSpawnLocation,
                plugin.getConfig().getString("first-spawn-marker.label", "First Spawn"),
                plugin.getConfig().getString("first-spawn-marker.description", "First spawn location for new players")
            );
            if (plugin.getConfig().getBoolean("settings.log-marker-additions", true)) {
                plugin.getLogger().info("First Spawn marker added at " + formatLocation(firstSpawnLocation));
            }
        }
    }

    private void addWarpMarkers() {
        if (!plugin.getConfig().getBoolean("integrations.warps", true)) {
            return;
        }

        CMIIntegration cmiIntegration = plugin.getCMIIntegration();
        Map<String, Location> warps = cmiIntegration.getWarps();
        int maxWarps = plugin.getConfig().getInt("warps-marker.max-warps", 0);
        int count = 0;

        for (Map.Entry<String, Location> warp : warps.entrySet()) {
            if (maxWarps > 0 && count >= maxWarps) {
                break;
            }

            Location warpLocation = warp.getValue();
            if (warpLocation != null && warpLocation.getWorld() != null) {
                addMarker(
                    "warp-" + warp.getKey(),
                    warpLocation,
                    "Warp: " + warp.getKey(),
                    "Warp point: " + warp.getKey()
                );
                count++;
            }
        }

        if (plugin.getConfig().getBoolean("settings.log-marker-additions", true)) {
            plugin.getLogger().info(count + " warp markers added");
        }
    }

    private void addMarker(String markerId, Location location, String label, String description) {
        try {
            POIMarker marker = POIMarker.builder()
                .label(label)
                .description(description)
                .position(location.getX(), location.getY(), location.getZ())
                .build();

            if (markerSet != null) {
                markerSet.put(markerId, marker);
                activeMarkers.put(markerId, marker);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add marker " + markerId + ": " + e.getMessage());
        }
    }

    public void updateMarkers() {
        try {
            // Clear old markers
            markerSet.getMarkers().forEach((key, marker) -> markerSet.remove(key));
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
            if (markerSet != null && !activeMarkers.isEmpty()) {
                activeMarkers.forEach((key, marker) -> markerSet.remove(key));
                activeMarkers.clear();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error cleaning up markers: " + e.getMessage());
        }
    }

    private String formatLocation(Location location) {
        if (location == null) return "null";
        return String.format("%s [%.0f, %.0f, %.0f]", 
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }

}
