@echo off

echo Building ClaimChunk...
del /S /Q "%~dp0..\target\"
cd "%~dp0..\"
call mvn clean package || (echo Error: Maven not found. Maven is required to build ClaimChunk! && cd "%~dp0" && exit /b 1)
echo Copying...
if exist "%~dp0..\run\plugins\claimchunk-1.12.2_*.jar" del "%~dp0..\run\plugins\claimchunk-1.12.2_*.jar"
if not exist "%~dp0..\run\plugins\" mkdir "%~dp0..\run\plugins\"
copy /B "%~dp0..\target\claimchunk.jar" "%~dp0..\run\plugins\claimchunk-1.12.2_VERSION.jar"
cd "%~dp0"