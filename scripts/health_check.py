#!/usr/bin/env python3
"""
Health Check per Smart Dive Controller
"""

import requests
import subprocess
import json
from datetime import datetime

def check_service(name, url):
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            print(f"✅ {name}: OK")
            return True
        else:
            print(f"❌ {name}: HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ {name}: {e}")
        return False

def check_docker_services():
    try:
        result = subprocess.run(['docker', 'ps'], capture_output=True, text=True)
        if 'dive_influxdb' in result.stdout and 'dive_mosquitto' in result.stdout:
            print("✅ Docker services: OK")
            return True
        else:
            print("❌ Docker services: NOK")
            return False
    except Exception as e:
        print(f"❌ Docker: {e}")
        return False

def main():
    print("🔍 Smart Dive Controller - Health Check")
    print("=" * 50)
    print(f"Timestamp: {datetime.now()}")
    print()
    
    services = [
        ("Docker Services", None),
        ("InfluxDB", "http://localhost:8086/health"),
        ("Node-RED", "http://localhost:1880"),
        ("Grafana", "http://localhost:3000/api/health"),
    ]
    
    all_ok = True
    
    # Check Docker first
    if not check_docker_services():
        all_ok = False
    
    # Check other services
    for name, url in services[1:]:
        if not check_service(name, url):
            all_ok = False
    
    print()
    if all_ok:
        print("🎉 Sistema: TUTTO OK")
    else:
        print("⚠️ Sistema: PROBLEMI RILEVATI")

if __name__ == "__main__":
    main()