#!/bin/bash
# XU-News-AI-RAG n8n 工作流自动化配置脚本

set -e

echo "================================================"
echo "  XU-News-AI-RAG n8n 工作流配置工具"
echo "================================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 n8n 容器是否运行
check_n8n_running() {
    echo -e "${YELLOW}[1/6] 检查 n8n 服务状态...${NC}"
    
    if docker ps | grep -q "xu-news-n8n"; then
        echo -e "${GREEN}✓ n8n 服务正在运行${NC}"
        return 0
    else
        echo -e "${RED}✗ n8n 服务未运行${NC}"
        echo "请先启动 n8n: docker-compose up -d n8n"
        exit 1
    fi
}

# 检查 n8n 是否可访问
check_n8n_accessible() {
    echo -e "${YELLOW}[2/6] 检查 n8n 访问性...${NC}"
    
    max_retries=30
    retry_count=0
    
    while [ $retry_count -lt $max_retries ]; do
        if curl -s -o /dev/null -w "%{http_code}" http://localhost:5678 | grep -q "200\|401"; then
            echo -e "${GREEN}✓ n8n 服务可访问 (http://localhost:5678)${NC}"
            return 0
        fi
        
        retry_count=$((retry_count + 1))
        echo "等待 n8n 启动... ($retry_count/$max_retries)"
        sleep 2
    done
    
    echo -e "${RED}✗ n8n 服务无法访问${NC}"
    exit 1
}

