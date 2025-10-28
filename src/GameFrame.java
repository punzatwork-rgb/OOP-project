import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private GamePanel gamePanel;
    private JPanel titlePanel;
    private JPanel tutorialPanel; 
    private JPanel endPanel;
    private JLabel endMessageLabel; 

    public static final String TITLE_SCREEN = "Title";
    public static final String TUTORIAL_SCREEN = "Tutorial"; 
    public static final String GAME_SCREEN = "Game";
    public static final String END_SCREEN = "End";

    public GameFrame() {
        setTitle("Lanes Tower Defense");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // --- Create Screens ---
        titlePanel = createTitleScreen();
        tutorialPanel = createTutorialScreen(); 
        endPanel = createEndScreen();

        mainPanel.add(titlePanel, TITLE_SCREEN);
        mainPanel.add(tutorialPanel, TUTORIAL_SCREEN); 
        mainPanel.add(endPanel, END_SCREEN);

        add(mainPanel);
        setPreferredSize(new Dimension(1000, 520)); 
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Start on the Title Screen
        cardLayout.show(mainPanel, TITLE_SCREEN);
    }

    // --- Screen Setup Methods ---

    private JPanel createTitleScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(20, 20, 30));

        JLabel titleLabel = new JLabel("Botanical Royale");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 48));
        panel.add(titleLabel);
        
        JLabel pressAnyKey = new JLabel("<html><br>- Press any KEY to continue -</html>");
        pressAnyKey.setForeground(Color.ORANGE);
        pressAnyKey.setFont(new Font("Comic Sans MS", Font.ITALIC, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 0, 0, 0);

        panel.add(pressAnyKey, gbc);
        
        // Listener to transition on any key press
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                showTutorialScreen();
            }
        });
        panel.setFocusable(true); // Must be focusable to receive key events
        
        // Ensure the panel has focus when shown
        SwingUtilities.invokeLater(panel::requestFocusInWindow);

        return panel;
    }

    private JPanel createTutorialScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 40, 60));
        
        String tutorialText = "<html><center>"
            + "<h2>TUTORIAL</h2>"
            + "<hr style='width: 50%;'>"
            + "<p style='font-size: 20pt;'>Use NUMBER KEYS (1 to 5) to summon units from the bottom bar</p>"
            + "<p style='font-size: 20pt;'>Clicking the UPGRADE button to increase currency generation rate</p>"
            + "<p style='font-size: 20pt;'>Use UP/DOWN ARROWS to change the selected lane.</p>"
            + "<hr style='width: 50%;'>"
            + "<p style='font-size: 16pt; color: #FFD700;'>Press ENTER to start the battle!</p>"
            + "</center></html>";

        JLabel tutorialLabel = new JLabel(tutorialText);
        tutorialLabel.setForeground(Color.WHITE);
        panel.add(tutorialLabel);
        
        // Listener to transition on ENTER key press
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startGame();
                }
            }
        });
        panel.setFocusable(true);
        
        return panel;
    }

    private JPanel createEndScreen() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 30));

        endMessageLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        endMessageLabel.setForeground(Color.RED);
        endMessageLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 48));
        panel.add(endMessageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 40));
        buttonPanel.setBackground(new Color(20, 20, 30));
        
        JButton retryButton = new JButton("RETRY");
        retryButton.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        retryButton.addActionListener(e -> startGame());

        JButton titleButton = new JButton("TITLE");
        titleButton.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        titleButton.addActionListener(e -> showTitleScreen());
        
        buttonPanel.add(retryButton);
        buttonPanel.add(titleButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Screen Control ---

    public void startGame() {
        if (gamePanel != null) {
            gamePanel.stopTimers();
            mainPanel.remove(gamePanel);
        }

        gamePanel = new GamePanel(this); 
        mainPanel.add(gamePanel, GAME_SCREEN);
        gamePanel.scheduleNextEnemySpawn();
        
        cardLayout.show(mainPanel, GAME_SCREEN);
        gamePanel.requestFocusInWindow();
    }

    public void showTitleScreen() {
        if (gamePanel != null) {
            gamePanel.stopTimers();
        }
        cardLayout.show(mainPanel, TITLE_SCREEN);
        titlePanel.requestFocusInWindow();
    }
    
    public void showTutorialScreen() {
        cardLayout.show(mainPanel, TUTORIAL_SCREEN);
        tutorialPanel.requestFocusInWindow();
    }

    public void showEndScreen(String finalMessage) {
        endMessageLabel.setText(finalMessage);
        cardLayout.show(mainPanel, END_SCREEN);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}