/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codebreaker;

/**
 *
 * @author Norah
 */

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameServer server;
    private String playerName;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            // First message from client should be their name
            playerName = in.readLine();
            server.addPlayer(this, playerName);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equals("READY")) {
                    server.playerWantsToPlay(this);
                } else if (server.isGameStarted(this)) {
                    server.handleAnswer(this, message);
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getPlayerName() {
        return playerName;
    }
}
