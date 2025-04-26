package com.minecraft.webhook;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * 事件监听器类，负责监听玩家事件并发送到Webhook
 */
public class EventListener implements Listener {
    
    private final MinecraftWebhook plugin;
    private final ConfigManager configManager;
    private final WebhookSender webhookSender;
    
    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public EventListener(MinecraftWebhook plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.webhookSender = plugin.getWebhookSender();
    }
    
    /**
     * 监听玩家登录事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (configManager.isEventEnabled("login")) {
            Player player = event.getPlayer();
            String message = configManager.getEventMessage("login").replace("%player%", player.getName());
            
            Map<String, Object> data = createBasePlayerData(player, "login");
            data.put("message", message);
            
            webhookSender.sendWebhook(data);
            plugin.log(Level.INFO, "玩家 " + player.getName() + " 登录事件已发送到Webhook");
        }
    }
    
    /**
     * 监听玩家离开事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (configManager.isEventEnabled("quit")) {
            Player player = event.getPlayer();
            String message = configManager.getEventMessage("quit").replace("%player%", player.getName());
            
            Map<String, Object> data = createBasePlayerData(player, "quit");
            data.put("message", message);
            
            webhookSender.sendWebhook(data);
            plugin.log(Level.INFO, "玩家 " + player.getName() + " 离开事件已发送到Webhook");
        }
    }
    
    /**
     * 监听玩家死亡事件
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (configManager.isEventEnabled("death")) {
            Player player = event.getEntity();
            String message = configManager.getEventMessage("death").replace("%player%", player.getName());
            
            Map<String, Object> data = createBasePlayerData(player, "death");
            data.put("message", message);
            data.put("death_message", event.getDeathMessage());
            
            webhookSender.sendWebhook(data);
            plugin.log(Level.INFO, "玩家 " + player.getName() + " 死亡事件已发送到Webhook");
        }
    }
    
    /**
     * 创建基础玩家数据
     */
    private Map<String, Object> createBasePlayerData(Player player, String eventType) {
        Map<String, Object> data = new HashMap<>();
        
        // 基本信息
        data.put("event_type", eventType);
        data.put("player_name", player.getName());
        data.put("player_uuid", player.getUniqueId().toString());
        
        // 时间信息
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        data.put("timestamp", now.format(formatter));
        
        // 位置信息
        Location location = player.getLocation();
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("world", location.getWorld().getName());
        locationData.put("x", location.getX());
        locationData.put("y", location.getY());
        locationData.put("z", location.getZ());
        locationData.put("yaw", location.getYaw());
        locationData.put("pitch", location.getPitch());
        data.put("location", locationData);
        
        // 服务器信息
        data.put("server_name", plugin.getServer().getName());
        data.put("server_version", plugin.getServer().getVersion());
        
        return data;
    }
}
