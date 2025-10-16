# XU-News-AI-RAG Docker环境测试脚本 (PowerShell)
# 用途: 在VMware Docker环境中运行后端测试

param(
    [string]$TestClass = "",
    [switch]$Coverage = $false,
    [switch]$CopyReports = $true
)

$ErrorActionPreference = "Stop"

Write-Host "=========================================="  -ForegroundColor Cyan
Write-Host "XU-News-AI-RAG Docker 测试套件" -ForegroundColor Cyan
Write-Host "=========================================="  -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否运行
Write-Host "检查Docker环境..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
} catch {
    Write-Host "❌ Docker未运行或未安装！" -ForegroundColor Red
    exit 1
}

# 检查后端容器是否存在
$containerExists = docker ps -a --filter "name=xu-news-backend" --format "{{.Names}}"
if (-not $containerExists) {
    Write-Host "❌ 后端容器 'xu-news-backend' 不存在！" -ForegroundColor Red
    Write-Host "请先启动项目: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

# 检查容器是否运行
$containerRunning = docker ps --filter "name=xu-news-backend" --format "{{.Names}}"
if (-not $containerRunning) {
    Write-Host "⚠️  后端容器未运行，正在启动..." -ForegroundColor Yellow
    docker start xu-news-backend
    Start-Sleep -Seconds 5
}

Write-Host "✅ Docker环境检查通过" -ForegroundColor Green
Write-Host ""

# 构建测试命令
$testCommand = "cd /app && mvn clean test"

if ($TestClass) {
    Write-Host "运行指定测试: $TestClass" -ForegroundColor Cyan
    $testCommand += " -Dtest=$TestClass"
}

if ($Coverage) {
    Write-Host "生成代码覆盖率报告..." -ForegroundColor Cyan
    $testCommand += " jacoco:report"
}

Write-Host ""
Write-Host "=========================================="  -ForegroundColor Cyan
Write-Host "在Docker容器中运行测试..." -ForegroundColor Cyan
Write-Host "=========================================="  -ForegroundColor Cyan
Write-Host ""

# 运行测试
docker exec -it xu-news-backend sh -c $testCommand

$testResult = $LASTEXITCODE

Write-Host ""

if ($testResult -eq 0) {
    Write-Host "=========================================="  -ForegroundColor Green
    Write-Host "✅ 测试通过！" -ForegroundColor Green
    Write-Host "=========================================="  -ForegroundColor Green
    Write-Host ""
    
    if ($CopyReports) {
        Write-Host "正在复制测试报告到本地..." -ForegroundColor Yellow
        
        # 创建报告目录
        $reportDir = ".\test-reports"
        if (Test-Path $reportDir) {
            Remove-Item -Recurse -Force $reportDir
        }
        New-Item -ItemType Directory -Path $reportDir | Out-Null
        
        # 复制Surefire报告
        Write-Host "  - 复制Surefire报告..." -ForegroundColor Gray
        docker cp xu-news-backend:/app/target/surefire-reports "$reportDir\surefire" 2>$null
        
        if ($Coverage) {
            # 复制JaCoCo报告
            Write-Host "  - 复制JaCoCo覆盖率报告..." -ForegroundColor Gray
            docker cp xu-news-backend:/app/target/site/jacoco "$reportDir\jacoco" 2>$null
            
            if (Test-Path "$reportDir\jacoco\index.html") {
                Write-Host ""
                Write-Host "📊 代码覆盖率报告:" -ForegroundColor Cyan
                Write-Host "   $((Get-Location).Path)\test-reports\jacoco\index.html" -ForegroundColor White
                Write-Host ""
                Write-Host "是否在浏览器中打开报告? (Y/N): " -ForegroundColor Yellow -NoNewline
                $response = Read-Host
                if ($response -eq 'Y' -or $response -eq 'y') {
                    Start-Process "$reportDir\jacoco\index.html"
                }
            }
        }
        
        Write-Host ""
        Write-Host "✅ 报告已复制到: .\test-reports\" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "测试报告位置（容器内）:" -ForegroundColor Cyan
    Write-Host "  - Surefire: /app/target/surefire-reports/" -ForegroundColor White
    if ($Coverage) {
        Write-Host "  - JaCoCo: /app/target/site/jacoco/index.html" -ForegroundColor White
    }
} else {
    Write-Host "=========================================="  -ForegroundColor Red
    Write-Host "❌ 测试失败！" -ForegroundColor Red
    Write-Host "=========================================="  -ForegroundColor Red
    Write-Host ""
    Write-Host "查看详细错误信息:" -ForegroundColor Yellow
    Write-Host "  docker exec -it xu-news-backend cat /app/target/surefire-reports/*.txt" -ForegroundColor White
    Write-Host ""
    
    if ($CopyReports) {
        # 即使失败也复制报告
        docker cp xu-news-backend:/app/target/surefire-reports ./test-reports/surefire 2>$null
        Write-Host "失败报告已复制到: .\test-reports\surefire\" -ForegroundColor Yellow
    }
    
    exit 1
}

Write-Host ""
Write-Host "=========================================="  -ForegroundColor Cyan
Write-Host "测试完成！" -ForegroundColor Cyan
Write-Host "=========================================="  -ForegroundColor Cyan

