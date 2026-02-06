/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package codebreaker;

/**
 *
 * @author Norah
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> connectedClients;
    private List<ClientHandler> waitingPlayers;
    private List<GameRoom> gameRooms;
    private Map<ClientHandler, String> playerNames;
    private Timer waitingRoomTimer;
    private final int PORT = 5555;
    private final int MAX_PLAYERS_PER_ROOM = 5;
    private final int WAITING_ROOM_TIMER = 10; // seconds

    public GameServer() {
        connectedClients = new ArrayList<>();
        waitingPlayers = new ArrayList<>();
        gameRooms = new ArrayList<>();
        playerNames = new HashMap<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                connectedClients.add(clientHandler);
                new Thread(clientHandler).start();
                broadcastConnectedPlayers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

      public synchronized void addPlayer(ClientHandler client, String name) {
        if (!connectedClients.contains(client)) {
            connectedClients.add(client);
        }
        playerNames.put(client, name);
        broadcastConnectedPlayers();
        if (!waitingPlayers.isEmpty()) {
            broadcastWaitingRoomStatus("Waiting for players...");
        }
    }


    private void broadcastConnectedPlayers() {
        // Just show all connected players in the connected room
        StringBuilder connectedMessage = new StringBuilder("CONNECTED:");
        for (ClientHandler client : connectedClients) {
            if (playerNames.containsKey(client)) {
                connectedMessage.append(playerNames.get(client)).append(",");
            }
        }
        if (connectedMessage.length() > "CONNECTED:".length()) {
            connectedMessage.setLength(connectedMessage.length() - 1);
        }

        // Send to all clients
        for (ClientHandler client : connectedClients) {
            try {
                client.sendMessage(connectedMessage.toString());
            } catch (Exception e) {
                removeClient(client);
            }
        }
    }
    

    private void broadcastWaitingRoomStatus(String timerMessage) {
        if (waitingPlayers.isEmpty()) return;

        StringBuilder waitingMessage = new StringBuilder("WAITING:");
        for (ClientHandler client : waitingPlayers) {
            waitingMessage.append(playerNames.get(client)).append(",");
        }
        waitingMessage.setLength(waitingMessage.length() - 1);
        waitingMessage.append("|").append(timerMessage);

        for (ClientHandler client : waitingPlayers) {
            try {
                client.sendMessage(waitingMessage.toString());
                System.out.println("Sent to " + playerNames.get(client) + ": " + waitingMessage);
            } catch (Exception e) {
                System.err.println("Error sending to " + playerNames.get(client));
                removeClient(client);
            }
        }
    }

    public synchronized void playerWantsToPlay(ClientHandler client) {
        if (!waitingPlayers.contains(client)) {
            waitingPlayers.add(client);
            broadcastWaitingRoomStatus("Waiting for players...");
            checkAndStartWaitingRoomTimer();
        }
    }


    private void checkAndStartWaitingRoomTimer() {
        if (waitingPlayers.size() >= 2 && waitingPlayers.size() < MAX_PLAYERS_PER_ROOM) {
            if (waitingRoomTimer == null) {
                int secondsLeft = WAITING_ROOM_TIMER;
                waitingRoomTimer = new Timer();
                waitingRoomTimer.scheduleAtFixedRate(new TimerTask() {
                    int timeLeft = secondsLeft;
                    
                    @Override
                    public void run() {
                        if (timeLeft <= 0 || waitingPlayers.size() >= MAX_PLAYERS_PER_ROOM) {
                            waitingRoomTimer.cancel();
                            waitingRoomTimer = null;
                            if (waitingPlayers.size() >= 2) {
                                createGameRoom();
                            }
                        } else {
                            broadcastWaitingRoomStatus("Starting in " + timeLeft + " seconds...");
                            timeLeft--;
                        }
                    }
                }, 0, 1000);
            }
        } else if (waitingPlayers.size() >= MAX_PLAYERS_PER_ROOM) {
            if (waitingRoomTimer != null) {
                waitingRoomTimer.cancel();
                waitingRoomTimer = null;
            }
            createGameRoom();
        }
    }

    private synchronized void createGameRoom() {
        int playerCount = Math.min(waitingPlayers.size(), MAX_PLAYERS_PER_ROOM);
        List<ClientHandler> gamePlayers = new ArrayList<>(
            waitingPlayers.subList(0, playerCount));
        
        // Create new game room
        GameRoom gameRoom = new GameRoom(gamePlayers, this);
        gameRooms.add(gameRoom);
        
        // Remove players from waiting list
        waitingPlayers.removeAll(gamePlayers);
        
        // Notify players game is starting
        for (ClientHandler player : gamePlayers) {
            player.sendMessage("GAME_START");
        }
        
        // Start the game
        new Thread(gameRoom).start();
        
        // Update waiting room status
        broadcastWaitingRoomStatus();
    }


    private void broadcastWaitingRoomStatus() {
        broadcastWaitingRoomStatus("");
    }

    public synchronized void removeClient(ClientHandler client) {
        connectedClients.remove(client);
        waitingPlayers.remove(client);
        playerNames.remove(client);
    
        for (GameRoom room : gameRooms) {
            if (room.hasPlayer(client)) {
                room.removePlayer(client.getPlayerName());
            }
        }
    
        broadcastConnectedPlayers();
        broadcastWaitingRoomStatus();
    }

    public synchronized void handleAnswer(ClientHandler client, String answer) {
        for (GameRoom room : gameRooms) {
            if (room.hasPlayer(client)) {
                room.handleAnswer(client, answer);
                break;
            }
        }
    }

    public boolean isGameStarted(ClientHandler client) {
        for (GameRoom room : gameRooms) {
            if (room.hasPlayer(client) && room.isGameStarted()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    
}
