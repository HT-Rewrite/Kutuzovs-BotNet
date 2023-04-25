@echo off
echo PingBypass is now currently installing...
java -jar .\files\installer.jar
echo Installed following locations...
cd files
dir /b *.loc
echo PingBypass has been successfully installed!
pause