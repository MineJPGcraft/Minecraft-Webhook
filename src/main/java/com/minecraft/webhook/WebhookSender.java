package com.minecraft.webhook;

import org.bukkit.scheduler.BukkitTask;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List; // Added for List<String>
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
     * 发送数据到所有配置的webhook地址
     * @param data 要发送的数据
     */
    public void sendWebhook(Map<String, Object> data) {
        if (!configManager.isWebhookEnabled()) {
            plugin.log(Level.INFO, "Webhook功能已禁用，跳过发送");
            return;
        }
        
        List<String> webhookUrls = configManager.getWebhookUrls(); // Get list of URLs
        if (webhookUrls == null || webhookUrls.isEmpty()) {
            plugin.log(Level.WARNING, "Webhook URL列表未配置或为空，跳过发送");
            return;
        }
        
        // 异步发送到每个URL，避免阻塞主线程
        for (String webhookUrl : webhookUrls) {
            if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("https://example.com/webhook")) {
                plugin.log(Level.INFO, "跳过无效或默认的Webhook URL: " + webhookUrl);
                continue;
            }
            final String currentUrl = webhookUrl; // Final variable for lambda
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                sendWebhookAsync(currentUrl, data);
            });
        }
    }
    
    /**
     * 异步发送webhook到单个URL
     */
    private void sendWebhookAsync(String webhookUrl, Map<String, Object> data) {
        JSONObject jsonData = new JSONObject(data);
        String jsonString = jsonData.toString();
        
        int retryCount = configManager.getRetryCount();
        int retryDelay = configManager.getRetryDelay();
        int timeout = configManager.getTimeout();
        
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(webhookUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("User-Agent", "MinecraftWebhook/" + plugin.getDescription().getVersion());
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
                    return; // Exit loop on success for this URL
                } else {
                    // 失败但可能是临时问题，尝试重试
                    if (configManager.logFailure()) {
                        plugin.log(Level.WARNING, "Webhook发送失败: " + webhookUrl + " (状态码: " + responseCode + ")，尝试次数: " + (attempt + 1) + "/" + (retryCount + 1));
                    }
                    
                    if (attempt < retryCount) {
                        try {
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException e) {
                            plugin.log(Level.WARNING, "Webhook发送线程被中断: " + webhookUrl);
                            Thread.currentThread().interrupt();
                            return; // Exit if interrupted
                        }
                    }
                }
            } catch (IOException e) {
                if (configManager.logFailure()) {
                    plugin.log(Level.WARNING, "Webhook发送异常到 " + webhookUrl + ": " + e.getMessage() + "，尝试次数: " + (attempt + 1) + "/" + (retryCount + 1));
                }
                
                if (attempt < retryCount) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        plugin.log(Level.WARNING, "Webhook发送线程被中断: " + webhookUrl);
                        Thread.currentThread().interrupt();
                        return; // Exit if interrupted
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        
        // 所有重试都失败
        if (configManager.logFailure()) {
            plugin.log(Level.SEVERE, "Webhook发送失败，已达到最大重试次数 (" + retryCount + ") 到URL: " + webhookUrl);
        }
    }
}

