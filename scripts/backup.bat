@echo off
echo 🔄 Backup Smart Dive Controller
echo ===============================

set BACKUP_DIR=C:\SmartDiveController\backups\%date:~-4,4%-%date:~-10,2%-%date:~-7,2%

mkdir "%BACKUP_DIR%" 2>nul

echo Backup database InfluxDB...
docker exec dive_influxdb influx backup /tmp/backup
docker cp dive_influxdb:/tmp/backup "%BACKUP_DIR%\influxdb"

echo Backup configurazioni...
xcopy "C:\SmartDiveController\config" "%BACKUP_DIR%\config" /E /I /Y

echo ✅ Backup completato in: %BACKUP_DIR%
pause