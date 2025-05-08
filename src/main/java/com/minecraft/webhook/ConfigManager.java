package com.minecraft.webhook;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * 配置管理器类，负责处理插件配置
 */
public class ConfigManager {
    
    private final MinecraftWebhook plugin;
    private FileConfiguration config;
    
    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public ConfigManager(MinecraftWebhook plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    /**
     * 获取Webhook URL列表
     * 支持单个URL字符串或URL列表
     */
    public List<String> getWebhookUrls() {
        if (config.isList("webhook.urls")) {
            return config.getStringList("webhook.urls");
        } else if (config.isString("webhook.url")) {
            // For backward compatibility with old single webhook.url
            return Collections.singletonList(config.getString("webhook.url"));
        } else if (config.isString("webhook.urls")) {
            // Handle case where webhook.urls might be a single string by mistake
             return Collections.singletonList(config.getString("webhook.urls"));
        }
        // Default or if no valid URL config is found
        return Collections.singletonList("https://example.com/webhook"); 
    }
    
    /**
     * 检查Webhook功能是否启用
     */
    public boolean isWebhookEnabled() {
        return config.getBoolean("webhook.enabled", true);
    }
    
    /**
     * 获取连接超时时间
     */
    public int getTimeout() {
        return config.getInt("webhook.timeout", 5000);
    }
    
    /**
     * 获取重试次数
     */
    public int getRetryCount() {
        return config.getInt("webhook.retry-count", 3);
    }
    
    /**
     * 获取重试延迟
     */
    public int getRetryDelay() {
        return config.getInt("webhook.retry-delay", 2000);
    }
    
    /**
     * 检查指定事件是否启用
     */
    public boolean isEventEnabled(String eventType) {
        return config.getBoolean("events." + eventType + ".enabled", true);
    }
    
    /**
     * 获取事件消息模板
     */
    public String getEventMessage(String eventType) {
        return config.getString("events." + eventType + ".message", "%player% 触发了事件");
    }
    
    /**
     * 检查日志功能是否启用
     */
    public boolean isLoggingEnabled() {
        return config.getBoolean("logging.enabled", true);
    }
    
    /**
     * 获取日志级别
     */
    public Level getLoggingLevel() {
        String levelStr = config.getString("logging.level", "INFO");
        try {
            return Level.parse(levelStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的日志级别: " + levelStr + "，使用默认级别 INFO");
            return Level.INFO;
        }
    }
    
    /**
     * 检查是否记录成功的webhook调用
     */
    public boolean logSuccess() {
        return config.getBoolean("logging.log-success", true);
    }
    
    /**
     * 检查是否记录失败的webhook调用
     */
    public boolean logFailure() {
        return config.getBoolean("logging.log-failure", true);
    }
}

