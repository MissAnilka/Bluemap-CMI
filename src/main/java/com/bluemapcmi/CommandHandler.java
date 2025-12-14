package com.bluemapcmi;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final BluemapCMIPlugin plugin;
    private FileConfiguration messages;

    public CommandHandler(BluemapCMIPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bluemapcmi.admin")) {
            sender.sendMessage(colorize(messages.getString("no-permission", "&cYou don't have permission to use this command!")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "toggle":
                if (args.length < 2) {
                    sender.sendMessage(colorize(messages.getString("toggle-usage", "&cUsage: /bluemapcmi toggle <spawn|firstspawn|warp>")));
                    return true;
                }
                handleToggle(sender, args[1].toLowerCase());
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reloadConfig();
            loadMessages();
            
            // Reload markers
            if (plugin.getBluemapIntegration() != null) {
                plugin.getBluemapIntegration().updateMarkers();
            }
            
            sender.sendMessage(colorize(messages.getString("reload-success", "&aConfiguration reloaded successfully!")));
            
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("Configuration reloaded by " + sender.getName());
            }
        } catch (Exception e) {
            sender.sendMessage(colorize(messages.getString("reload-error", "&cError reloading configuration: {error}").replace("{error}", e.getMessage())));
            e.printStackTrace();
        }
    }

    private void handleToggle(CommandSender sender, String markerType) {
        String configPath;
        String markerName;

        switch (markerType) {
            case "spawn":
                configPath = "spawn-marker.enabled";
                markerName = "Spawn";
                break;
            case "firstspawn":
                configPath = "first-spawn-marker.enabled";
                markerName = "First Spawn";
                break;
            case "warp":
            case "warps":
                configPath = "warps-marker.enabled";
                markerName = "Warps";
                break;
            default:
                sender.sendMessage(colorize(messages.getString("toggle-invalid", "&cInvalid marker type! Use: spawn, firstspawn, or warp")));
                return;
        }

        boolean currentState = plugin.getConfig().getBoolean(configPath, true);
        boolean newState = !currentState;
        
        plugin.getConfig().set(configPath, newState);
        plugin.saveConfig();

        // Update markers immediately
        if (plugin.getBluemapIntegration() != null) {
            plugin.getBluemapIntegration().updateMarkers();
        }

        String statusKey = newState ? "toggle-enabled" : "toggle-disabled";
        String message = messages.getString(statusKey, newState ? "&a{marker} markers enabled!" : "&c{marker} markers disabled!")
                .replace("{marker}", markerName);
        sender.sendMessage(colorize(message));

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info(markerName + " markers " + (newState ? "enabled" : "disabled") + " by " + sender.getName());
        }
    }

    private void sendHelp(CommandSender sender) {
        List<String> helpMessages = messages.getStringList("help");
        if (helpMessages.isEmpty()) {
            helpMessages = Arrays.asList(
                "&6&lBluemapCMI Commands:",
                "&e/bluemapcmi reload &7- Reload configuration",
                "&e/bluemapcmi toggle <spawn|firstspawn|warp> &7- Toggle marker visibility"
            );
        }
        
        for (String line : helpMessages) {
            sender.sendMessage(colorize(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("bluemapcmi.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("toggle");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            completions.add("spawn");
            completions.add("firstspawn");
            completions.add("warp");
        }

        return completions;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
