@echo off
REM #############################################################################
REM XU-News-AI-RAG Docker环境测试脚本 (Windows)
REM 
REM 此脚本在Docker环境下运行所有测试
REM
REM @author XU
REM @since 2025-10-17
REM #############################################################################

setlocal enabledelayedexpansion

REM 颜色定义（Windows CMD不支持颜色，使用文本标识）
set INFO=[信息]
set SUCCESS=[成功]
set WARNING=[警告]
set ERROR=[错误]

echo.
echo ==========================================
echo XU-News-AI-RAG Docker环境测试
echo ==========================================
echo.

REM 检查Docker是否安装
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo %ERROR% Docker未安装，请先安装Docker Desktop
    exit /b 1
)

where docker-compose >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo %ERROR% Docker Compose未安装
    exit /b 1
)

echo %SUCCESS% Docker环境检查通过
echo.

REM 解析参数
if "%1"=="" goto run_all
if "%1"=="all" goto run_all
if "%1"=="backend" goto run_backend
if "%1"=="frontend" goto run_frontend
if "%1"=="clean" goto clean
if "%1"=="help" goto show_help

echo %ERROR% 未知选项: %1
goto show_help

:run_all
echo ==========================================
echo 运行所有测试（Docker）
echo ==========================================
echo.

docker-compose -f docker-compose.test.yml up --abort-on-container-exit --exit-code-from backend-test

if %ERRORLEVEL% EQU 0 (
    echo.
    echo %SUCCESS% 所有测试通过
) else (
    echo.
    echo %ERROR% 测试失败，请检查上面的错误信息
    exit /b 1
)

goto summary

:run_backend
echo ==========================================
echo 运行后端测试（Docker）
echo ==========================================
echo.

docker-compose -f docker-compose.test.yml run --rm backend-test

if %ERRORLEVEL% EQU 0 (
    echo.
    echo %SUCCESS% 后端测试通过
    echo %INFO% 覆盖率报告: backend\target\site\jacoco\index.html
) else (
    echo.
    echo %ERROR% 后端测试失败
    exit /b 1
)

goto summary

:run_frontend
echo ==========================================
echo 运行前端测试（Docker）
echo ==========================================
echo.

docker-compose -f docker-compose.test.yml run --rm frontend-test

if %ERRORLEVEL% EQU 0 (
    echo.
    echo %SUCCESS% 前端测试通过
    echo %INFO% 覆盖率报告: frontend\coverage\index.html
) else (
    echo.
    echo %WARNING% 前端测试失败或未完全配置
)

goto summary

:clean
echo ==========================================
echo 清理测试环境
echo ==========================================
echo.

echo %INFO% 停止并删除测试容器...
docker-compose -f docker-compose.test.yml down -v

echo %INFO% 清理Maven缓存...
if exist backend\target rmdir /s /q backend\target

echo %INFO% 清理前端缓存...
if exist frontend\node_modules rmdir /s /q frontend\node_modules
if exist frontend\coverage rmdir /s /q frontend\coverage

echo.
echo %SUCCESS% 测试环境清理完成
goto end

:show_help
echo.
echo 使用方法: %~nx0 [选项]
echo.
echo 选项:
echo   all       运行所有测试（默认）
echo   backend   仅运行后端测试
echo   frontend  仅运行前端测试
echo   clean     清理测试容器和缓存
echo   help      显示此帮助信息
echo.
echo 示例:
echo   %~nx0              # 运行所有测试
echo   %~nx0 backend      # 仅运行后端测试
echo   %~nx0 clean        # 清理测试环境
echo.
goto end

:summary
echo.
echo ==========================================
echo 测试执行摘要
echo ==========================================
echo.
echo 测试报告位置：
echo   后端覆盖率报告: backend\target\site\jacoco\index.html
echo   前端覆盖率报告: frontend\coverage\index.html
echo.
echo 相关文档：
echo   Docker测试指南: DOCKER_TEST_GUIDE.md
echo   完整测试指南: TESTING_COMPLETE_GUIDE.md
echo   后端测试指南: backend\TEST_GUIDE.md
echo   前端测试指南: frontend\tests\README.md
echo.
echo 提示：
echo   在宿主机运行测试更快: run-all-tests.bat
echo   清理测试环境: %~nx0 clean
echo   查看帮助: %~nx0 help
echo.
echo %SUCCESS% 测试执行完成！
echo.

:end
endlocal

