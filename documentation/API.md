# 🌐 Smart Dive Controller - API Documentation

## 📡 **Base URL**
```
http://localhost:1880
http://YOUR_IP:1880
```

**Content-Type**: `application/json`  
**CORS**: Abilitato per tutti gli origins

---

## 🏖️ **Sites API**

### **GET /api/dive/sites**
Recupera tutti i siti di immersione monitorati.

#### **Response**
```json
[
  {
    "site_id": "capo_vaticano",
    "name": "Capo Vaticano",
    "latitude": 38.6878,
    "longitude": 15.8742,
    "depth_category": "shallow",
    "status": "online",
    "last_update": "2025-05-30T14:30:15.123Z"
  },
  {
    "site_id": "tropea_reef",
    "name": "Tropea Reef", 
    "latitude": 38.6767,
    "longitude": 15.8989,
    "depth_category": "deep",
    "status": "warning",
    "last_update": "2025-05-30T14:29:45.456Z"
  },
  {
    "site_id": "stromboli_east",
    "name": "Stromboli East",
    "latitude": 38.7891,
    "longitude": 15.2134,
    "depth_category": "surface",
    "status": "online",
    "last_update": "2025-05-30T14:30:00.789Z"
  }
]
```

#### **Status Values**
- `online` - Sensori funzionanti normalmente
- `warning` - Problemi minori (es. batteria bassa)
- `offline` - Sensori non raggiungibili
- `error` - Errori critici

#### **Depth Categories**
- `surface` - 0-5 metri
- `shallow` - 5-18 metri  
- `deep` - 18+ metri

---

## 🌊 **Current Conditions API**

### **GET /api/dive/sites/{siteId}/current**
Recupera le condizioni attuali per un sito specifico.

#### **Path Parameters**
- `siteId` (string) - ID del sito (es. "capo_vaticano")

#### **Response**
```json
{
  "timestamp": "2025-05-30T14:30:15.123Z",
  "site_id": "capo_vaticano",
  "sensor_id": "capo_vaticano_sensor_01",
  "depth": "shallow",
  "temperature": 19.2,
  "current_speed": 0.8,
  "current_direction": 47,
  "visibility": 22.5,
  "luminosity": 847.3,
  "battery_level": 78.4
}
```

#### **Field Descriptions**
| Campo | Tipo | Unità | Descrizione |
|-------|------|-------|-------------|
| `timestamp` | string | ISO 8601 | Momento della rilevazione |
| `site_id` | string | - | Identificativo sito |
| `sensor_id` | string | - | Identificativo sensore |
| `depth` | string | categoria | Categoria profondità |
| `temperature` | number | °C | Temperatura acqua |
| `current_speed` | number | m/s | Velocità corrente |
| `current_direction` | number | gradi | Direzione corrente (0-359°) |
| `visibility` | number | metri | Visibilità sott'acqua |
| `luminosity` | number | lux | Luminosità ambientale |
| `battery_level` | number | % | Livello batteria sensore |

#### **Example Requests**
```bash
# Capo Vaticano
curl http://localhost:1880/api/dive/sites/capo_vaticano/current

# Tropea Reef
curl http://localhost:1880/api/dive/sites/tropea_reef/current

# Stromboli East  
curl http://localhost:1880/api/dive/sites/stromboli_east/current
```

---

## 🚨 **Alerts API**

### **GET /api/dive/alerts/active**
Recupera tutti gli alert attivi nel sistema.

#### **Response**
```json
[
  {
    "type": "current",
    "level": "warning",
    "message": "Corrente forte rilevata",
    "value": 1.8,
    "threshold": 1.5,
    "site_id": "capo_vaticano",
    "timestamp": "2025-05-30T14:30:15.123Z"
  },
  {
    "type": "battery",
    "level": "critical", 
    "message": "Batteria sensore scarica",
    "value": 15,
    "threshold": 20,
    "site_id": "tropea_reef",
    "timestamp": "2025-05-30T14:25:30.456Z"
  }
]
```

#### **Alert Types**
- `temperature` - Temperatura fuori range
- `current` - Corrente troppo forte
- `visibility` - Visibilità scarsa
- `battery` - Batteria sensore bassa
- `connectivity` - Problemi connessione sensore

#### **Alert Levels**
- `info` - Informativo
- `warning` - Attenzione richiesta
- `critical` - Azione immediata necessaria

---

## 📊 **Historical Data API**

### **GET /api/dive/sites/{siteId}/history**
Recupera dati storici per un sito (implementazione futura).

#### **Query Parameters**
- `start` (string) - Data inizio (ISO 8601)
- `end` (string) - Data fine (ISO 8601)  
- `interval` (string) - Intervallo aggregazione (1m, 5m, 1h, 1d)
- `fields` (string) - Campi richiesti (comma-separated)

#### **Example**
```bash
curl "http://localhost:1880/api/dive/sites/capo_vaticano/history?start=2025-05-29T00:00:00Z&end=2025-05-30T00:00:00Z&interval=1h&fields=temperature,current_speed"
```

---

## 🔧 **System Status API**

### **GET /api/system/status**
Controlla stato generale del sistema (implementazione futura).

