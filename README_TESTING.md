# 测试快速指南

## 🎯 快速开始

### 在Windows (VMware Docker环境) 中运行测试

```powershell
# PowerShell - 运行所有测试
.\test-in-docker.ps1

# 运行测试并生成覆盖率报告
.\test-in-docker.ps1 -Coverage

# 运行特定测试类
.\test-in-docker.ps1 -TestClass "JwtUtilTest"

# 不复制报告到本地
.\test-in-docker.ps1 -Coverage -CopyReports:$false
```

### 在Linux/Mac中运行测试

```bash
# 赋予执行权限
chmod +x test-in-docker.sh

# 运行所有测试
./test-in-docker.sh

# 运行测试并生成覆盖率报告
./test-in-docker.sh --coverage

# 运行特定测试类
./test-in-docker.sh --test "JwtUtilTest"
```

## 📚 详细文档

- **Docker环境测试**: [backend/DOCKER_TEST_GUIDE.md](backend/DOCKER_TEST_GUIDE.md)
- **完整测试报告**: [backend/TEST_REPORT.md](backend/TEST_REPORT.md)
- **测试指南**: [测试指南.md](测试指南.md)

## 📊 测试覆盖

本项目包含完整的测试套件：

- ✅ **10+ 测试类**
- ✅ **100+ 测试用例**
- ✅ **单元测试** - 工具类、Service层
- ✅ **集成测试** - Mapper层、数据库
- ✅ **API测试** - Controller层REST接口
- ✅ **端到端测试** - 完整业务流程

## 🎯 测试覆盖率目标

根据PRD要求：
- **核心功能测试覆盖率**: > 80%
- **API测试通过率**: > 95%

## 🐛 遇到问题？

1. 查看 [DOCKER_TEST_GUIDE.md](backend/DOCKER_TEST_GUIDE.md) 的常见问题部分
2. 检查Docker容器状态: `docker ps`
3. 查看容器日志: `docker logs xu-news-backend`

---

**项目**: XU-News-AI-RAG  
**环境**: VMware Docker  
**更新**: 2025-10-16

