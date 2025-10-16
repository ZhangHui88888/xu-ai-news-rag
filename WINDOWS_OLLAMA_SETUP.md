# Windows Ollama 配置指南（用于 Docker 访问）

## ⚡ 快速配置（每次启动 Ollama 前执行）

### PowerShell 管理员模式

```powershell
# 1. 停止所有 Ollama 进程
Get-Process | Where-Object {$_.Name -like "*ollama*"} | Stop-Process -Force
Start-Sleep -Seconds 3

# 2. 设置环境变量
$env:OLLAMA_HOST="0.0.0.0:11434"

# 3. 启动 Ollama
ollama serve

# 保持窗口运行
```

---

## 🔥 防火墙配置（一次性，管理员模式）

```powershell
# 添加防火墙规则（允许所有入站连接）
New-NetFirewallRule -DisplayName "Ollama-AllowAll" `
  -Direction Inbound `
  -Protocol TCP `
  -LocalPort 11434 `
  -Action Allow `
  -Profile Any

# 验证
Get-NetFirewallRule -DisplayName "Ollama-AllowAll"
```

---

## ✅ 验证配置

### 在新 PowerShell 窗口：

```powershell
# 测试访问
curl http://localhost:11434/api/version
curl http://192.168.171.1:11434/api/version

# 检查监听（应该显示 0.0.0.0:11434）
netstat -ano | findstr "11434"
```

**预期输出：**
```
TCP    0.0.0.0:11434          0.0.0.0:0              LISTENING       XXXXX
```

---

## 🚀 测试从 Docker 访问

### 在 Linux 服务器上：

```bash
# 设置 no_proxy
export no_proxy="localhost,127.0.0.1,::1,172.18.0.0/16,192.168.0.0/16"

# 测试导入
timeout 15 curl -X POST http://localhost:8080/api/knowledge/import \
  -H "Content-Type: application/json" \
  -d '{"title":"测试","content":"测试","tags":[],"contentType":"article"}' \
  | grep vectorId
```

---

## 📋 故障排查

### 问题：防火墙规则不生效

```powershell
# 查看所有 Ollama 相关规则
Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*Ollama*"}

# 删除所有旧规则
Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*Ollama*"} | Remove-NetFirewallRule

# 重新添加
New-NetFirewallRule -DisplayName "Ollama-AllowAll" `
  -Direction Inbound `
  -Protocol TCP `
  -LocalPort 11434 `
  -Action Allow `
  -Profile Any
```

### 问题：端口被占用

```powershell
# 查找并结束进程
$pid = (netstat -ano | findstr "11434" | Select-Object -First 1) -replace '.*\s+(\d+)$','$1'
taskkill /PID $pid /F
```

---

## 💡 永久配置

### 创建启动脚本

保存为 `start_ollama.ps1`：

```powershell
# start_ollama.ps1
$env:OLLAMA_HOST="0.0.0.0:11434"
ollama serve
```

每次启动只需运行：
```powershell
.\start_ollama.ps1
```

---

## ✅ 配置成功标志

- `netstat` 显示 `0.0.0.0:11434`
- 能从 `http://192.168.171.1:11434` 访问
- Linux 服务器能导入数据（返回 vectorId）
- 查询能检索到文档
- 有重排日志

---

**配置完成后，重新连接服务器测试！**

