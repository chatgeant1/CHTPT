@echo off
chcp 65001 > nul

echo Đang biên dịch code Java...
javac -encoding UTF-8 *.java
if errorlevel 1 (
    echo Biên dịch thất bại! 
    pause
    exit /b
)

echo Biên dịch thành công! Đang kích hoạt 6 Node...

:: Lệnh start sẽ bật một cửa sổ CMD mới độc lập cho mỗi dòng
start "Node 1 - Port 9001" java mainClass 1 9001
start "Node 2 - Port 9002" java mainClass 2 9002
start "Node 3 - Port 9003" java mainClass 3 9003
start "Node 4 - Port 9004" java mainClass 4 9004
start "Node 5 - Port 9005" java mainClass 5 9005
start "Node 6 - Port 9006" java mainClass 6 9006


pause