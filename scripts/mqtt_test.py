#!/usr/bin/env python3
"""
MQTT Test Client per Smart Dive Controller
Sostituisce mosquitto_sub su Windows
"""

import paho.mqtt.client as mqtt
import json
import time
from datetime import datetime

class MQTTTestClient:
    def __init__(self, broker_host="localhost", broker_port=1883):
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.client = mqtt.Client(client_id="test_client_windows")
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_disconnect = self.on_disconnect
        
        print(f"🔗 MQTT Test Client per Smart Dive Controller")
        print(f"   Broker: {broker_host}:{broker_port}")
        print("=" * 60)
    
    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print(f"✅ Connesso al broker MQTT")
            
            # Subscribe a tutti i topic importanti
            topics = [
                "dive/+/sensors/data",      # Dati sensori
                "dive/+/alerts",            # Alert
                "dive/+/status/+",          # Status vari
                "dive/+/sensors/+",         # Sensori individuali
            ]
            
            for topic in topics:
                client.subscribe(topic)
                print(f"   📡 Subscribed: {topic}")
                
        else:
            print(f"❌ Errore connessione: {rc}")
    
    def on_message(self, client, userdata, msg):
        timestamp = datetime.now().strftime("%H:%M:%S")
        topic = msg.topic
        
        try:
            # Prova a parsare come JSON
            payload = json.loads(msg.payload.decode())
            payload_str = json.dumps(payload, indent=2)
        except:
            # Se non è JSON, mostra come stringa
            payload_str = msg.payload.decode()
        
        # Colora l'output in base al tipo di messaggio
        if "alerts" in topic:
            print(f"🚨 [{timestamp}] ALERT - {topic}")
        elif "sensors/data" in topic:
            print(f"📊 [{timestamp}] DATA - {topic}")
        elif "status" in topic:
            print(f"💡 [{timestamp}] STATUS - {topic}")
        else:
            print(f"📨 [{timestamp}] MSG - {topic}")
        
        print(f"   {payload_str}")
        print("-" * 60)
    
    def on_disconnect(self, client, userdata, rc):
        print(f"🔌 Disconnesso dal broker (rc: {rc})")
    
    def start_listening(self):
        try:
            print("🚀 Avvio listening MQTT...")
            self.client.connect(self.broker_host, self.broker_port, 60)
            self.client.loop_start()
            
            print("✅ Listening attivo! Premi Ctrl+C per fermare")
            print("💡 Avvia il simulatore Arduino in un altro terminale:")
            print("   python scripts\\arduino_simulator.py capo_vaticano shallow 10")
            print()
            
            # Loop infinito
            while True:
                time.sleep(1)
                
        except KeyboardInterrupt:
            print("\n🛑 Interruzione utente")
        except Exception as e:
            print(f"❌ Errore: {e}")
        finally:
            self.client.loop_stop()
            self.client.disconnect()
            print("✅ Client MQTT chiuso")
    
    def test_connection(self):
        """Test rapido di connessione"""
        print("🔍 Test connessione MQTT...")
        try:
            test_client = mqtt.Client(client_id="test_connection")
            test_client.connect(self.broker_host, self.broker_port, 10)
            test_client.disconnect()
            print("✅ Broker MQTT raggiungibile")
            return True
        except Exception as e:
            print(f"❌ Broker MQTT non raggiungibile: {e}")
            print("💡 Assicurati che Docker sia avviato:")
            print("   docker-compose up -d")
            return False

def main():
    import sys
    
    broker_host = sys.argv[1] if len(sys.argv) > 1 else "localhost"
    
    client = MQTTTestClient(broker_host=broker_host)
    
    # Test connessione prima
    if client.test_connection():
        print()
        client.start_listening()
    else:
        print("\n🛠️ Risolvi i problemi di connessione e riprova")

if __name__ == "__main__":
    main()