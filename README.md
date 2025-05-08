# MinecraftWebhook 插件 v1.1.0

这是一个 Minecraft Spigot 插件，用于将玩家事件（如登录、离开、死亡、聊天、指令调用）发送到配置文件中指定的一个或多个 webhook 地址。

## 功能特点

- **多种事件监听**: 监听玩家登录、离开、死亡、聊天消息和指令调用事件。
- **多 Webhook 支持**: 可以配置一个或多个 webhook 地址，事件将发送到所有配置的地址。
- **JSON 格式**: 将事件数据以 JSON 格式发送到配置的 webhook 地址。
- **详细数据**: 包含玩家名称、UUID、坐标、时间、服务器信息等。
- **高度可配置**: 
    - 全局 Webhook 开关。
    - 每个事件类型（登录、离开、死亡、聊天、指令）都有独立的启用/禁用开关。
    - 可自定义每个事件的消息模板。
    - 可配置连接超时、重试次数和重试延迟。
    - 可配置日志记录级别和成功/失败调用的日志开关。
- **配置自动迁移**: 从旧版本升级时，插件会自动迁移和补全配置文件，确保兼容性。
- **兼容性**: 主要针对 Minecraft 1.21 版本优化，并尽力保持对旧版本（如1.13+）的兼容性。

## 安装说明

1.  下载 `MinecraftWebhook-1.1.0.jar` 文件 (或对应版本)。
2.  将 JAR 文件放入服务器的 `plugins` 目录。
3.  重启服务器或使用插件管理器加载插件。
4.  插件首次加载时会自动生成默认配置文件 `plugins/MinecraftWebhook/config.yml`。如果已有旧版配置，插件会尝试自动迁移。
5.  编辑配置文件，设置您的 webhook 地址和其他选项。

## 配置说明 (v1.1.0)

插件首次加载时会生成默认配置文件。以下是配置选项说明：

```yaml
# 插件配置版本号，请勿手动修改
plugin-config-version: "1.1.0"

# Webhook设置
webhook:
  # Webhook URL列表，可以配置一个或多个URL
  # 例如: 
  # urls: 
  #   - "https://example.com/webhook1"
  #   - "https://example.com/webhook2"
  # 或者单个URL (也会被视为列表):
  # urls: ["https://example.com/webhook"]
  urls:
    - "https://example.com/webhook" # 请修改为您的Webhook地址
  # 是否启用Webhook功能 (全局开关)
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
    # 是否启用此事件的上报
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

  # 聊天事件
  chat:
    enabled: true
    # %player% - 玩家名, %message% - 聊天内容
    message: "%player% 说: %message%"

  # 指令事件
  command:
    enabled: true
    # %player% - 玩家名, %command% - 指令内容
    message: "%player% 执行了指令: %command%"

# 日志设置
logging:
  # 是否启用插件日志
  enabled: true
  # 日志级别：INFO, WARNING, SEVERE (或更详细的 Java Logging Levels)
  level: "INFO"
  # 是否记录成功的webhook调用
  log-success: true
  # 是否记录失败的webhook调用
  log-failure: true
```

**重要**: 如果您从旧版本升级，插件会自动尝试将 `webhook.url` (单URL字符串) 迁移到 `webhook.urls` (URL列表)，并为新增的 `chat` 和 `command` 事件添加默认配置。

## 命令

-   `/webhook reload` - 重新加载配置文件（需要 `minecraftwebhook.admin` 权限）

## 权限

-   `minecraftwebhook.admin` - 允许使用管理命令

## Webhook 数据格式

插件发送的 JSON 数据格式示例：

**登录/离开/死亡事件基础结构:**
```json
{
  "event_type": "login", // "login", "quit", "death"
  "player_name": "Steve",
  "player_uuid": "c8c58c9a-8bed-4a11-8b92-5b4a5f0f0000",
  "timestamp": "2025-05-06T15:30:00Z", // ISO 8601 格式
  "message": "Steve 登录了服务器", // 根据配置模板生成
  // 死亡事件特有字段:
  // "death_message": "Steve was slain by Zombie", 
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

**聊天事件:**
```json
{
  "event_type": "chat",
  "player_name": "Alex",
  "player_uuid": "d9e48b3a-9c1f-4a2e-8d76-6c5b6f1e1f1f",
  "timestamp": "2025-05-06T15:32:00Z",
  "message": "Alex 说: Hello everyone!", // 根据配置模板生成
  "chat_message": "Hello everyone!", // 原始聊天内容
  "location": { /* ... */ },
  "server_name": "My Minecraft Server",
  "server_version": "git-Spigot-1.21 (MC: 1.21)"
}
```

**指令事件:**
```json
{
  "event_type": "command",
  "player_name": "Herobrine",
  "player_uuid": "e0a3f5b7-1c8d-4b6e-9f0a-2d1c4e6f8a2b",
  "timestamp": "2025-05-06T15:35:00Z",
  "message": "Herobrine 执行了指令: /gamemode creative", // 根据配置模板生成
  "command_string": "/gamemode creative", // 原始指令内容 (包含斜杠)
  "location": { /* ... */ },
  "server_name": "My Minecraft Server",
  "server_version": "git-Spigot-1.21 (MC: 1.21)"
}
```

## 开发说明

如果您想修改或扩展此插件，可以使用提供的源代码。

### 构建要求

-   Java 11 或更高版本
-   Maven
-   Spigot API (1.21 或更高版本，代码中已适配1.13+的API)

### 构建步骤

1.  克隆或下载源代码。
2.  确保您的本地Maven环境可以访问Spigot API。如果您将Spigot JARs放在本地，可能需要调整`pom.xml`中的`<scope>system</scope>`和`<systemPath>`，或者安装到本地Maven仓库。
3.  运行 `mvn clean package`。
4.  编译后的插件将位于 `target` 目录中。

## 作者

WindyPear-Team

## 许可证

此插件使用 MIT 许可证。
