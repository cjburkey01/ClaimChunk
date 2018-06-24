del /S /Q "%~dp0..\target\"
cd "%~dp0..\"
call mvn clean package
if exist "%~dp0..\run\plugins\claimchunk-1.12.2_*.jar" del "%~dp0..\run\plugins\claimchunk-1.12.2_*.jar"
if not exist "%~dp0..\run\plugins\" mkdir "%~dp0..\run\plugins\"
copy /B "%~dp0..\target\claimchunk.jar" "%~dp0..\run\plugins\claimchunk-1.12.2_VERSION.jar"