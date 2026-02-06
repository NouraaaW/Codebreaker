# Codebreaker - Multiplayer Network Game 🎮

## 🚀 Project Overview
**Codebreaker** is a networked multiplayer desktop game where players compete to solve visual puzzles and break codes. The system is built on a robust **Client-Server architecture** using Java Sockets, allowing multiple clients to connect, interact, and compete in real-time.

## 🔑 Key Features
* **Client-Server Communication:** Implements `ServerSocket` and `Socket` for low-latency TCP communication between the host and players.
* **Concurrency & Multithreading:** Utilizes multi-threading (`Runnable`) to handle multiple client connections and game rooms simultaneously without blocking.
* **Lobby System:** Features a "Waiting Room" mechanism that groups players before starting a match.
* **Real-time Synchronization:** Broadcasts game state, live scores, and winner announcements to all connected clients instantly.
* **Interactive GUI:** Built with **Java Swing**, featuring custom graphics rendering and event handling for a smooth user experience.

## 🛠 Tech Stack
* **Language:** Java.
* **Networking:** Java Sockets (TCP/IP).
* **GUI:** Java Swing (JFrame, Graphics2D).
* **Concepts:** Multithreading, Synchronization, Object-Oriented Design.
