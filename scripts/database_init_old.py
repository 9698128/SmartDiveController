#!/usr/bin/env python3
"""
Smart Dive Site Controller - Database Initialization per Windows
Crea la struttura database InfluxDB per il monitoraggio siti immersione
"""

from influxdb_client import InfluxDBClient, Point, WritePrecision
from influxdb_client.client.write_api import SYNCHRONOUS
import json
from datetime import datetime, timedelta
import random
import math

# Configurazione InfluxDB
INFLUXDB_URL = "http://localhost:8086"
INFLUXDB_TOKEN = "dive-monitoring-token-2024"
INFLUXDB_ORG = "DivingCenter"
INFLUXDB_BUCKET = "dive_data"

class DiveSiteDB:
    def __init__(self):
        self.client = InfluxDBClient(
            url=INFLUXDB_URL,
            token=INFLUXDB_TOKEN,
            org=INFLUXDB_ORG
        )
        self.write_api = self.client.write_api(write_options=SYNCHRONOUS)
        self.query_api = self.client.query_api()
    
    def create_schema(self):
        """Crea la struttura del database per i dati di immersione"""
        print("🏗️  Inizializzazione schema database...")
        
        schema_info = {
            "measurements": {
                "dive_conditions": {
                    "fields": {
                        "temperature": "float (°C)",
                        "current_speed": "float (m/s)", 
                        "current_direction": "int (degrees 0-360)",
                        "visibility": "float (meters)",
                        "luminosity": "float (lux)",
                        "battery_level": "float (percentage)"
                    },
                    "tags": {
                        "site_id": "string (location identifier)",
                        "sensor_id": "string (device identifier)",
                        "depth": "string (depth category: surface/shallow/deep)"
                    }
                },
                "system_alerts": {
                    "fields": {
                        "alert_level": "string (info/warning/critical)",
                        "message": "string",
                        "value": "float (trigger value)",
                        "threshold": "float (threshold exceeded)"
                    },
                    "tags": {
                        "site_id": "string",
                        "alert_type": "string (temperature/current/visibility/battery)"
                    }
                }
            }
        }
        
        print("📋 Schema definito:")
        print(json.dumps(schema_info, indent=2))
        return schema_info
    
    def insert_sample_data(self, hours_back=24):
        """Inserisce dati di esempio per testare il sistema"""
        print(f"📊 Inserimento dati di esempio per le ultime {hours_back} ore...")
        
        sites = ["capo_vaticano", "tropea_reef", "stromboli_east"]
        depths = ["surface", "shallow", "deep"]
        
        points = []
        base_time = datetime.utcnow() - timedelta(hours=hours_back)
        
        for i in range(hours_back * 6):  # Un dato ogni 10 minuti
            timestamp = base_time + timedelta(minutes=i*10)
            
            for site in sites:
                for depth in depths:
                    # Simula dati realistici basati su profondità e ora
                    hour = timestamp.hour
                    depth_factor = {"surface": 0, "shallow": 0.3, "deep": 0.6}[depth]
                    
                    # Temperatura: più fredda in profondità e di notte
                    base_temp = 18 + 4 * math.sin(2 * math.pi * hour / 24)
                    temperature = base_temp - depth_factor * 5 + random.uniform(-1, 1)
                    
                    # Corrente: più forte in superficie, pattern semi-random
                    current_speed = (0.5 + 0.3 * (1 - depth_factor) + 
                                   0.2 * math.sin(2 * math.pi * i / 144)) + random.uniform(-0.1, 0.1)
                    current_direction = (45 + 30 * math.sin(2 * math.pi * i / 72) + 
                                       random.uniform(-15, 15)) % 360
                    
                    # Visibilità: meglio in profondità durante il giorno
                    base_visibility = 15 + 5 * math.sin(2 * math.pi * hour / 24)
                    visibility = base_visibility + depth_factor * 3 + random.uniform(-2, 2)
                    visibility = max(1, min(30, visibility))
                    
                    # Luminosità: zero in profondità di notte
                    if depth == "deep":
                        luminosity = max(0, 10 * math.sin(2 * math.pi * hour / 24))
                    else:
                        luminosity = max(0, 1000 * math.sin(2 * math.pi * hour / 24) * 
                                       (1 - depth_factor * 0.8))
                    
                    # Batteria: degrado graduale
                    battery_level = 100 - (i * 0.01) + random.uniform(-1, 1)
                    battery_level = max(0, min(100, battery_level))
                    
                    # Crea point InfluxDB
                    point = (Point("dive_conditions")
                           .tag("site_id", site)
                           .tag("sensor_id", f"{site}_sensor_01")
                           .tag("depth", depth)
                           .field("temperature", round(temperature, 2))
                           .field("current_speed", round(max(0, current_speed), 2))
                           .field("current_direction", int(current_direction))
                           .field("visibility", round(visibility, 1))
                           .field("luminosity", round(max(0, luminosity), 1))
                           .field("battery_level", round(battery_level, 1))
                           .time(timestamp, WritePrecision.S))
                    
                    points.append(point)
        
        # Scrivi tutti i punti in batch
        self.write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=points)
        print(f"✅ Inseriti {len(points)} punti dati nel database")
    
    def test_queries(self):
        """Testa alcune query di esempio"""
        print("\n🔍 Test query database...")
        
        # Query condizioni attuali
        query = f'''
        from(bucket: "{INFLUXDB_BUCKET}")
          |> range(start: -1h)
          |> filter(fn: (r) => r["_measurement"] == "dive_conditions")
          |> filter(fn: (r) => r["site_id"] == "capo_vaticano")
          |> filter(fn: (r) => r["depth"] == "shallow")
          |> last()
        '''
        
        result = self.query_api.query(org=INFLUXDB_ORG, query=query)
        
        print("📊 Condizioni attuali Capo Vaticano (shallow):")
        for table in result:
            for record in table.records:
                print(f"   {record.get_field()}: {record.get_value()}")
    
    def close(self):
        """Chiude la connessione"""
        self.client.close()

def main():
    """Funzione principale per inizializzare il database"""
    print("🚀 Smart Dive Site Controller - Database Setup (Windows)")
    print("=" * 60)
    
    try:
        db = DiveSiteDB()
        
        # Crea schema e inserisci dati di test
        db.create_schema()
        db.insert_sample_data(hours_back=48)  # 2 giorni di dati
        db.test_queries()
        
        print("\n✅ Database inizializzato con successo!")
        print("\n📋 Prossimi passi:")
        print("   1. Avvia Node-RED: http://localhost:1880")
        print("   2. Configura Grafana: http://localhost:3000")
        print("   3. Testa MQTT: localhost:1883")
        
    except Exception as e:
        print(f"❌ Errore durante l'inizializzazione: {e}")
        print("💡 Assicurati che Docker sia avviato e InfluxDB sia accessibile")
    finally:
        if 'db' in locals():
            db.close()

if __name__ == "__main__":
    main()