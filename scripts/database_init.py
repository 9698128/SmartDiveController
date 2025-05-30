#!/usr/bin/env python3
"""
Smart Dive Site Controller - InfluxDB Fix & Reset Script
Risolve conflitti di tipo e resetta il database
"""

from influxdb_client import InfluxDBClient, Point, WritePrecision
from influxdb_client.client.write_api import SYNCHRONOUS
from influxdb_client.client.delete_api import DeleteApi
import json
from datetime import datetime, timedelta, timezone
import random
import math

# Configurazione InfluxDB
INFLUXDB_URL = "http://localhost:8086"
INFLUXDB_TOKEN = "dive-monitoring-token-2024"
INFLUXDB_ORG = "DivingCenter"
INFLUXDB_BUCKET = "dive_data"

class DiveSiteDBFixed:
    def __init__(self):
        self.client = InfluxDBClient(
            url=INFLUXDB_URL,
            token=INFLUXDB_TOKEN,
            org=INFLUXDB_ORG
        )
        self.write_api = self.client.write_api(write_options=SYNCHRONOUS)
        self.query_api = self.client.query_api()
        self.delete_api = self.client.delete_api()
    
    def reset_database(self):
        """Pulisce completamente il database per ripartire da zero"""
        print("🧹 Pulizia database...")
        
        try:
            # Cancella tutti i dati nel bucket
            start = "1970-01-01T00:00:00Z"
            stop = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
            
            self.delete_api.delete(
                start=start,
                stop=stop,
                predicate="",  # Cancella tutto
                bucket=INFLUXDB_BUCKET,
                org=INFLUXDB_ORG
            )
            
            print("✅ Database pulito con successo")
            return True
            
        except Exception as e:
            print(f"⚠️ Errore durante pulizia: {e}")
            print("💡 Questo è normale se il database era già vuoto")
            return True
    
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
                }
            }
        }
        
        print("📋 Schema definito:")
        print(json.dumps(schema_info, indent=2))
        return schema_info
    
    def insert_sample_data(self, hours_back=24):
        """Inserisce dati di esempio per testare il sistema (FIXED)"""
        print(f"📊 Inserimento dati di esempio per le ultime {hours_back} ore...")
        
        sites = ["capo_vaticano", "tropea_reef", "stromboli_east"]
        depths = ["surface", "shallow", "deep"]
        
        points = []
        # FIX: Usa timezone-aware datetime
        base_time = datetime.now(timezone.utc) - timedelta(hours=hours_back)
        
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
                    
                    # Batteria: degrado graduale (FIX: Assicura sempre float)
                    battery_level = 100.0 - (i * 0.01) + random.uniform(-1.0, 1.0)
                    battery_level = max(0.0, min(100.0, battery_level))
                    
                    # Crea point InfluxDB (FIX: Esplicita i tipi)
                    point = (Point("dive_conditions")
                           .tag("site_id", site)
                           .tag("sensor_id", f"{site}_sensor_01")
                           .tag("depth", depth)
                           .field("temperature", float(round(temperature, 2)))
                           .field("current_speed", float(round(max(0, current_speed), 2)))
                           .field("current_direction", int(current_direction))
                           .field("visibility", float(round(visibility, 1)))
                           .field("luminosity", float(round(max(0, luminosity), 1)))
                           .field("battery_level", float(round(battery_level, 1)))  # FIX: Esplicitamente float
                           .time(timestamp, WritePrecision.S))
                    
                    points.append(point)
        
        # Scrivi punti in batch più piccoli per evitare timeout
        batch_size = 100
        total_points = len(points)
        
        for i in range(0, total_points, batch_size):
            batch = points[i:i + batch_size]
            try:
                self.write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=batch)
                print(f"   ✅ Batch {i//batch_size + 1}/{(total_points + batch_size - 1)//batch_size} scritto ({len(batch)} punti)")
            except Exception as e:
                print(f"   ❌ Errore batch {i//batch_size + 1}: {e}")
                return False
        
        print(f"✅ Inseriti {total_points} punti dati nel database")
        return True
    
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
        
        try:
            result = self.query_api.query(org=INFLUXDB_ORG, query=query)
            
            print("📊 Condizioni attuali Capo Vaticano (shallow):")
            for table in result:
                for record in table.records:
                    print(f"   {record.get_field()}: {record.get_value()}")
            
            if not result or not any(table.records for table in result):
                print("   ⚠️ Nessun dato trovato (normale se appena inizializzato)")
            
        except Exception as e:
            print(f"   ❌ Errore query: {e}")
    
    def verify_database_health(self):
        """Verifica lo stato del database"""
        print("\n🏥 Verifica salute database...")
        
        try:
            # Test connessione
            health = self.client.health()
            print(f"   ✅ Connessione: {health.status}")
            
            # Conta record totali
            count_query = f'''
            from(bucket: "{INFLUXDB_BUCKET}")
              |> range(start: -30d)
              |> filter(fn: (r) => r["_measurement"] == "dive_conditions")
              |> count()
            '''
            
            result = self.query_api.query(org=INFLUXDB_ORG, query=count_query)
            total_records = 0
            for table in result:
                for record in table.records:
                    if record.get_field() == "_value":
                        total_records += record.get_value()
            
            print(f"   📊 Record totali: {total_records}")
            
            # Verifica tipi campi
            schema_query = f'''
            import "influxdata/influxdb/schema"
            schema.fieldKeys(bucket: "{INFLUXDB_BUCKET}")
            '''
            
            result = self.query_api.query(org=INFLUXDB_ORG, query=schema_query)
            print("   🔧 Campi nel database:")
            for table in result:
                for record in table.records:
                    print(f"      - {record.get_value()}")
            
        except Exception as e:
            print(f"   ❌ Errore verifica: {e}")
    
    def close(self):
        """Chiude la connessione"""
        self.client.close()

def main():
    """Funzione principale per resettare e inizializzare il database"""
    print("🚀 Smart Dive Site Controller - Database Fix & Reset")
    print("=" * 60)
    
    try:
        db = DiveSiteDBFixed()
        
        # Step 1: Reset database
        print("\n🎯 STEP 1: Reset Database")
        db.reset_database()
        
        # Step 2: Crea schema
        print("\n🎯 STEP 2: Crea Schema")
        db.create_schema()
        
        # Step 3: Inserisci dati di test
        print("\n🎯 STEP 3: Inserisci Dati Test")
        success = db.insert_sample_data(hours_back=24)  # Solo 24h per velocità
        
        if success:
            # Step 4: Test query
            print("\n🎯 STEP 4: Test Query")
            db.test_queries()
            
            # Step 5: Verifica salute
            print("\n🎯 STEP 5: Verifica Database")
            db.verify_database_health()
            
            print("\n✅ Database inizializzato con successo!")
            print("\n📋 Prossimi passi:")
            print("   1. Avvia Node-RED: http://localhost:1880")
            print("   2. Configura Grafana: http://localhost:3000")
            print("   3. Testa MQTT: localhost:1883")
            print("   4. Avvia simulatore: python scripts\\arduino_simulator.py")
        else:
            print("\n❌ Errore durante inserimento dati")
        
    except Exception as e:
        print(f"❌ Errore durante l'inizializzazione: {e}")
        print("💡 Verifica che Docker sia avviato e InfluxDB sia accessibile")
        print("💡 Prova: docker-compose restart influxdb")
    finally:
        if 'db' in locals():
            db.close()

if __name__ == "__main__":
    main()