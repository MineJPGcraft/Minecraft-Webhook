package com.minecraft.webhook;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import java.util.logging.Level;

/**
 * MinecraftWebhook插件主类
 */
public class MinecraftWebhook extends JavaPlugin implements CommandExecutor {
    
    private ConfigManager configManager;
    private WebhookSender webhookSender;
    private EventListener eventListener;
    
    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化Webhook发送器
        webhookSender = new WebhookSender(this);
        
        // 注册事件监听器
        eventListener = new EventListener(this);
        Bukkit.getPluginManager().registerEvents(eventListener, this);
        
        // 注册命令
        getCommand("webhook").setExecutor(this);
        
        // 输出启动信息
        getLogger().info("MinecraftWebhook插件已启用!");
        getLogger().info("监听玩家登录、离开、死亡事件并发送到Webhook");
        
        // 检查配置
        if (!configManager.isWebhookEnabled()) {
            getLogger().warning("Webhook功能当前已禁用，请在配置文件中启用");
        } else {
            getLogger().info("Webhook URL: " + configManager.getWebhookUrl());
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
                    reloadConfig();
                    configManager.reloadConfig();
                    sender.sendMessage("§a[MinecraftWebhook] 配置已重新加载!");
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
