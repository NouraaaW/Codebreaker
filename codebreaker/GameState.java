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

public class GameState {
    private Map<String, Integer> playerPoints;  // Player name -> points
    private String currentAnswer;
    private int currentImageIndex;

    private final String[] IMAGES = {
        "1.jpg",  // File names only
        "2.jpg",
        "3.jpg",
        "4.jpg",
        "5.jpg",
        "14.jpg"
    };

    private final String[] ANSWERS = {
        "164", "042", "846", "264", "853"
    };

    public GameState() {
        playerPoints = new HashMap<>();
        currentImageIndex = -1;
    }

    public void addPlayer(String playerName) {
        playerPoints.putIfAbsent(playerName, 0);
    }

    public void removePlayer(String playerName) {
       playerPoints.remove(playerName);  // Don't remove points, just remove from active players
    }

    public void addPoints(String playerName, int points) {
        playerPoints.put(playerName, playerPoints.getOrDefault(playerName, 0) + points);
    }

    public String getScoreboard() {
        StringBuilder sb = new StringBuilder("Current Scores:\n");
        playerPoints.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" points\n"));
        return sb.toString();
    }

    public String nextImage() {
        currentImageIndex = (currentImageIndex + 1) % IMAGES.length;
        currentAnswer = ANSWERS[Math.min(currentImageIndex, ANSWERS.length - 1)];
        return IMAGES[currentImageIndex]; // Returns 14.jpg when index=5
    }
    

    public boolean checkAnswer(String answer) {
        return answer.trim().equalsIgnoreCase(currentAnswer);
    }

    public String getCurrentImage() {
        return currentImageIndex >= 0 ? IMAGES[currentImageIndex] : null;
    }

    public int getCurrentImageIndex() {
        return currentImageIndex;
    }

    public boolean isGameFinished() {
        return currentImageIndex >= IMAGES.length - 1;
    }

    public String getWinnerName() {
        if (playerPoints.isEmpty()) return "No players";

        return playerPoints.entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("No winner");
    }

    public int getWinnerPoints() {
        if (playerPoints.isEmpty()) return 0;

        return playerPoints.entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getValue)
            .orElse(0);
    }

    public String getWinner() {
        if (playerPoints.isEmpty()) return "No players in the game";

        Optional<Map.Entry<String, Integer>> maxEntry = playerPoints.entrySet()
            .stream()
            .max(Map.Entry.comparingByValue());

        if (maxEntry.isPresent()) {
            int maxPoints = maxEntry.get().getValue();
            long winnersCount = playerPoints.values().stream().filter(v -> v == maxPoints).count();

            if (winnersCount > 1) {
                return "It's a tie! Multiple players have " + maxPoints + " points!";
            }
            return maxEntry.get().getKey() + " wins with " + maxPoints + " points!";
        }
        return "No winner";
    }

    public int getPlayerPoints(String playerName) {
        return playerPoints.getOrDefault(playerName, 0);
    }

    public Map<String, Integer> getPlayerPoints() {
        return new HashMap<>(playerPoints); // Return defensive copy
    }
}
