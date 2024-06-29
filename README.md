# TYRE_PRESSURE_MONITORING_2
Overview
The TYRE_PRESSURE_MONITORING project is a tire pressure monitoring system developed using ESP32 microcontroller boards and Android devices. This system aims to monitor and display real-time tire pressure information wirelessly.

# Hardware Requirements
ESP32 GSM Board: Used for collecting tire pressure data and broadcasting signals.
Battery: Power source for the ESP32 GSM board.
Air Pressure Sensor: Measures tire pressure.

# Project Structure
ESP32 Code for TPMS: Contains the firmware code for ESP32 boards responsible for collecting and broadcasting tire pressure data.

TYRE_PRESSURE_MONITORING_2: Includes the Android application code used to receive and display tire pressure information from ESP32 devices.
Operation

The TYRE_PRESSURE_MONITORING project operates on the ESP32 broadcasting concept, where ESP32 boards collect tire pressure data using air pressure sensors and broadcast this data. Android devices equipped with the TYRE_PRESSURE_MONITORING_2 app can receive and display this real-time information.

# License
Before using this project, please review the license provided

# Installation and Setup
ESP32 Setup:

Flash the ESP32 boards with the firmware from the ESP32 Code for TPMS folder.
Ensure the ESP32 boards are connected to the air pressure sensors and powered with batteries.
Android App Setup:

# Install the TYRE_PRESSURE_MONITORING_2 app on your Android device.
Ensure Bluetooth or other communication channels are enabled to receive broadcasts from ESP32 boards.
Usage
Turn on the ESP32 boards and ensure they are broadcasting tire pressure data.
Launch the TYRE_PRESSURE_MONITORING_2 app on your Android device to view real-time tire pressure information.

# Support
For any questions or support regarding the TYRE_PRESSURE_MONITORING project, please contact iotkudos@gmail.com.