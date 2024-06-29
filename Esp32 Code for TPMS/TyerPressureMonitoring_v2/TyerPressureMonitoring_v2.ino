/*
 * VTPM_v0002
 * 
 * Code which changes byte in advertising packet which is intented to be a tyre pressure in future implementation.
 * Currently a random number is generated which is w. r. t. typre pressure values this value is shared in the advertising packet 

 * MAY 20, 2021, Currently BMP180 senor readig part added 
 * 
 * Based on RammaK ESP Example library from GitHub
 * Based on Neil Kolban's ESP32-BLE library at https://github.com/nkolban/ESP32_BLE_Arduino
*/

/*ESP32 BLE Libraries*/
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <Wire.h>
#include <Adafruit_BMP085.h>

#define TYERNUMBER  0x01

//Class declaration helps setting various advertising parameters
BLEAdvertisementData advert;
//Class to Intialise advertising mode in ESP32
BLEAdvertising *pAdvertising;

Adafruit_BMP085 bmp;

//Tyre Pressure value
uint8_t pressure_value;

//manufacturer code (0x02E5 for Espressif)
int man_code = 0x02E5;

//Srting used while manupalation in adv packet
String OutputStr;



//Function which gets Tyre pressure and returns the value
//Currently random number is generated and sent as tyre pressure
uint8_t get_tyre_pressure()
{
  long tyrepressurevalue;
  uint8_t Result;
//  tyrepressurevalue = random(24, 36);
  tyrepressurevalue = bmp.readPressure();
  Result = (uint8_t)(tyrepressurevalue/6894);

  Serial.println(); Serial.print("Actual Pressure Value: "); Serial.print(tyrepressurevalue);
    Serial.println(); Serial.print("Converted Pressure Value: "); Serial.print(Result);Serial.print(", "); Serial.print(Result,HEX);
    
//  Result = (uint8_t)tyrepressurevalue;
  return Result;
}

//Advertising packet contains a unique code as 0x4D505456 which is appended with input parameter passed
String AttachValue(char a)
{
  String Adv_str = "MPTV"; //0x4D505456
  Adv_str = Adv_str+TYERNUMBER;
  Adv_str.concat(a);
  return Adv_str;
}
//function takes String and adds manufacturer code at the beginning 
void setManData(String c, int c_size, BLEAdvertisementData &adv, int m_code) 
{
  
  String s;
  char b2 = (char)(m_code >> 8);
  m_code <<= 8;
  char b1 = (char)(m_code >> 8);
  s.concat(b1);
  s.concat(b2);
  s.concat(c);

  
  adv.setManufacturerData(s.c_str());
  
}

void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");

  if (!bmp.begin()) 
  {
    Serial.println("Could not find a valid BMP085/BMP180 sensor, check wiring!");
    while (1) {}
  }

  BLEDevice::init("VTPM_v0002");
  BLEServer *pServer = BLEDevice::createServer();

  pAdvertising = pServer->getAdvertising();
  advert.setName("VTPM_v0001");
  pAdvertising->setAdvertisementData(advert);
  pAdvertising->start();
}

void loop() 
{
   Serial.println("Sssss!");
  pressure_value = get_tyre_pressure();

  OutputStr  = AttachValue(pressure_value);  
  
  BLEAdvertisementData scan_response;
  
  setManData(OutputStr, OutputStr.length() , scan_response, man_code);
  Serial.println(OutputStr);
  pAdvertising->stop();
  pAdvertising->setScanResponseData(scan_response);
  pAdvertising->start();
    
  delay(2000);
}