# 检查必要的工作流文件
check_workflow_files() {
    echo -e "${YELLOW}[3/6] 检查工作流文件...${NC}"
    
    workflows=(
        "rss-news-collector.json"
        "webpage-scraper.json"
        "rag-qa-workflow.json"
        "document-processor.json"
        "email-notification.json"
    )
    
    missing_files=()
    
    for workflow in "${workflows[@]}"; do
        if [ -f "$workflow" ]; then
            echo -e "${GREEN}✓ 找到 $workflow${NC}"
        else
            echo -e "${RED}✗ 缺失 $workflow${NC}"
            missing_files+=("$workflow")
        fi
    done
    
    if [ ${#missing_files[@]} -gt 0 ]; then
        echo -e "${RED}错误: 缺少 ${#missing_files[@]} 个工作流文件${NC}"
        exit 1
    fi
}

# 显示导入说明
show_import_instructions() {
    echo -e "${YELLOW}[4/6] 工作流导入说明${NC}"
    echo ""
    echo "请按以下步骤手动导入工作流:"
    echo ""
    echo "1. 打开浏览器访问: ${GREEN}http://localhost:5678${NC}"
    echo "2. 使用以下凭据登录:"
    echo "   用户名: ${GREEN}admin${NC}"
    echo "   密码: ${GREEN}admin123${NC}"
    echo ""
    echo "3. 点击左侧菜单 'Workflows'"
    echo "4. 点击右上角 'Import from File'"
    echo "5. 依次导入以下工作流文件:"
    echo ""
    
    workflows=(
        "rss-news-collector.json      (RSS新闻采集)"
        "webpage-scraper.json         (网页抓取)"
        "rag-qa-workflow.json         (RAG智能问答)"
        "document-processor.json      (文档处理)"
        "email-notification.json      (邮件通知)"
    )
    
    for workflow in "${workflows[@]}"; do
        echo "   - $workflow"
    done
    
    echo ""
    read -p "按回车键继续配置凭据..." 
}

# 显示凭据配置说明
show_credentials_instructions() {
    echo -e "${YELLOW}[5/6] 凭据配置说明${NC}"
    echo ""
    echo "请在 n8n 中配置以下凭据:"
    echo ""
    
    echo "【MySQL 数据库凭据】"
    echo "  名称: MySQL - xu_news_rag"
    echo "  类型: MySQL"
    echo "  配置:"
    echo "    Host: ${GREEN}mysql${NC}"
    echo "    Port: ${GREEN}3306${NC}"
    echo "    Database: ${GREEN}xu_news_rag${NC}"
    echo "    User: ${GREEN}xu_news${NC}"
    echo "    Password: ${GREEN}xu_news_pass${NC}"
    echo ""
    
    echo "【SMTP 邮件凭据(可选)】"
    echo "  名称: SMTP - XU News"
    echo "  类型: SMTP"
    echo "  配置:"
    echo "    Host: ${YELLOW}smtp.example.com${NC} (修改为你的SMTP服务器)"
    echo "    Port: ${YELLOW}587${NC}"
    echo "    Secure: ${YELLOW}true${NC}"
    echo "    User: ${YELLOW}your_email@example.com${NC}"
    echo "    Password: ${YELLOW}your_password${NC}"
    echo ""
    
    read -p "按回车键继续激活工作流..."
}

# 显示工作流激活说明
show_activation_instructions() {
    echo -e "${YELLOW}[6/6] 工作流激活说明${NC}"
    echo ""
    echo "请激活以下工作流:"
    echo ""
    echo "1. RSS新闻采集工作流 (定时触发,每6小时)"
    echo "2. 网页抓取工作流 (Webhook触发)"
    echo "3. RAG智能问答工作流 (Webhook触发)"
    echo "4. 文档处理工作流 (Webhook触发)"
    echo "5. 邮件通知工作流 (Webhook触发)"
    echo ""
    echo "激活方法:"
    echo "  - 打开每个工作流"
    echo "  - 点击右上角 'Active' 开关"
    echo "  - Webhook工作流会自动生成URL"
    echo ""
}

# 配置 RSS 数据源
configure_rss_sources() {
    echo -e "${YELLOW}配置 RSS 数据源${NC}"
    echo ""
    echo "是否需要添加默认 RSS 订阅源? (y/n)"
    read -r response
    
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo "正在添加默认 RSS 源..."
        
        docker exec xu-news-mysql mysql -u xu_news -pxu_news_pass xu_news_rag << 'EOF'
INSERT IGNORE INTO data_source (name, type, url, status, created_at, updated_at) 
VALUES 
  ('TechCrunch', 'RSS', 'https://techcrunch.com/feed/', 1, NOW(), NOW()),
  ('The Verge', 'RSS', 'https://www.theverge.com/rss/index.xml', 1, NOW(), NOW()),
  ('Hacker News', 'RSS', 'https://news.ycombinator.com/rss', 1, NOW(), NOW());
EOF
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ RSS 数据源添加成功${NC}"
        else
            echo -e "${YELLOW}⚠ RSS 数据源添加失败,请手动添加${NC}"
        fi
    fi
}

# 显示完成信息
show_completion_message() {
    echo ""
    echo "================================================"
    echo -e "${GREEN}  n8n 工作流配置完成!${NC}"
    echo "================================================"
    echo ""
    echo "📋 下一步操作:"
    echo ""
    echo "1. 访问 n8n 管理界面: ${GREEN}http://localhost:5678${NC}"
    echo "2. 确认所有工作流已激活"
    echo "3. 测试 RSS 采集工作流"
    echo "4. 查看工作流执行历史"
    echo ""
    echo "📖 文档:"
    echo "  - 配置指南: ${GREEN}./README.md${NC}"
    echo "  - 技术文档: ${GREEN}../技术架构文档-XU-News-AI-RAG.md${NC}"
    echo ""
    echo "🔗 Webhook 端点:"
    echo "  - 网页抓取: http://localhost:5678/webhook/scrape-webpage"
    echo "  - RAG问答: http://localhost:5678/webhook/rag-query"
    echo "  - 文档处理: http://localhost:5678/webhook/process-document"
    echo "  - 邮件通知: http://localhost:5678/webhook/send-notification"
    echo ""
    echo "🐛 遇到问题? 查看日志:"
    echo "  ${GREEN}docker logs -f xu-news-n8n${NC}"
    echo ""
}

# 主流程
main() {
    check_n8n_running
    check_n8n_accessible
    check_workflow_files
    show_import_instructions
    show_credentials_instructions
    show_activation_instructions
    configure_rss_sources
    show_completion_message
}

# 执行主流程
main

