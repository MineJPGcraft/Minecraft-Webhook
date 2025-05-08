package com.minecraft.webhook;

// Bukkit & Spigot API Imports
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

// Java Standard Library Imports
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * MinecraftWebhook插件主类
 */
public class MinecraftWebhook extends JavaPlugin implements CommandExecutor {
    
    private ConfigManager configManager;
    private WebhookSender webhookSender;
    private EventListener eventListener;
    private static final String PLUGIN_CONFIG_VERSION = "1.1.0"; // Current config version

    @Override
    public void onEnable() {
        // 保存默认配置 (如果不存在)
        saveDefaultConfig();
        
        // 加载配置并执行迁移 (如果需要)
        FileConfiguration config = getConfig();
        migrateConfig(config);
        saveConfig(); // 保存迁移后的配置

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化Webhook发送器
        webhookSender = new WebhookSender(this);
        
        // 注册事件监听器
        eventListener = new EventListener(this);
        getServer().getPluginManager().registerEvents(eventListener, this);
        
        // 注册命令
        getCommand("webhook").setExecutor(this);
        
        // 输出启动信息
        getLogger().info("MinecraftWebhook插件已启用! 版本: " + getDescription().getVersion());
        getLogger().info("监听玩家登录、离开、死亡、聊天和指令事件并发送到Webhook");
        
        // 检查配置
        if (!configManager.isWebhookEnabled()) {
            getLogger().warning("Webhook功能当前已禁用，请在配置文件中启用");
        } else {
            List<String> urls = configManager.getWebhookUrls();
            if (urls.isEmpty() || (urls.size() == 1 && urls.get(0).equals("https://example.com/webhook"))) {
                 getLogger().warning("Webhook URL 未配置或仍为默认值，请检查配置文件!");
            } else {
                getLogger().info("Webhook URLs: " + String.join(", ", urls));
            }
        }
    }

    private void migrateConfig(FileConfiguration config) {
        String currentVersion = config.getString("plugin-config-version");

        if (PLUGIN_CONFIG_VERSION.equals(currentVersion)) {
            // 配置已是最新版本
            return;
        }

        getLogger().info("检测到旧版本配置文件，正在尝试迁移...");
        boolean migrated = false;

        // 迁移 webhook.url 到 webhook.urls
        if (config.isSet("webhook.url") && !config.isSet("webhook.urls")) {
            String oldUrl = config.getString("webhook.url");
            if (oldUrl != null && !oldUrl.isEmpty()) {
                config.set("webhook.urls", Arrays.asList(oldUrl));
                config.set("webhook.url", null); // 移除旧字段
                getLogger().info("迁移 webhook.url -> webhook.urls");
                migrated = true;
            }
        } else if (!config.isSet("webhook.urls")) {
            // 如果 webhook.urls 和 webhook.url 都不存在，则设置默认值
            config.set("webhook.urls", Arrays.asList("https://example.com/webhook"));
            getLogger().info("设置默认 webhook.urls");
            migrated = true;
        }

        // 确保所有事件都有 enabled 和 message 字段
        String[] eventTypes = {"login", "quit", "death", "chat", "command"};
        for (String eventType : eventTypes) {
            String enabledPath = "events." + eventType + ".enabled";
            String messagePath = "events." + eventType + ".message";

            if (!config.isSet(enabledPath)) {
                config.set(enabledPath, true); // 默认启用
                getLogger().info("为事件 '" + eventType + "' 添加默认 'enabled' 设置 (true)");
                migrated = true;
            }
            if (!config.isSet(messagePath)) {
                String defaultMessage = "";
                switch (eventType) {
                    case "login":
                        defaultMessage = "%player% 登录了服务器";
                        break;
                    case "quit":
                        defaultMessage = "%player% 离开了服务器";
                        break;
                    case "death":
                        defaultMessage = "%player% 死亡了";
                        break;
                    case "chat":
                        defaultMessage = "%player% 说: %message%";
                        break;
                    case "command":
                        defaultMessage = "%player% 执行了指令: %command%";
                        break;
                }
                config.set(messagePath, defaultMessage);
                getLogger().info("为事件 '" + eventType + "' 添加默认 'message' 设置");
                migrated = true;
            }
        }
        
        // 更新配置版本号
        config.set("plugin-config-version", PLUGIN_CONFIG_VERSION);
        
        if (migrated) {
            getLogger().info("配置文件迁移完成。");
        } else {
            getLogger().info("配置文件已是最新或无需迁移。");
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MinecraftWebhook插件已禁用!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("webhook")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("minecraftwebhook.admin")) {
                    super.reloadConfig(); // Bukkit method to reload config from disk
                    FileConfiguration newConfig = super.getConfig();
                    migrateConfig(newConfig);
                    super.saveConfig(); 
                    configManager.reloadConfig(); // Tell our ConfigManager to re-read values
                    sender.sendMessage("§a[MinecraftWebhook] 配置已重新加载!");
                    getLogger().info("配置已通过 /webhook reload 命令重新加载。");
                    return true;
                } else {
                    sender.sendMessage("§c[MinecraftWebhook] 你没有权限执行此命令!");
                    return true;
                }
            } else {
                sender.sendMessage("§e[MinecraftWebhook] 用法: /webhook reload");
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取Webhook发送器
     */
    public WebhookSender getWebhookSender() {
        return webhookSender;
    }
    
    /**
     * 记录日志
     */
    public void log(Level level, String message) {
        if (configManager.isLoggingEnabled()) {
            Level configLevel = configManager.getLoggingLevel();
            if (level.intValue() >= configLevel.intValue()) {
                getLogger().log(level, message);
            }
        }
    }
}

