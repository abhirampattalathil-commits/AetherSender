AetherSender (Sender App)

AetherSender is the sender application of the Smart Clock system. It runs on the primary device and transmits real-time data to the receiver device via Bluetooth.

---

✨ Core Features

1. Real-Time Data Transmission

- Sends battery status to the receiver device
- Transmits live system and device information
- Maintains continuous data sync

2. Music Metadata Sharing

- Sends song title and artist name
- Transfers album art (image) in real-time
- Keeps receiver display updated with current media

3. Media Control System

- Receives control commands from receiver
- Executes Play, Pause, Next, and Previous actions
- Syncs playback state between devices

4. Volume Synchronization

- Sends volume level updates to receiver
- Adjusts media volume based on receiver input
- Keeps both devices in sync

5. Force Connection System

- Automatically establishes Bluetooth connection
- Maintains stable communication channel
- Reconnects when connection is lost

---

🧠 Technical Highlights

- Language: Kotlin (Android SDK)
- Communication: Bluetooth RFCOMM (SPP) with UUID-based pairing
- Architecture: Client-side controller for data transmission
- Data Handling: Efficient encoding for image transfer (Base64)

---

📱 Role in System

AetherSender acts as the data source (sender) in the Smart Clock system.
It collects system and media data from the device and sends it to the receiver for display and interaction.

---

🚀 Future Improvements

- Wi-Fi based communication option
- Faster data compression for images
- Background optimization for battery efficiency
- Advanced media app integration
