/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codebreaker;

/**
 *
 * @author Norah
 */
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;



public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JFrame frame;
    private JTextArea playerList;
    private JTextArea scoreList;
    private JButton connectButton;
    private JButton playButton;
    private String playerName;
    private final String HOST = "localhost";
    private final int PORT = 5555;
    private Point mouseDownCompCoords = null;

    // Background image paths
    private final String LOGIN_BG = "src\\codebreaker\\NetworkLevels\\12.jpg";
    private final String CONNECTED_BG = "src\\codebreaker\\NetworkLevels\\13.jpg";
    private final String WAITING_BG = "src\\codebreaker\\NetworkLevels\\6.jpg";
    private final String GAME_BG = "src\\codebreaker\\NetworkLevels\\1.jpg";
    private final String L2 = "src\\codebreaker\\NetworkLevels\\2.jpg";
    private final String L3 = "src\\codebreaker\\NetworkLevels\\3.jpg";
    private final String L4 = "src\\codebreaker\\NetworkLevels\\4.jpg";
    private final String L5 = "src\\codebreaker\\NetworkLevels\\5.jpg";

    
    public GameClient() {
        createLoginWindow();
    }

    private BufferedImage loadGameImage(String imageName) {
        String[] paths = {
            "src/codebreaker/NetworkLevels/" + imageName,
            "NetworkLevels/" + imageName,
            "../NetworkLevels/" + imageName,
            imageName
        };
        
        for (String path : paths) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    System.out.println("Loading image from: " + file.getAbsolutePath());
                    return ImageIO.read(file);
                }
            } catch (IOException e) {
                System.out.println("Failed to load from: " + path);
            }
        }
        
        System.err.println("Could not load image: " + imageName);
        return null;
    }
    


    private void setupFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));

        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                mouseDownCompCoords = null;
            }
        });

        frame.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });
    }

    private JButton createCloseButton() {
        JButton closeButton = new JButton("X");
        closeButton.setName("closeButton");
        closeButton.setFont(new Font("Arial", Font.BOLD, 24));
        closeButton.setForeground(Color.RED);
        closeButton.setBackground(new Color(0, 0, 0, 0)); 
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setPreferredSize(new Dimension(50, 50));
        closeButton.addActionListener(e -> System.exit(0));
        return closeButton;
    }

    //(Login Window)
    private void createLoginWindow() {
        frame = new JFrame("CodeBreaker");
        setupFrame();
    
        try {
            BufferedImage bgImage = ImageIO.read(new File(LOGIN_BG));
            BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
            mainPanel.setLayout(new GridBagLayout());
    
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(createCloseButton(), BorderLayout.EAST);
    
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(10, 10, 10, 10);
            mainPanel.add(topPanel, gbc);
    
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0; 
            gbc.weightx = 1.0;
            gbc.weighty = 0.5; 
            gbc.anchor = GridBagConstraints.SOUTH;
            gbc.insets = new Insets(0, 0, -20, 0);  
    
            JTextField nameField = new JTextField(12);
            nameField.setFont(new Font("PixelFont", Font.PLAIN, 35));
            nameField.setOpaque(false);
            nameField.setForeground(Color.DARK_GRAY);
            nameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            mainPanel.add(nameField, gbc);
    
            gbc.gridy = 1;  
            gbc.weighty = 0.1;  
            gbc.insets = new Insets(0, 0, 130, 20);
    
            connectButton = new JButton("Connect");
            connectButton.setFont(new Font("PixelFont", Font.BOLD, 30));
            connectButton.setContentAreaFilled(false);
            connectButton.setBorderPainted(false);
            connectButton.setForeground(Color.DARK_GRAY);
            
            connectButton.addActionListener(e -> {
                playerName = nameField.getText().trim();
                if (!playerName.isEmpty()) {
                    if (connect()) {
                        createConnectedRoom();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter your name", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            mainPanel.add(connectButton, gbc);
    
            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //(Connected Room)
    private void createConnectedRoom() {
        frame.getContentPane().removeAll();
        try {
            BufferedImage bgImage = ImageIO.read(new File(CONNECTED_BG));
            BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
            mainPanel.setLayout(new GridBagLayout());
    
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(createCloseButton(), BorderLayout.EAST);
    
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(15, 15, 15, 15);
            mainPanel.add(topPanel, gbc);
    
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
    
            playerList = new JTextArea(10, 15);
            playerList.setEditable(false);
            playerList.setOpaque(false);
            playerList.setForeground(Color.darkGray);
            playerList.setFont(new Font("PixelFont", Font.BOLD, 28));
            playerList.setLineWrap(true);
            playerList.setWrapStyleWord(true);
            playerList.setBorder(BorderFactory.createEmptyBorder(0, 150, 160, 150));
    
            JScrollPane scrollPane = new JScrollPane(playerList);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER); 
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); 
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
    
            centerPanel.add(scrollPane, BorderLayout.CENTER);
    
            GridBagConstraints gbcCenter = new GridBagConstraints();
            gbcCenter.gridx = 0;
            gbcCenter.gridy = 1;
            gbcCenter.weightx = 1.0;
            gbcCenter.weighty = 1.0;
            gbcCenter.fill = GridBagConstraints.BOTH;
            gbcCenter.insets = new Insets(90, 50, 30, 100);
            mainPanel.add(centerPanel, gbcCenter);
    
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setOpaque(false);
    
            playButton = new JButton("Play");
            playButton.setFont(new Font("PixelFont", Font.BOLD, 30));
            playButton.setContentAreaFilled(false);
            playButton.setBorderPainted(false);
            playButton.setForeground(Color.WHITE);
            playButton.setPreferredSize(new Dimension(200, 30));
    
            playButton.addActionListener(e -> {
                if (socket == null || socket.isClosed()) {
                    JOptionPane.showMessageDialog(frame, "Not connected to server", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                out.println("READY");
            });
    
            buttonPanel.add(playButton);
    
            GridBagConstraints gbcButton = new GridBagConstraints();
            gbcButton.gridx = 0;
            gbcButton.gridy = 2;
            gbcButton.weightx = 1.0;
            gbcButton.weighty = 0.0;
            gbcButton.anchor = GridBagConstraints.CENTER;
            gbcButton.insets = new Insets(0, 0, 10, 0);
            mainPanel.add(buttonPanel, gbcButton);
    
            frame.add(mainPanel);
            frame.revalidate();
            frame.repaint();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading room background: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // (Waiting Room)
    private JLabel timerLabel;

    private void createWaitingRoom() {
        frame.getContentPane().removeAll();
        try {
            BufferedImage bgImage = ImageIO.read(new File(WAITING_BG));
            BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
            mainPanel.setLayout(new GridBagLayout());
    
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(createCloseButton(), BorderLayout.EAST);
    
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(15, 15, 15, 15);
            mainPanel.add(topPanel, gbc);
    
            gbc.gridy = 1;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(90, 50, 30, 180);
    
            playerList = new JTextArea(8, 12);
            playerList.setEditable(false);
            playerList.setOpaque(false);
            playerList.setForeground(Color.white);
            playerList.setFont(new Font("PixelFont", Font.BOLD, 28));
            playerList.setAlignmentX(Component.TOP_ALIGNMENT);
            playerList.setBorder(BorderFactory.createEmptyBorder(0, 150, 160, 150));
    
            JScrollPane scrollPane = new JScrollPane(playerList);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    
            mainPanel.add(scrollPane, gbc);
    
            // timer
            timerLabel = new JLabel("Waiting for players...", SwingConstants.CENTER);
            timerLabel.setFont(new Font("PixelFont", Font.BOLD, 22));
            timerLabel.setForeground(new Color(255, 255, 255)); 
            timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    
            gbc.gridy = 2;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(0, 0, 20, 0);
            mainPanel.add(timerLabel, gbc);
    
            frame.add(mainPanel);
            frame.revalidate();
            frame.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // (Game Room)
    private void createGameRoom() {
        frame.getContentPane().removeAll();
        try {
            BufferedImage bgImage = ImageIO.read(new File(GAME_BG));
            BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
            mainPanel.setLayout(new GridBagLayout());
    
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(createCloseButton(), BorderLayout.EAST);
    
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(15, 15, 15, 15);
            mainPanel.add(topPanel, gbc);
    
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(60, 250, 0, 65);
    
            JTextPane scorePane = new JTextPane();
            scorePane.setName("scorePane");
            scorePane.setEditable(false);
            scorePane.setOpaque(false);
            scorePane.setForeground(Color.white);
            scorePane.setFont(new Font("PixelFont", Font.BOLD, 19));
            
            int charsPerLine = 18;
            int charWidth = 10; 
            int paneWidth = charsPerLine * charWidth;
            
            scorePane.setPreferredSize(new Dimension(paneWidth, 500));
            
            SimpleAttributeSet leftAlign = new SimpleAttributeSet();
            StyleConstants.setAlignment(leftAlign, StyleConstants.ALIGN_LEFT);
            StyleConstants.setSpaceAbove(leftAlign, 5); 
            StyleConstants.setSpaceBelow(leftAlign, 10);
            
            StyledDocument doc = scorePane.getStyledDocument();
            doc.setParagraphAttributes(0, doc.getLength(), leftAlign, false);
    
            JScrollPane scrollPane = new JScrollPane(scorePane);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setPreferredSize(new Dimension(paneWidth + 50, 300)); 
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    
            mainPanel.add(scrollPane, gbc);
    
            JPanel inputPanel = new JPanel();
            inputPanel.setOpaque(false);
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 30, 0));
    
            JTextField answerField = new JTextField();
            answerField.setFont(new Font("PixelFont", Font.PLAIN, 28));
            answerField.setOpaque(false);
            answerField.setForeground(Color.white);
            answerField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            answerField.setMaximumSize(new Dimension(150, 40));
            answerField.setPreferredSize(new Dimension(150, 40));
            inputPanel.add(answerField);
    
            inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    
            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                String answer = answerField.getText().trim();
                if (!answer.isEmpty()) {
                    out.println(answer); 
                    answerField.setText(""); 
                }
            });
            submitButton.setFont(new Font("PixelFont", Font.BOLD, 28));
            submitButton.setContentAreaFilled(false);
            submitButton.setBorderPainted(false);
            submitButton.setForeground(Color.white);
            submitButton.setBorder(BorderFactory.createLineBorder(Color.white, 2));
            submitButton.setAlignmentY(Component.CENTER_ALIGNMENT);
            submitButton.setPreferredSize(new Dimension(150, 40));
            inputPanel.add(submitButton);
    
            gbc.gridy = 2;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            gbc.insets = new Insets(0, 60, 0, 0);
            mainPanel.add(inputPanel, gbc);
    
            frame.add(mainPanel);
            frame.revalidate();
            frame.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void updateRoom(String message) {
        System.out.println("Server message: " + message);
    
        if (message == null) {
            System.out.println("Received null message");
            return;
        }
        
        if (message.startsWith("WAITING")) {
            System.out.println("Transitioning to waiting room");
            createWaitingRoom();
            handleWaitingMessage(message);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (message.startsWith("CONNECTED:")) {
                    handleConnectedMessage(message);
                } else if (message.startsWith("WAITING:")) {
                    createWaitingRoom(); 
                    handleWaitingMessage(message);
                } else if (message.equals("GAME_START")) {
                    createGameRoom();
                } else if (message.startsWith("SCORES:")) {
                    handleScoresMessage(message);
                } else if (message.startsWith("GAME_OVER:")) {
                    handleGameOverMessage(message);
                }
            } catch (Exception e) {
                System.err.println("Error updating room: " + e.getMessage());
                e.printStackTrace();
            }
        });

        if (message.startsWith("IMAGE:")) {
            String imagePath = message.substring(6);
            SwingUtilities.invokeLater(() -> {
                updateGameBackground(imagePath);
                frame.revalidate();
                frame.repaint();
            });
        } else if (message.equals("TIME_UP")) {
            JOptionPane.showMessageDialog(frame, "TIME_UP");
        }
    }
    
    private void updateGameBackground(String imageName) {
        BufferedImage image = loadGameImage(imageName);
        if (image != null) {
            Component component = frame.getContentPane().getComponent(0);
            if (component instanceof BackgroundPanel) {
                BackgroundPanel panel = (BackgroundPanel) component;
                panel.setBackgroundImage(image);  // This is a void method
                panel.repaint();  // Call repaint separately
            }
        } else {
            System.err.println("Failed to update background for: " + imageName);
        }
    }

    
    private void handleConnectedMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length > 1) {
            String[] players = parts[1].split(",");
            playerList.setText("");
            for (String player : players) {
                if (!player.trim().isEmpty()) {
                    playerList.append(player.trim() + "\n");
                }
            }
        }
    }


    private BufferedImage loadUniversalImage(String filename) {
        // Try all possible locations
        String[] locations = {
            filename,                           // Same directory
            "NetworkLevels/" + filename,        // Common deployment
            "../NetworkLevels/" + filename,  
            "src\\codebreaker\\NetworkLevels\\" + filename,    // IDE run location
        };
    
        for (String path : locations) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    System.out.println("[SUCCESS] Loading: " + file.getAbsolutePath());
                    return ImageIO.read(file);
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to load: " + path);
            }
        }
        System.err.println("[CRITICAL] Image not found: " + filename);
        return null;
    }


    private void handleWaitingMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length > 1) {
            String[] info = parts[1].split("\\|", 2);
            String[] readyPlayers = info[0].split(",");
            
            StringBuilder playersText = new StringBuilder();
            for (String player : readyPlayers) {
                if (!player.trim().isEmpty()) {
                    playersText.append(player.trim()).append("\n");
                }
            }
            
            playerList.setText(playersText.toString());
            
            if (timerLabel != null) {
                if (readyPlayers.length >= 2 && info.length > 1) {
                    timerLabel.setText(info[1]);
                } else {
                    timerLabel.setText("Waiting for players..."); 
                }
            }
        }
    }
    
    private void handleScoresMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length > 1) {
            String[] playerScores = parts[1].split(",");
            StringBuilder scoresText = new StringBuilder();
            
            for (String score : playerScores) {
                String[] scoreInfo = score.split(":", 2);
                if (scoreInfo.length == 2) {
                    scoresText.append(scoreInfo[0]).append(": ").append(scoreInfo[1]).append("\n");
                }
            }
            
            Component[] components = frame.getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof BackgroundPanel) {
                    Component[] bgComponents = ((BackgroundPanel)comp).getComponents();
                    for (Component bgComp : bgComponents) {
                        if (bgComp instanceof JScrollPane) {
                            JViewport viewport = ((JScrollPane)bgComp).getViewport();
                            if (viewport.getView() instanceof JTextPane) {
                                JTextPane scorePane = (JTextPane)viewport.getView();
                                StyledDocument doc = scorePane.getStyledDocument();
                                try {
                                    doc.remove(0, doc.getLength());
                                    doc.insertString(0, scoresText.toString(), null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private JTextPane findScorePaneInFrame() {
        Component[] components = frame.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof BackgroundPanel) {
                Component[] bgComponents = ((BackgroundPanel)comp).getComponents();
                for (Component bgComp : bgComponents) {
                    if (bgComp instanceof JScrollPane) {
                        JViewport viewport = ((JScrollPane)bgComp).getViewport();
                        if (viewport.getView() instanceof JTextPane) {
                            return (JTextPane)viewport.getView();
                        }
                    }
                }
            }
        }
        return null;
    }

    private void handleGameOverMessage(String message) {
        String winner = message.substring(10);
        
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            
            try {
                BufferedImage bgImage = loadGameImage("14.jpg");
                if (bgImage == null) {
                    throw new IOException("Failed to load game over image");
                }
                
                BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
                mainPanel.setLayout(new GridBagLayout()); 
                
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);
                topPanel.add(createCloseButton(), BorderLayout.EAST);
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1.0;
                gbc.weighty = 0.0;
                gbc.anchor = GridBagConstraints.NORTHEAST;
                gbc.insets = new Insets(15, 15, 15, 15);
                mainPanel.add(topPanel, gbc);
                
                JLabel winnerLabel = new JLabel(winner, SwingConstants.CENTER);
                winnerLabel.setFont(new Font("PixelFont", Font.BOLD, 24));
                winnerLabel.setForeground(Color.WHITE);
                
                gbc.gridy = 1;
                gbc.weighty = 1.0;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets = new Insets(0, 0, 230, 0); 
                mainPanel.add(winnerLabel, gbc);
                
                frame.setContentPane(mainPanel);
                frame.revalidate();
                frame.repaint();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, 
                    "Error displaying game over screen: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    
    private boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        updateRoom(message);
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(frame, "Disconnected from server: " + e.getMessage(), 
                            "Connection Error", JOptionPane.ERROR_MESSAGE));
                } finally {
                    closeConnection();
                }
            }).start();

            out.println(playerName);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to server: " + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }

        
    }


public static void main(String[] args) {
    // Debug paths
    System.out.println("Working Directory: " + System.getProperty("user.dir"));
    File img = new File("NetworkLevels/1.jpg");
    System.out.println("Image path: " + img.getAbsolutePath());
    System.out.println("Exists: " + img.exists());
    
    // Start application
    SwingUtilities.invokeLater(() -> new GameClient());
}


    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private String winnerText;

        public BackgroundPanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
            setOpaque(backgroundImage == null);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
            
            if (winnerText != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                
                // Calculate the position for the winner text (in the yellow area)
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2 - 20; // Slightly above center to fit in the yellow area
                
                // Set up the font and color
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.setColor(Color.white);
                
                // Center the text
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(winnerText);
                g2d.drawString(winnerText, centerX - textWidth / 2, centerY);
            }
        }

        public void setBackgroundImage(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
            repaint();
        }

        public void setWinnerText(String text) {
            this.winnerText = text;
            repaint();
        }
    }
}