#### **Response**
```json
{
  "status": "healthy",
  "timestamp": "2025-05-30T14:30:15.123Z",
  "services": {
    "mqtt": "online",
    "influxdb": "online", 
    "node_red": "online"
  },
  "sensors_count": 3,
  "active_alerts": 2,
  "uptime": "2d 14h 30m"
}
```

---

## 🛠️ **MQTT Topics**

### **Incoming Sensor Data**
```
Topic: sensors/{site_id}/{sensor_id}/data
Payload: {
  "timestamp": "2025-05-30T14:30:15.123Z",
  "temperature": 19.2,
  "current_speed": 0.8,
  "current_direction": 47,
  "visibility": 22.5,
  "luminosity": 847.3,
  "battery_level": 78.4
}
```

### **Outgoing Alerts**
```
Topic: alerts/{site_id}
Payload: {
  "type": "current",
  "level": "warning",
  "message": "Corrente forte rilevata",
  "value": 1.8,
  "threshold": 1.5,
  "timestamp": "2025-05-30T14:30:15.123Z"
}
```

---

## 🧪 **Testing APIs**

### **cURL Examples**
```bash
# Test connettività
curl -I http://localhost:1880/api/dive/sites

# Test con verbose output
curl -v http://localhost:1880/api/dive/sites/capo_vaticano/current

# Test con timeout
curl --max-time 5 http://localhost:1880/api/dive/alerts/active
```

### **Postman Collection**
Importa la collection da `exports/SmartDiveController.postman_collection.json`:

1. **Postman → Import**
2. **Seleziona file** `exports/SmartDiveController.postman_collection.json`
3. **Test tutte le API** con un click

### **Python Testing Script**
```python
import requests
import json

def test_api():
    base_url = "http://localhost:1880"
    
    # Test sites
    response = requests.get(f"{base_url}/api/dive/sites")
    print(f"Sites: {response.status_code}")
    
    # Test current data
    sites = ["capo_vaticano", "tropea_reef", "stromboli_east"]
    for site in sites:
        response = requests.get(f"{base_url}/api/dive/sites/{site}/current")
        print(f"{site}: {response.status_code}")
    
    # Test alerts
    response = requests.get(f"{base_url}/api/dive/alerts/active")
    print(f"Alerts: {response.status_code}")

if __name__ == "__main__":
    test_api()
```

---

## 🔐 **Authentication (Future)**

### **Planned Security Features**
- **JWT Tokens** per authentication
- **API Keys** per rate limiting
- **HTTPS** per production
- **Role-based access** (admin, operator, viewer)

### **Future Headers**
```http
Authorization: Bearer <jwt_token>
X-API-Key: <api_key>
```

---

## 🚀 **Rate Limiting**

### **Current Limits**
- **No limits** in development
- **Best practice**: Max 1 request/second per endpoint

### **Future Limits**
- **Sites API**: 10 requests/minute
- **Current Data**: 60 requests/minute  
- **Alerts**: 30 requests/minute

---

## 📡 **WebSocket API (Future)**

### **Real-time Updates**
```javascript
// Connect to WebSocket
const ws = new WebSocket('ws://localhost:1880/api/ws');

// Subscribe to site updates
ws.send(JSON.stringify({
  type: 'subscribe',
  sites: ['capo_vaticano', 'tropea_reef']
}));

// Receive real-time data
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Real-time update:', data);
};
```

---

## 🔧 **Error Handling**

### **HTTP Status Codes**
- `200` - Success
- `400` - Bad Request (invalid site_id)
- `404` - Not Found (site not exists)
- `500` - Internal Server Error
- `503` - Service Unavailable (database down)

### **Error Response Format**
```json
{
  "error": {
    "code": "SITE_NOT_FOUND",
    "message": "Site 'invalid_site' not found",
    "timestamp": "2025-05-30T14:30:15.123Z"
  }
}
```

---

## 📝 **API Changelog**

### **v1.0.0 (Current)**
- ✅ Basic sites listing
- ✅ Current conditions data
- ✅ Active alerts
- ✅ CORS support

### **v1.1.0 (Planned)**
- 🔄 Historical data endpoints
- 🔄 System status API
- 🔄 WebSocket support
- 🔄 Authentication

### **v2.0.0 (Future)**
- 🔮 GraphQL endpoint
- 🔮 Real-time subscriptions
- 🔮 Advanced filtering
- 🔮 Bulk operations

---

## 🧑‍💻 **Development Notes**

### **Adding New Endpoints**
1. **Create HTTP input node** in Node-RED
2. **Add function node** for business logic
3. **Connect to HTTP response node**
4. **Update this documentation**
5. **Add to test collection**

### **Data Validation**
```javascript
// Example Node-RED validation
const siteId = msg.req.params.siteId;
const validSites = ['capo_vaticano', 'tropea_reef', 'stromboli_east'];

if (!validSites.includes(siteId)) {
    msg.statusCode = 404;
    msg.payload = {
        error: {
            code: 'SITE_NOT_FOUND',
            message: `Site '${siteId}' not found`
        }
    };
    return msg;
}
```

---

**🔗 Per ulteriori informazioni tecniche, vedi `TROUBLESHOOTING.md`**