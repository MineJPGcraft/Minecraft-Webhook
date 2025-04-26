# MinecraftWebhook 插件

这是一个 Minecraft Spigot 插件，用于将玩家事件（如登录、离开、死亡）发送到配置文件中指定的 webhook 地址。

## 功能特点

- 监听玩家登录、离开和死亡事件
- 将事件数据以 JSON 格式发送到配置的 webhook 地址
- 包含玩家名称、UUID、坐标、时间等信息
- 支持配置文件中的各种选项，如启用/禁用功能、重试机制、日志记录等
- 兼容 Minecraft 1.21

## 安装说明

1. 下载 `MinecraftWebhook-1.21.jar` 文件
2. 将 JAR 文件放入服务器的 `plugins` 目录
3. 重启服务器或使用插件管理器加载插件
4. 编辑生成的配置文件 `plugins/MinecraftWebhook/config.yml`

## 配置说明

插件首次加载时会生成默认配置文件。以下是配置选项说明：

```yaml
# Webhook设置
webhook:
  # Webhook URL，必须设置为有效的URL
  url: "https://example.com/webhook"
  # 是否启用Webhook功能
  enabled: true
  # 连接超时时间（毫秒）
  timeout: 5000
  # 失败重试次数
  retry-count: 3
  # 重试延迟（毫秒）
  retry-delay: 2000

# 事件设置
events:
  # 登录事件
  login:
    # 是否启用
    enabled: true
    # 消息模板，%player%会被替换为玩家名称
    message: "%player% 登录了服务器"
  
  # 离开事件
  quit:
    enabled: true
    message: "%player% 离开了服务器"
  
  # 死亡事件
  death:
    enabled: true
    message: "%player% 死亡了"

# 日志设置
logging:
  # 是否启用日志
  enabled: true
  # 日志级别：INFO, WARNING, SEVERE
  level: "INFO"
  # 是否记录成功的webhook调用
  log-success: true
  # 是否记录失败的webhook调用
  log-failure: true
```

## 命令

- `/webhook reload` - 重新加载配置文件（需要 `minecraftwebhook.admin` 权限）

## 权限

- `minecraftwebhook.admin` - 允许使用管理命令

## Webhook 数据格式

插件发送的 JSON 数据格式示例：

```json
{
  "event_type": "login",
  "player_name": "Steve",
  "player_uuid": "c8c58c9a-8bed-4a11-8b92-5b4a5f0f0000",
  "timestamp": "2025-04-26T14:30:00",
  "message": "Steve 登录了服务器",
  "location": {
    "world": "world",
    "x": 100.5,
    "y": 64.0,
    "z": -200.5,
    "yaw": 90.0,
    "pitch": 0.0
  },
  "server_name": "My Minecraft Server",
  "server_version": "git-Spigot-1.21 (MC: 1.21)"
}
```

## 开发说明

如果您想修改或扩展此插件，可以使用提供的源代码。

### 构建要求

- Java 11 或更高版本
- Maven
- Spigot API (1.21 或更高版本)

### 构建步骤

1. 克隆或下载源代码
2. 运行 `mvn clean package`
3. 编译后的插件将位于 `target` 目录中

## 许可证

此插件使用 MIT 许可证。
