@echo off

echo Running...
cd "%~dp0..\run\"
java -Xms3G -Xmx3G -jar -DIReallyKnowWhatIAmDoingISwear spigot-1.12.2.jar
echo "Exited."
cd "%~dp0"