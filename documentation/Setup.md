# 🚀 Smart Dive Controller - Guida Setup Completa

## 📋 **Requisiti di Sistema**

### **Software Richiesto:**
- **Windows 10/11** (64-bit)
- **Docker Desktop** 4.0+ con WSL2
- **Python 3.8+** 
- **Android Studio** 2022.3+ (Giraffe)
- **Git** (opzionale ma raccomandato)

### **Hardware Minimo:**
- **RAM**: 8GB (raccomandati 16GB)
- **Storage**: 5GB liberi
- **CPU**: 4 core (per container Docker)

### **Rete:**
- **Porte libere**: 1880, 8086, 1883, 3000
- **Connessione internet** per download dipendenze

---

## 🐳 **STEP 1: Setup Backend Docker**

### **1.1 Installa Docker Desktop**

```powershell
# Verifica installazione Docker
docker --version
docker-compose --version

# Se non installato, scarica da: https://desktop.docker.com/
```

### **1.2 Avvia Backend**

```powershell
# Clona/scarica il progetto
cd C:\
git clone [REPOSITORY_URL] SmartDiveController
# OPPURE estrai ZIP scaricato

cd SmartDiveController

# Avvia tutti i servizi
docker-compose up -d

# Verifica che tutti i container siano running
docker ps
```

**Output atteso:**
```
CONTAINER ID   IMAGE               STATUS
dive_nodered   nodered/node-red    Up
dive_influxdb  influxdb:2.0       Up  
dive_mosquitto eclipse-mosquitto   Up
dive_grafana   grafana/grafana     Up
```

### **1.3 Inizializza Database**

```powershell
# Installa dipendenze Python
pip install -r requirements.txt

# Inizializza InfluxDB
python scripts/database_init.py

# Output atteso: ✅ Database initialized successfully
```

---

## 🌐 **STEP 2: Configura Node-RED**

### **2.1 Accedi a Node-RED**

1. **Apri browser**: http://localhost:1880
2. **Dovresti vedere l'interfaccia Node-RED**

### **2.2 Importa Flows**

1. **Menu (☰) → Import**
2. **Select a file to import**
3. **Seleziona**: `exports/flows.json`
4. **Import**
5. **Deploy** (pulsante rosso)

### **2.3 Verifica API**

```powershell
# Test API endpoints
curl http://localhost:1880/api/dive/sites
curl http://localhost:1880/api/dive/sites/capo_vaticano/current
curl http://localhost:1880/api/dive/alerts/active
```

**Ogni comando dovrebbe restituire JSON valido.**

---

## 📱 **STEP 3: Setup App Android**

### **3.1 Installa Android Studio**

1. **Scarica**: https://developer.android.com/studio
2. **Installa Android SDK** (API 24-34)
3. **Configura emulatore** o connetti dispositivo fisico

### **3.2 Configura IP Address**

1. **Trova IP del tuo PC**:
```powershell
ipconfig | findstr "IPv4"
# Esempio output: 192.168.1.100
```

2. **Aggiorna NetworkModule.kt**:
```kotlin
// File: app/src/main/java/com/divecontroller/utils/NetworkModule.kt
private const val BASE_URL = "http://192.168.1.100:1880/"  // ⬅️ TUO IP
```

### **3.3 Compila e Avvia**

```powershell
cd android/DiveController

# Sync progetto
./gradlew build

# Installa su device/emulatore
./gradlew installDebug
```

**In Android Studio:**
1. **Open Project** → seleziona `android/DiveController`
2. **Sync** → **Clean** → **Rebuild**
3. **Run** (▶️)

---

## 🧪 **STEP 4: Test Sistema Completo**

### **4.1 Avvia Simulatore Dati**

```powershell
# Terminal 1: Simula sensore Capo Vaticano
python scripts/arduino_simulator.py capo_vaticano shallow 10

# Terminal 2: Simula sensore Tropea Reef  
python scripts/arduino_simulator.py tropea_reef deep 15

# Terminal 3: Simula sensore Stromboli East
python scripts/arduino_simulator.py stromboli_east surface 20
```

### **4.2 Verifica Flusso Dati**

1. **Node-RED Debug**: http://localhost:1880 → Debug tab
2. **InfluxDB**: Dovresti vedere messaggi di sensori
3. **API**: Le chiamate dovrebbero restituire dati aggiornati
4. **App Android**: Dashboard mostra dati reali che cambiano

### **4.3 Test Connettività Mobile**

**Dal telefono/emulatore:**
```
http://TUO_IP:1880/api/dive/sites
```

**Se non funziona, configura firewall:**
```powershell
# Esegui come Amministratore
netsh advfirewall firewall add rule name="Node-RED" dir=in action=allow protocol=TCP localport=1880
netsh advfirewall firewall add rule name="InfluxDB" dir=in action=allow protocol=TCP localport=8086
```

---

## 📊 **STEP 5: Setup Grafana (Opzionale)**

### **5.1 Accesso Grafana**

1. **URL**: http://localhost:3000
2. **Credenziali**: admin/admin (cambiale al primo accesso)

### **5.2 Configura Data Source**

1. **Configuration → Data Sources**
2. **Add InfluxDB**:
   - **URL**: http://dive_influxdb:8086
   - **Organization**: DivingCenter
   - **Token**: dive-monitoring-token-2024
   - **Bucket**: dive_data

### **5.3 Importa Dashboard**

1. **+ → Import**
2. **Upload JSON file**: `config/grafana/dashboard.json`
3. **Import**

---

## 🔧 **STEP 6: Configurazioni Avanzate**

### **6.1 Configurazione MQTT**

**File**: `config/mosquitto/mosquitto.conf`
```
listener 1883
allow_anonymous true
persistence true
persistence_location /mosquitto/data/
log_dest file /mosquitto/log/mosquitto.log
```

### **6.2 Configurazione InfluxDB**

**File**: `config/influxdb/influxdb.conf`
```
[http]
  enabled = true
  bind-address = ":8086"
  
[data]
  dir = "/var/lib/influxdb2"
```

### **6.3 Variabili Ambiente**

**File**: `.env`
```
# InfluxDB
INFLUXDB_ADMIN_TOKEN=dive-monitoring-token-2024
INFLUXDB_ORG=DivingCenter
INFLUXDB_BUCKET=dive_data

# MQTT
MQTT_BROKER=dive_mosquitto
MQTT_PORT=1883

# Grafana
GF_SECURITY_ADMIN_PASSWORD=dive_admin_2024
```

---

## 🎯 **STEP 7: Verifica Setup Completo**

### **7.1 Checklist Funzionalità**

- [ ] **Docker containers** tutti running
- [ ] **Node-RED** accessible su :1880
- [ ] **API endpoints** restituiscono JSON
- [ ] **Simulatori** generano dati
- [ ] **App Android** si connette e mostra dati
- [ ] **Dati cambiano** nel tempo
- [ ] **Alert system** funziona
- [ ] **Grafana** (opzionale) mostra grafici

## 🚨 **Troubleshooting Rapido**

### **Problema: Containers non si avviano**
```powershell
# Controlla log
docker-compose logs

# Riavvia servizi
docker-compose down
docker-compose up -d
```

### **Problema: App non si connette**
```powershell
# Verifica IP
ipconfig
# Aggiorna NetworkModule.kt
# Ricompila app
```

### **Problema: No data in InfluxDB**
```powershell
# Verifica MQTT
python scripts/mqtt_test.py
# Controlla Node-RED debug tab
```