@echo off
chcp 65001 > nul

javac -encoding UTF-8 *.java
if errorlevel 1 (
    echo Compile failed!
    pause
    exit /b
)

java mainClass 4 9004

pause