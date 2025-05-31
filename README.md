#Smart Dive Controller

Sistema IoT completo per monitoraggio siti d'immersione subacquei.

#Architettura
Arduino/ESP32 → MQTT → Node-RED → InfluxDB → App Android

#Quick Start
1. `docker-compose up -d`
2. `python scripts/database_init.py`
3. Importa flows Node-RED da `exports/flows.json`
4. Apri app Android

#Struttura Progetto
- `/android/` - App Android (Jetpack Compose)
- `/scripts/` - Scripts Python (simulatori, test)
- `/config/` - Configurazioni Docker
- `/exports/` - Flows Node-RED e backup
- `/documentation/` - Guide dettagliate

#Requisiti
- Docker Desktop
- Python 3.8+
- Android Studio
- Windows 10/11

Vedi `documentation/SETUP.md` per guida completa.
