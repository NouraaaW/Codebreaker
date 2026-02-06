/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codebreaker;

/**
 *
 * @author Norah
 */


import java.util.*;
import java.util.stream.Collectors;

public class GameRoom implements Runnable {
    private List<ClientHandler> players;
    private GameServer server;
    private boolean gameStarted;
    private GameState gameState;
    private Timer currentTimer;

    public GameRoom(List<ClientHandler> players, GameServer server) {
        this.players = new ArrayList<>(players);
        this.server = server;
        this.gameStarted = false;
        this.gameState = new GameState();
        
        // Initialize players in game state (only if not already exists)
        players.forEach(player -> gameState.addPlayer(player.getPlayerName()));
    }

    @Override
    public void run() {
        gameStarted = true;
        startGame();
    }

    private void startGame() {
        broadcast("GAME_START");
        broadcastScores(); // Initialize scores at game start
        nextStage();
    }

    private void nextStage() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
    
        String nextImagePath = gameState.nextImage();
        
        System.out.println("[DEBUG] Attempting to load image: " + nextImagePath);
        System.out.println("[DEBUG] Current image index: " + gameState.getCurrentImageIndex());
    
        if (gameState.getCurrentImageIndex() >= 5) { // أو gameState.isGameFinished()
            String winnerName = gameState.getWinnerName();
            int winnerPoints = gameState.getWinnerPoints();
            String winnerMessage = String.format("%s wins with %d points!", winnerName, winnerPoints);
            broadcast("GAME_OVER:" + winnerMessage);
            System.out.println("[DEBUG] Game over, winner: " + winnerMessage);
            return;
        }
    
        try {
            String imageName = nextImagePath.substring(nextImagePath.lastIndexOf('/') + 1);
            broadcast("IMAGE:" + imageName);
            System.out.println("[DEBUG] Sent image to clients: " + imageName);
    
            currentTimer = new Timer();
            currentTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    broadcast("TIME_UP");
                    System.out.println("[DEBUG] Time up for current stage");
                    nextStage(); 
                }
            }, 240000); 
    
            broadcastScores();
            System.out.println("[DEBUG] Started new stage with timer");
    
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start next stage: " + e.getMessage());
            e.printStackTrace();
            
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    nextStage();
                }
            }, 2000);
        }
    }

    private void endGame() {
        String winner = gameState.getWinner();
        broadcast("GAME_OVER:" + winner);
        gameStarted = false;
        //ed!t 
        //server.broadcastWinner(winner);
    }

    public void handleAnswer(ClientHandler client, String answer) {
        if (!gameStarted) return;

        String playerName = client.getPlayerName();
        if (gameState.checkAnswer(answer)) {
            gameState.addPoints(playerName, 1);
            broadcast(playerName + " found the correct code!");
            broadcastScores();
            nextStage();
        }
    }

    public void removePlayer(String playerName) {
        players.removeIf(p -> p.getPlayerName().equals(playerName));
        gameState.removePlayer(playerName); // remove from scores
        broadcastScores(); // update the scoreboard on all clients

        if (players.size() <= 1) { // end the game if only one player left
            endGame();
        }
    }

    private void broadcastScores() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        
        gameState.getPlayerPoints().entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEachOrdered(entry -> scores.put(entry.getKey(), entry.getValue()));
        
        String scoreMessage = "SCORES:" +
            scores.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
        
        broadcast(scoreMessage);
    }

    public void broadcast(String message) {
        players.forEach(player -> player.sendMessage(message));
    }

    public boolean hasPlayer(ClientHandler client) {
        return players.contains(client);
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
}
