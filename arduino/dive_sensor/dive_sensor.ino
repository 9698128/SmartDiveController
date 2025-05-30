/*
  Smart Dive Site Controller - Arduino ESP32
  Sensori per monitoraggio sito immersione subacqueo
  
  Hardware:
  - ESP32 DevKit V1
  - DS18B20 (Temperatura)
  - HMC5883L (Magnetometro per direzione corrente)
  - TSD-10 (Torbidità per visibilità)
  - BH1750 (Luminosità)
*/

#include <WiFi.h>
#include <PubSubClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Wire.h>
#include <HMC5883L.h>
#include <BH1750.h>
#include <ArduinoJson.h>
#include <esp_task_wdt.h>

// ========== CONFIGURAZIONE ==========
// WiFi
const char* ssid = "TUO_WIFI_SSID";          // MODIFICA CON IL TUO WIFI
const char* password = "oakdrive";   // MODIFICA CON LA TUA PASSWORD

// MQTT
const char* mqtt_server = "192.168.1.76";    // MODIFICA CON IP DEL TUO PC
const int mqtt_port = 1883;
const char* mqtt_client_id = "dive_sensor_esp32";

// Identificazione sito
const char* site_id = "capo_vaticano";
const char* sensor_id = "sensor_01";
const char* depth_category = "shallow";  // surface, shallow, deep

// Pin definitions
#define ONE_WIRE_BUS 4        // DS18B20 temperatura
#define TURBIDITY_PIN 34      // TSD-10 torbidità (ADC)
#define LED_STATUS 2          // LED interno ESP32
#define SDA_PIN 21            // I2C SDA
#define SCL_PIN 22            // I2C SCL

// Timing
#define SENSOR_INTERVAL 30000  // 30 secondi tra letture
#define WIFI_TIMEOUT 10000     // 10 secondi timeout WiFi
#define MQTT_TIMEOUT 5000      // 5 secondi timeout MQTT

// ========== INIZIALIZZAZIONE SENSORI ==========
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature temperatureSensor(&oneWire);
HMC5883L compass;
BH1750 lightMeter;

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

// ========== VARIABILI GLOBALI ==========
struct SensorData {
  float temperature;
  float current_speed;
  int current_direction;
  float visibility;
  float luminosity;
  float battery_level;
  unsigned long timestamp;
};

float battery_voltage = 3.7;  // Simulazione batteria
unsigned long last_sensor_read = 0;
int connection_attempts = 0;
bool sensors_initialized = false;

// ========== SETUP ==========
void setup() {
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("🚀 Smart Dive Site Controller - ESP32");
  Serial.println("=====================================");
  
  // Setup pin
  pinMode(LED_STATUS, OUTPUT);
  pinMode(TURBIDITY_PIN, INPUT);
  
  // Setup I2C
  Wire.begin(SDA_PIN, SCL_PIN);
  
  // Inizializza sensori
  initializeSensors();
  
  // Setup WiFi
  setupWiFi();
  
  // Setup MQTT
  mqttClient.setServer(mqtt_server, mqtt_port);
  mqttClient.setCallback(mqttCallback);
  
  // Setup watchdog
  esp_task_wdt_init(30, true);  // 30 secondi watchdog
  esp_task_wdt_add(NULL);
  
  Serial.println("✅ Setup completato");
  blinkLED(3, 200);  // 3 blink veloci = setup OK
}

// ========== LOOP PRINCIPALE ==========
void loop() {
  esp_task_wdt_reset();  // Reset watchdog
  
  // Mantieni connessioni attive
  if (!WiFi.isConnected()) {
    Serial.println("⚠️ WiFi disconnesso, riconnessione...");
    setupWiFi();
  }
  
  if (!mqttClient.connected()) {
    connectToMQTT();
  }
  
  mqttClient.loop();
  
  // Lettura sensori ogni SENSOR_INTERVAL
  if (millis() - last_sensor_read >= SENSOR_INTERVAL) {
    SensorData data = readAllSensors();
    publishSensorData(data);
    
    // Status LED
    digitalWrite(LED_STATUS, HIGH);
    delay(100);
    digitalWrite(LED_STATUS, LOW);
    
    last_sensor_read = millis();
  }
  
  delay(1000);  // 1 secondo tra cicli principali
}

// ========== INIZIALIZZAZIONE SENSORI ==========
void initializeSensors() {
  Serial.println("🔧 Inizializzazione sensori...");
  
  // DS18B20 Temperatura
  temperatureSensor.begin();
  int tempSensorCount = temperatureSensor.getDeviceCount();
  Serial.printf("   DS18B20: %d sensori trovati\n", tempSensorCount);
  
  // HMC5883L Magnetometro
  if (compass.begin()) {
    compass.setRange(HMC5883L_RANGE_1_3GA);
    compass.setMeasurementMode(HMC5883L_CONTINOUS);
    compass.setDataRate(HMC5883L_DATARATE_15HZ);
    compass.setSamples(HMC5883L_SAMPLES_8);
    Serial.println("   HMC5883L: OK");
  } else {
    Serial.println("   HMC5883L: ERRORE");
  }
  
  // BH1750 Luminosità
  if (lightMeter.begin()) {
    Serial.println("   BH1750: OK");
  } else {
    Serial.println("   BH1750: ERRORE");
  }
  
  // Test torbidità (ADC)
  int turbidityTest = analogRead(TURBIDITY_PIN);
  Serial.printf("   Torbidità ADC: %d (test)\n", turbidityTest);
  
  sensors_initialized = true;
  Serial.println("✅ Sensori inizializzati");
}

