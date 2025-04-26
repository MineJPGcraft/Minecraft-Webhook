package com.minecraft.webhook;

import org.bukkit.scheduler.BukkitTask;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

/**
 * Webhook发送器类，负责将数据发送到配置的webhook地址
 */
public class WebhookSender {
    
    private final MinecraftWebhook plugin;
    private final ConfigManager configManager;
    
    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public WebhookSender(MinecraftWebhook plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    /**
     * 发送数据到webhook
     * @param data 要发送的数据
     */
    public void sendWebhook(Map<String, Object> data) {
        if (!configManager.isWebhookEnabled()) {
            plugin.log(Level.INFO, "Webhook功能已禁用，跳过发送");
            return;
        }
        
        String webhookUrl = configManager.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            plugin.log(Level.WARNING, "Webhook URL未配置，跳过发送");
            return;
        }
        
        // 异步发送，避免阻塞主线程
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            sendWebhookAsync(webhookUrl, data);
        });
    }
    
    /**
     * 异步发送webhook
     */
    private void sendWebhookAsync(String webhookUrl, Map<String, Object> data) {
        JSONObject jsonData = new JSONObject(data);
        String jsonString = jsonData.toString();
        
        int retryCount = configManager.getRetryCount();
        int retryDelay = configManager.getRetryDelay();
        int timeout = configManager.getTimeout();
        
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "MinecraftWebhook/1.0");
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                connection.setDoOutput(true);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode >= 200 && responseCode < 300) {
                    // 成功
                    if (configManager.logSuccess()) {
                        plugin.log(Level.INFO, "Webhook发送成功: " + webhookUrl + " (状态码: " + responseCode + ")");
                    }
                    return;
                } else {
                    // 失败但可能是临时问题，尝试重试
                    if (configManager.logFailure()) {
                        plugin.log(Level.WARNING, "Webhook发送失败: " + webhookUrl + " (状态码: " + responseCode + ")，尝试次数: " + (attempt + 1) + "/" + (retryCount + 1));
                    }
                    
                    if (attempt < retryCount) {
                        try {
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                if (configManager.logFailure()) {
                    plugin.log(Level.WARNING, "Webhook发送异常: " + e.getMessage() + "，尝试次数: " + (attempt + 1) + "/" + (retryCount + 1));
                }
                
                if (attempt < retryCount) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        
        // 所有重试都失败
        if (configManager.logFailure()) {
            plugin.log(Level.SEVERE, "Webhook发送失败，已达到最大重试次数: " + retryCount);
        }
    }
}
