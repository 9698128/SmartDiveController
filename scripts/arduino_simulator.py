#!/usr/bin/env python3
"""
Smart Dive Site Controller - Arduino Simulator per Windows
Simula i dati di un sensore Arduino subacqueo realistico
"""

import paho.mqtt.client as mqtt
import json
import time
import random
import math
from datetime import datetime
import threading
import sys
import os

class DiveSensorSimulator:
    def __init__(self, site_id="capo_vaticano", sensor_id="sensor_01", mqtt_host="localhost", mqtt_port=1883):
        self.site_id = site_id
        self.sensor_id = sensor_id
        self.mqtt_host = mqtt_host
        self.mqtt_port = mqtt_port
        
        # Stato sensore
        self.depth = "shallow"
        self.battery_level = 100.0
        self.is_running = False
        
        # Pattern realistici
        self.base_temperature = 18.0
        self.tide_cycle = 0
        self.weather_pattern = random.choice(["calm", "stormy", "changing"])
        
        # Setup MQTT
        self.mqtt_client = mqtt.Client(client_id=f"dive_simulator_{sensor_id}")
        self.mqtt_client.on_connect = self.on_mqtt_connect
        self.mqtt_client.on_disconnect = self.on_mqtt_disconnect
        
        print(f"🤖 Inizializzazione simulatore Arduino")
        print(f"   Site: {site_id}")
        print(f"   Sensor: {sensor_id}")
        print(f"   Depth: {self.depth}")
        print(f"   Weather: {self.weather_pattern}")
    
    def on_mqtt_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print(f"✅ Connesso a MQTT broker {self.mqtt_host}:{self.mqtt_port}")
        else:
            print(f"❌ Errore connessione MQTT: {rc}")
    
    def on_mqtt_disconnect(self, client, userdata, rc):
        print(f"🔌 Disconnesso da MQTT broker")
    
    def connect_mqtt(self):
        """Connette al broker MQTT"""
        try:
            self.mqtt_client.connect(self.mqtt_host, self.mqtt_port, 60)
            self.mqtt_client.loop_start()
            return True
        except Exception as e:
            print(f"❌ Errore connessione MQTT: {e}")
            return False
    
    def simulate_temperature(self):
        """Simula lettura sensore temperatura DS18B20"""
        hour = datetime.now().hour
        
        # Ciclo giornaliero temperatura
        daily_variation = 3 * math.sin(2 * math.pi * (hour - 6) / 24)
        
        # Effetto profondità
        depth_effect = {"surface": 0, "shallow": -2, "deep": -5}[self.depth]
        
        # Effetto meteo
        weather_effect = {"calm": 0, "stormy": -1, "changing": random.uniform(-1, 1)}[self.weather_pattern]
        
        # Rumore sensore
        noise = random.uniform(-0.1, 0.1)
        
        temperature = self.base_temperature + daily_variation + depth_effect + weather_effect + noise
        return round(temperature, 2)
    
    def simulate_current(self):
        """Simula corrente marina"""
        # Corrente base influenzata da marea
        self.tide_cycle += 0.01
        tide_current = 0.3 * math.sin(self.tide_cycle)
        
        # Corrente influenzata dal meteo
        weather_current = {
            "calm": random.uniform(0, 0.2),
            "stormy": random.uniform(0.5, 1.8),
            "changing": random.uniform(0.1, 0.8)
        }[self.weather_pattern]
        
        # Corrente più forte in superficie
        depth_factor = {"surface": 1.0, "shallow": 0.7, "deep": 0.4}[self.depth]
        
        speed = abs(tide_current + weather_current) * depth_factor
        speed = max(0, min(3.0, speed))
        
        # Direzione corrente
        base_direction = 45
        tide_direction_change = 30 * math.sin(self.tide_cycle * 2)
        weather_direction_change = random.uniform(-20, 20) if self.weather_pattern == "stormy" else random.uniform(-5, 5)
        
        direction = (base_direction + tide_direction_change + weather_direction_change) % 360
        
        return round(speed, 2), int(direction)
    
    def simulate_visibility(self):
        """Simula visibilità"""
        hour = datetime.now().hour
        
        # Visibilità migliore durante il giorno
        daily_factor = 0.8 + 0.2 * max(0, math.sin(2 * math.pi * (hour - 6) / 24))
        
        # Visibilità base per profondità
        base_visibility = {"surface": 12, "shallow": 18, "deep": 25}[self.depth]
        
        # Effetto meteo
        weather_factor = {
            "calm": random.uniform(0.9, 1.1),
            "stormy": random.uniform(0.3, 0.7),
            "changing": random.uniform(0.6, 1.0)
        }[self.weather_pattern]
        
        visibility = base_visibility * daily_factor * weather_factor
        visibility = max(1.0, min(30.0, visibility))
        
        return round(visibility, 1)
    
    def simulate_luminosity(self):
        """Simula luminosità"""
        hour = datetime.now().hour
        
        # Luce solare
        solar_light = max(0, 1200 * math.sin(2 * math.pi * (hour - 6) / 24))
        
        # Attenuazione per profondità
        depth_attenuation = {
            "surface": 0.95,
            "shallow": 0.6,
            "deep": 0.1
        }[self.depth]
        
        # Effetto meteo
        weather_factor = {
            "calm": 1.0,
            "stormy": 0.3,
            "changing": random.uniform(0.5, 0.9)
        }[self.weather_pattern]
        
        luminosity = solar_light * depth_attenuation * weather_factor
        
        if self.depth == "deep":
            luminosity += random.uniform(0, 5)
        
        return round(max(0, luminosity), 1)
    
    def simulate_battery(self):
        """Simula livello batteria"""
        consumption_rate = 0.008
        
        if self.weather_pattern == "stormy":
            consumption_rate *= 1.5
        
        self.battery_level = max(0, self.battery_level - consumption_rate)
        return round(self.battery_level, 1)
    
    def read_all_sensors(self):
        """Simula lettura completa sensori"""
        timestamp = datetime.now().isoformat()
        
        temperature = self.simulate_temperature()
        current_speed, current_direction = self.simulate_current()
        visibility = self.simulate_visibility()
        luminosity = self.simulate_luminosity()
        battery = self.simulate_battery()
        
        sensor_data = {
            "timestamp": timestamp,
            "site_id": self.site_id,
            "sensor_id": self.sensor_id,
            "depth": self.depth,
            "temperature": temperature,
            "current_speed": current_speed,
            "current_direction": current_direction,
            "visibility": visibility,
            "luminosity": luminosity,
            "battery_level": battery,
            "weather_pattern": self.weather_pattern
        }
        
        return sensor_data
    
    def check_alerts(self, data):
        """Controlla alert"""
        alerts = []
        
        if data["temperature"] < 12:
            alerts.append({
                "type": "temperature",
                "level": "critical",
                "message": "Temperatura critica",
                "value": data["temperature"],
                "threshold": 12
            })
        
        if data["current_speed"] > 1.5:
            alerts.append({
                "type": "current",
                "level": "warning", 
                "message": "Corrente forte",
                "value": data["current_speed"],
                "threshold": 1.5
            })
        
        if data["visibility"] < 5:
            alerts.append({
                "type": "visibility",
                "level": "warning",
                "message": "Scarsa visibilità",
                "value": data["visibility"],
                "threshold": 5
            })
        
        if data["battery_level"] < 20:
            alerts.append({
                "type": "battery",
                "level": "critical" if data["battery_level"] < 10 else "warning",
                "message": "Batteria scarica",
                "value": data["battery_level"],
                "threshold": 20
            })
        
        return alerts
    
    def publish_data(self, data):
        """Pubblica dati su MQTT"""
        # Topic principale
        main_topic = f"dive/{self.site_id}/sensors/data"
        self.mqtt_client.publish(main_topic, json.dumps(data))
        
        # Topic separati
        topics = {
            f"dive/{self.site_id}/sensors/temperature": data["temperature"],
            f"dive/{self.site_id}/sensors/current": {
                "speed": data["current_speed"],
                "direction": data["current_direction"]
            },
            f"dive/{self.site_id}/sensors/visibility": data["visibility"],
            f"dive/{self.site_id}/sensors/luminosity": data["luminosity"],
            f"dive/{self.site_id}/status/battery": data["battery_level"]
        }
        
        for topic, payload in topics.items():
            self.mqtt_client.publish(topic, json.dumps(payload))
        
        # Pubblica alert
        alerts = self.check_alerts(data)
        if alerts:
            alert_topic = f"dive/{self.site_id}/alerts"
            for alert in alerts:
                alert["timestamp"] = data["timestamp"]
                alert["site_id"] = self.site_id
                self.mqtt_client.publish(alert_topic, json.dumps(alert))
    
    def run_simulation(self, interval=30):
        """Avvia simulazione"""
        if not self.connect_mqtt():
            return
        
        self.is_running = True
        print(f"\n🚀 Avvio simulazione (intervallo: {interval}s)")
        print("   Premi Ctrl+C per fermare\n")
        
        try:
            while self.is_running:
                sensor_data = self.read_all_sensors()
                self.publish_data(sensor_data)
                
                # Log dei dati
                print(f"📊 {sensor_data['timestamp'][:19]} | "
                      f"T:{sensor_data['temperature']:5.1f}°C | "
                      f"C:{sensor_data['current_speed']:4.1f}m/s@{sensor_data['current_direction']:3d}° | "
                      f"V:{sensor_data['visibility']:5.1f}m | "
                      f"L:{sensor_data['luminosity']:6.1f}lux | "
                      f"B:{sensor_data['battery_level']:5.1f}%")
                
                # Eventi casuali
                if random.random() < 0.1:
                    self.simulate_random_event()
                
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n🛑 Simulazione interrotta dall'utente")
        finally:
            self.stop_simulation()
    
    def simulate_random_event(self):
        """Eventi casuali"""
        events = [
            ("weather_change", "Cambio meteo"),
            ("depth_change", "Sensore spostato"),
            ("maintenance", "Manutenzione")
        ]
        
        event_type, description = random.choice(events)
        
        if event_type == "weather_change":
            old_weather = self.weather_pattern
            self.weather_pattern = random.choice(["calm", "stormy", "changing"])
            print(f"🌊 {description}: {old_weather} → {self.weather_pattern}")
        
        elif event_type == "depth_change":
            old_depth = self.depth
            self.depth = random.choice(["surface", "shallow", "deep"])
            print(f"📏 {description}: {old_depth} → {self.depth}")
        
        elif event_type == "maintenance":
            self.battery_level = 100.0
            print(f"🔧 {description}: Batteria ricaricata")
    
    def stop_simulation(self):
        """Ferma simulazione"""
        self.is_running = False
        self.mqtt_client.loop_stop()
        self.mqtt_client.disconnect()
        print("✅ Simulazione terminata")

def main():
    print("🤖 Smart Dive Site Controller - Arduino Simulator (Windows)")
    print("=" * 70)
    
    site_id = sys.argv[1] if len(sys.argv) > 1 else "capo_vaticano"
    depth = sys.argv[2] if len(sys.argv) > 2 else "shallow"
    interval = int(sys.argv[3]) if len(sys.argv) > 3 else 30
    
    simulator = DiveSensorSimulator(site_id=site_id)
    simulator.depth = depth
    simulator.run_simulation(interval=interval)

if __name__ == "__main__":
    main()