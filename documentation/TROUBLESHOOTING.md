# 🔧 Smart Dive Controller - Troubleshooting Guide

## 🚨 **Problemi Comuni e Soluzioni**

---

## 🐳 **Problemi Docker**

### **❌ Problema: Container non si avviano**

#### **Sintomi:**
```
ERROR: Cannot start service dive_influxdb: driver failed programming external connectivity
ERROR: Port already in use
```

#### **Diagnosi:**
```powershell
# Controlla porte occupate
netstat -ano | findstr :1880
netstat -ano | findstr :8086
netstat -ano | findstr :1883

# Controlla container esistenti
docker ps -a
```

#### **Soluzioni:**
```powershell
# Opzione 1: Ferma processi che usano le porte
# Trova PID dalla colonna finale di netstat
taskkill /PID [PID_NUMBER] /F

# Opzione 2: Rimuovi container esistenti
docker-compose down --volumes
docker system prune -a

# Opzione 3: Cambia porte in docker-compose.yml
```

**docker-compose.yml modificato:**
```yaml
services:
  dive_nodered:
    ports:
      - "1881:1880"  # Porta diversa
  dive_influxdb:
    ports:
      - "8087:8086"  # Porta diversa
```

---

### **❌ Problema: Docker Desktop non avviato**

#### **Sintomi:**
```
Cannot connect to the Docker daemon at tcp://localhost:2375
```

#### **Soluzioni:**
1. **Avvia Docker Desktop** manualmente
2. **Verifica WSL2**:
   ```powershell
   wsl --status
   wsl --update
   ```
3. **Riavvia Docker**:
   ```powershell
   # In PowerShell come Amministratore
   Restart-Service docker
   ```

---

### **❌ Problema: Container lenti/instabili**

#### **Sintomi:**
- API timeout frequenti
- Container che si riavviano continuamente

#### **Diagnosi:**
```powershell
# Controlla risorse
docker stats

# Controlla log
docker-compose logs dive_influxdb
docker-compose logs dive_nodered
```

#### **Soluzioni:**
```powershell
# Aumenta memoria Docker Desktop
# Settings → Resources → Memory: 4GB+

# Riduci container simultanei
docker-compose up dive_nodered dive_influxdb
```

---

## 🌐 **Problemi Node-RED**

### **❌ Problema: Node-RED non caricabile**

#### **Sintomi:**
- http://localhost:1880 non risponde
- "This site can't be reached"

#### **Diagnosi:**
```powershell
# Verifica container
docker ps | findstr nodered

# Controlla log
docker logs dive_nodered
```

#### **Soluzioni:**
```powershell
# Riavvia container Node-RED
docker restart dive_nodered

# Se non funziona, ricostruisci
docker-compose down
docker-compose up -d dive_nodered
```

---

### **❌ Problema: Flows non importabili**

#### **Sintomi:**
- "Failed to import flows"
- JSON malformato
- Nodi mancanti

#### **Soluzioni:**
1. **Verifica JSON**:
   ```powershell
   # Valida con Python
   python -m json.tool exports/flows.json
   ```

2. **Import manuale**:
   - Apri `exports/flows.json`
   - Copia tutto il contenuto
   - Node-RED → Menu → Import → Clipboard

3. **Ricostruzione manuale**:
   - Vedi sezione "Ricostruzione Flows"

---

### **❌ Problema: API restituisce errori**

#### **Sintomi:**
```json
{"error": "Internal Server Error"}
curl: (7) Failed to connect to localhost port 1880
```

#### **Diagnosi:**
```powershell
# Test API base
curl http://localhost:1880

# Test specifico
curl -v http://localhost:1880/api/dive/sites
```

#### **Soluzioni:**
1. **Controlla Deploy**:
   - Node-RED → Deploy (pulsante rosso)
   - Verifica che non ci siano errori

2. **Verifica Debug**:
   - Node-RED → Debug tab
   - Cerca messaggi di errore

3. **Ricarica Flows**:
   ```powershell
   # Riavvia Node-RED
   docker restart dive_nodered
   ```

---

## 📡 **Problemi Connettività**

### **❌ Problema: App Android non si connette**

#### **Sintomi:**
- "Sistema: ERRORE CONNESSIONE"
- "Usando dati offline"
- Timeout nelle richieste

#### **Diagnosi Step-by-step:**

**1. Verifica IP PC:**
```powershell
ipconfig | findstr "IPv4"
# Output esempio: 192.168.1.100
```

**2. Test dal PC:**
```powershell
curl http://localhost:1880/api/dive/sites
# Deve restituire JSON
```

**3. Test dalla rete locale:**
```powershell
# Da altro PC/telefono nella stessa rete
curl http://192.168.1.100:1880/api/dive/sites
```

#### **Soluzioni:**

**1. Aggiorna IP nell'app:**
```kotlin
// NetworkModule.kt
private const val BASE_URL = "http://192.168.1.100:1880/"
```

**2. Configura Firewall:**
```powershell
# Esegui come Amministratore
netsh advfirewall firewall add rule name="Node-RED" dir=in action=allow protocol=TCP localport=1880
netsh advfirewall firewall add rule name="InfluxDB" dir=in action=allow protocol=TCP localport=8086
```

**3. Verifica antivirus:**
- Aggiungi eccezione per porte 1880, 8086
- Disabilita temporaneamente per test

**4. Test con browser mobile:**
- Apri browser su telefono
- Vai a http://TUO_IP:1880/api/dive/sites
- Se funziona, problema nell'app
- Se non funziona, problema di rete

---

### **❌ Problema: MQTT non funziona**

#### **Sintomi:**
- Simulatori non inviano dati
- Node-RED debug vuoto
- "MQTT disconnected"

#### **Diagnosi:**
```powershell
# Test MQTT diretto
python scripts/mqtt_test.py

# Controlla container
docker logs dive_mosquitto
```

#### **Soluzioni:**
```powershell
# Riavvia MQTT broker
docker restart dive_mosquitto

# Verifica configurazione
# File: config/mosquitto/mosquitto.conf
listener 1883
allow_anonymous true
```

---

## 📱 **Problemi Android App**

### **❌ Problema: App crash all'avvio**

#### **Sintomi:**
- App si chiude immediatamente
- "Unfortunately, DiveController has stopped"

#### **Diagnosi:**
```bash
# Controlla log Android
adb logcat | grep DiveController
adb logcat | grep "AndroidRuntime"