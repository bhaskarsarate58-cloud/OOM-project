
@echo off
cd /d "%~dp0"
title CineVerse

set "MAVEN=%USERPROFILE%\.copilot\session-state\5881b2d2-67eb-4592-a76e-24b26e2f4dce\files\maven-3.9.11\apache-maven-3.9.11\bin\mvn.cmd"
set "SETTINGS=%USERPROFILE%\.copilot\session-state\f3e562bd-3556-4d0a-bcf1-e22780bd5544\files\maven-settings-proxy.xml"

if not exist "%MAVEN%" (
  echo Maven file nahi mili.
  pause
  exit /b 1
)

if not exist "%SETTINGS%" (
  echo Maven settings file nahi mili.
  pause
  exit /b 1
)

"%MAVEN%" -s "%SETTINGS%" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
pause