// ========== WIFI SETUP ==========
void setupWiFi() {
  Serial.printf("🔗 Connessione WiFi a: %s\n", ssid);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  
  unsigned long start_time = millis();
  while (WiFi.status() != WL_CONNECTED && 
         (millis() - start_time) < WIFI_TIMEOUT) {
    delay(500);
    Serial.print(".");
  }
  
  if (WiFi.isConnected()) {
    Serial.println();
    Serial.printf("✅ WiFi connesso - IP: %s\n", WiFi.localIP().toString().c_str());
    Serial.printf("   RSSI: %d dBm\n", WiFi.RSSI());
  } else {
    Serial.println("\n❌ Timeout connessione WiFi");
    // Riprova dopo delay
    delay(5000);
  }
}

// ========== MQTT CONNECTION ==========
void connectToMQTT() {
  if (mqttClient.connected()) return;
  
  Serial.printf("🔗 Connessione MQTT a: %s:%d\n", mqtt_server, mqtt_port);
  
  connection_attempts++;
  String client_id = String(mqtt_client_id) + "_" + String(random(0xffff), HEX);
  
  if (mqttClient.connect(client_id.c_str())) {
    Serial.println("✅ MQTT connesso");
    connection_attempts = 0;
    
    // Subscribe a topic di controllo
    String control_topic = "dive/" + String(site_id) + "/control";
    mqttClient.subscribe(control_topic.c_str());
    
    // Pubblica status online
    publishStatus("online");
    
  } else {
    Serial.printf("❌ MQTT fallito, rc=%d\n", mqttClient.state());
    
    if (connection_attempts > 5) {
      Serial.println("🔄 Troppi tentativi, riavvio WiFi...");
      WiFi.disconnect();
      delay(1000);
      setupWiFi();
      connection_attempts = 0;
    }
  }
}

// ========== CALLBACK MQTT ==========
void mqttCallback(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  
  Serial.printf("📨 MQTT ricevuto [%s]: %s\n", topic, message.c_str());
  
  // Parsing comandi
  if (message == "status") {
    publishStatus("online");
  } else if (message == "restart") {
    Serial.println("🔄 Riavvio richiesto via MQTT");
    ESP.restart();
  } else if (message == "deep_sleep") {
    Serial.println("😴 Deep sleep 5 minuti");
    esp_deep_sleep(5 * 60 * 1000000);  // 5 minuti in microsecondi
  }
}

// ========== LETTURA SENSORI ==========
SensorData readAllSensors() {
  SensorData data;
  data.timestamp = millis();
  
  Serial.println("📊 Lettura sensori...");
  
  // Temperatura DS18B20
  temperatureSensor.requestTemperatures();
  data.temperature = temperatureSensor.getTempCByIndex(0);
  if (data.temperature == DEVICE_DISCONNECTED_C) {
    data.temperature = -999.0;  // Valore errore
  }
  
  // Magnetometro per direzione corrente
  Vector norm = compass.readNormalize();
  float heading = atan2(norm.YAxis, norm.XAxis);
  if (heading < 0) heading += 2 * PI;
  data.current_direction = (int)(heading * 180/PI);
  
  // Simula velocità corrente basata su magnetometro + rumore
  float magnetic_strength = sqrt(norm.XAxis*norm.XAxis + norm.YAxis*norm.YAxis);
  data.current_speed = constrain(magnetic_strength * 0.5 + random(-10, 10)/100.0, 0, 3.0);
  
  // Torbidità → Visibilità
  int turbidity_raw = analogRead(TURBIDITY_PIN);
  float turbidity_voltage = (turbidity_raw / 4095.0) * 3.3;
  // Conversione: più torbidità = meno visibilità
  data.visibility = map(turbidity_raw, 0, 4095, 30, 1);  // 30m max, 1m min
  
  // Luminosità BH1750
  data.luminosity = lightMeter.readLightLevel();
  if (data.luminosity < 0) data.luminosity = 0;
  
  // Batteria (simulata con degradazione)
  battery_voltage -= 0.001;  // Degrado graduale
  if (battery_voltage < 3.0) battery_voltage = 3.7;  // Reset per test
  data.battery_level = map(battery_voltage * 100, 300, 370, 0, 100);
  
  // Log letture
  Serial.printf("   T: %.1f°C | C: %.1fm/s@%d° | V: %.1fm | L: %.0flux | B: %.1f%%\n",
                data.temperature, data.current_speed, data.current_direction,
                data.visibility, data.luminosity, data.battery_level);
  
  return data;
}

