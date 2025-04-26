# MinecraftWebhook 插件

这是一个 Minecraft Spigot 插件，用于将玩家事件（如登录、离开、死亡）发送到配置文件中指定的 webhook 地址。

## 功能特点

- 监听玩家登录、离开、死亡事件
- 将事件数据以 JSON 格式发送到指定的 webhook 地址
- 包含玩家名称、UUID、坐标、时间等信息
- 支持配置文件中的各种选项，如启用/禁用功能、重试机制、日志记录等
- 支持 Minecraft 1.21 及其他版本

## 安装方法

1. 下载源代码或克隆此仓库
2. 使用 Maven 编译插件：`mvn clean package`
3. 将生成的 JAR 文件（位于 `target` 目录）复制到您的 Minecraft 服务器的 `plugins` 目录
4. 重启服务器或使用插件管理器加载插件

## 配置文件

插件首次运行时会生成一个默认的配置文件 `config.yml`，您可以根据需要修改它：

```yaml
# 配置文件
# 插件版本: 1.0

# Webhook设置
webhook:
  # Webhook URL地址
  url: "https://example.com/webhook"
  # 是否启用插件
  enabled: true
  # 连接超时时间(毫秒)
  timeout: 5000
  # 重试次数
  retry-count: 3
  # 重试间隔(毫秒)
  retry-delay: 2000

# 事件设置
events:
  # 玩家登录事件
  login:
    enabled: true
    # 自定义消息模板
    message: "%player% 登录了服务器"
  
  # 玩家离开事件
  quit:
    enabled: true
    message: "%player% 离开了服务器"
  
  # 玩家死亡事件
  death:
    enabled: true
    message: "%player% 死亡了"

# 日志设置
logging:
  # 是否启用日志
  enabled: true
  # 日志级别: INFO, WARNING, ERROR, DEBUG
  level: "INFO"
  # 是否记录成功的webhook调用
  log-success: true
  # 是否记录失败的webhook调用
  log-failure: true
```

## 命令

插件提供以下命令：

- `/webhook reload` - 重新加载配置文件（需要 `minecraftwebhook.admin` 权限）

## 权限

- `minecraftwebhook.admin` - 允许使用管理命令（如重新加载配置）

## Webhook 数据格式

插件发送的 JSON 数据格式示例：

```json
{
  "event_type": "login",
  "player_name": "Steve",
  "player_uuid": "c8c58c9a-8a23-4d40-a1fc-b2c1a1d0a7a3",
  "timestamp": "2025-04-26T12:34:56",
  "location": {
    "world": "world",
    "x": 100.5,
    "y": 64.0,
    "z": -200.3,
    "yaw": 90.0,
    "pitch": 0.0
  },
  "server_name": "My Minecraft Server",
  "server_version": "1.21",
  "message": "Steve 登录了服务器"
}
```

## 编译要求

- Java 11 或更高版本
- Maven 3.6 或更高版本
- Spigot API 1.13 或更高版本

## 故障排除

如果您遇到问题：

1. 确保您的 webhook URL 是正确的并且可以访问
2. 检查服务器日志中是否有错误消息
3. 确保您的服务器版本与插件兼容
4. 尝试使用 `/webhook reload` 重新加载配置

## 许可证

此插件采用 MIT 许可证。
