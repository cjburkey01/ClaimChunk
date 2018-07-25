@echo off

call "%~dp0build.bat" || (exit /b 1)
call "%~dp0run.bat"