// ========== PUBBLICAZIONE DATI ==========
void publishSensorData(SensorData data) {
  if (!mqttClient.connected()) return;
  
  // JSON principale
  StaticJsonDocument<512> doc;
  doc["timestamp"] = WiFi.getTime();  // Timestamp Unix
  doc["site_id"] = site_id;
  doc["sensor_id"] = sensor_id;
  doc["depth"] = depth_category;
  doc["temperature"] = round(data.temperature * 100) / 100.0;
  doc["current_speed"] = round(data.current_speed * 100) / 100.0;
  doc["current_direction"] = data.current_direction;
  doc["visibility"] = round(data.visibility * 10) / 10.0;
  doc["luminosity"] = round(data.luminosity * 10) / 10.0;
  doc["battery_level"] = round(data.battery_level * 10) / 10.0;
  doc["rssi"] = WiFi.RSSI();
  
  String json_string;
  serializeJson(doc, json_string);
  
  // Pubblica su topic principale
  String main_topic = "dive/" + String(site_id) + "/sensors/data";
  mqttClient.publish(main_topic.c_str(), json_string.c_str());
  
  // Topic individuali per facilità parsing
  String base_topic = "dive/" + String(site_id) + "/sensors/";
  mqttClient.publish((base_topic + "temperature").c_str(), String(data.temperature).c_str());
  mqttClient.publish((base_topic + "current_speed").c_str(), String(data.current_speed).c_str());
  mqttClient.publish((base_topic + "current_direction").c_str(), String(data.current_direction).c_str());
  mqttClient.publish((base_topic + "visibility").c_str(), String(data.visibility).c_str());
  mqttClient.publish((base_topic + "luminosity").c_str(), String(data.luminosity).c_str());
  
  String status_topic = "dive/" + String(site_id) + "/status/";
  mqttClient.publish((status_topic + "battery").c_str(), String(data.battery_level).c_str());
  mqttClient.publish((status_topic + "rssi").c_str(), String(WiFi.RSSI()).c_str());
  
  // Check e pubblica alert
  checkAndPublishAlerts(data);
  
  Serial.println("📤 Dati pubblicati su MQTT");
}

// ========== SISTEMA ALERT ==========
void checkAndPublishAlerts(SensorData data) {
  String alert_topic = "dive/" + String(site_id) + "/alerts";
  
  // Temperatura critica
  if (data.temperature < 12.0 && data.temperature > -50) {
    StaticJsonDocument<200> alert;
    alert["type"] = "temperature";
    alert["level"] = "critical";
    alert["message"] = "Temperatura critica";
    alert["value"] = data.temperature;
    alert["threshold"] = 12.0;
    alert["timestamp"] = WiFi.getTime();
    
    String alert_json;
    serializeJson(alert, alert_json);
    mqttClient.publish(alert_topic.c_str(), alert_json.c_str());
  }
  
  // Corrente forte
  if (data.current_speed > 1.5) {
    StaticJsonDocument<200> alert;
    alert["type"] = "current";
    alert["level"] = "warning";
    alert["message"] = "Corrente forte";
    alert["value"] = data.current_speed;
    alert["threshold"] = 1.5;
    alert["timestamp"] = WiFi.getTime();
    
    String alert_json;
    serializeJson(alert, alert_json);
    mqttClient.publish(alert_topic.c_str(), alert_json.c_str());
  }
  
  // Visibilità scarsa
  if (data.visibility < 5.0) {
    StaticJsonDocument<200> alert;
    alert["type"] = "visibility";
    alert["level"] = "warning";
    alert["message"] = "Scarsa visibilità";
    alert["value"] = data.visibility;
    alert["threshold"] = 5.0;
    alert["timestamp"] = WiFi.getTime();
    
    String alert_json;
    serializeJson(alert, alert_json);
    mqttClient.publish(alert_topic.c_str(), alert_json.c_str());
  }
  
  // Batteria scarica
  if (data.battery_level < 20.0) {
    StaticJsonDocument<200> alert;
    alert["type"] = "battery";
    alert["level"] = data.battery_level < 10.0 ? "critical" : "warning";
    alert["message"] = "Batteria scarica";
    alert["value"] = data.battery_level;
    alert["threshold"] = 20.0;
    alert["timestamp"] = WiFi.getTime();
    
    String alert_json;
    serializeJson(alert, alert_json);
    mqttClient.publish(alert_topic.c_str(), alert_json.c_str());
  }
}

// ========== UTILITY ==========
void publishStatus(String status) {
  String status_topic = "dive/" + String(site_id) + "/status/online";
  mqttClient.publish(status_topic.c_str(), status.c_str());
}

void blinkLED(int times, int delay_ms) {
  for (int i = 0; i < times; i++) {
    digitalWrite(LED_STATUS, HIGH);
    delay(delay_ms);
    digitalWrite(LED_STATUS, LOW);
    delay(delay_ms);
  }
}
