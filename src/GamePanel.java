import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // Constants
    private static final int LANE_COUNT = 3;
    private static final int LANE_HEIGHT = 90;
    private static final int LANE_SPACING = 30;
    private static final int L1_Y = 160;
    private static final int L2_Y = L1_Y + LANE_HEIGHT + LANE_SPACING;
    private static final int L3_Y = L2_Y + LANE_HEIGHT + LANE_SPACING;
    private static final double SPEED_SCALE = 0.7;
    private static final int MAX_GOLD_UPGRADES = 4;
    private static final int GOLD_PER_UPGRADE = -120;
    private static final int TIMER_DELAY_MS = 30;
    private static final int GOLD_GENERATION_FRAMES = 1000;
    private static final int ELITE_SPAWN_INTERVAL_MS = 20_000;
    private static final int MIN_ENEMY_SPAWN_DELAY = 1500;
    private static final int MAX_ENEMY_SPAWN_DELAY = 4000;
    
    // Game state
    private final GameFrame parentFrame;
    private final int[] laneY = {L1_Y, L2_Y, L3_Y};
    private int selectedLane = 1;
    
    // Timers
    private Timer gameTimer;
    private Timer enemySpawnTimer;
    
    // Units per lane
    private final ArrayList<Unit>[] playerUnits;
    private final ArrayList<Unit>[] enemyUnits;
    
    // Bases
    private Base playerBase;
    private Base enemyBase;
    
    // UI Components
    private JPanel bottomBar;
    private JButton btnUnit1, btnUnit2, btnUnit3, btnUnit4, btnUnit5, btnUpgrade;
    
    // Game status
    private boolean gameOver = false;
    private final Random random = new Random();
    
    // Economy
    private int gold = 100;
    private int goldPerSecond = 10;
    private int goldUpgradeCount = 0;
    private int upgradeCost = 40;
    private int goldGenerationFrames = GOLD_GENERATION_FRAMES;
    
    // Time tracking
    private int frameCount = 0;
    private int elapsedMs = 0;
    private int nextEliteSpawnMs = ELITE_SPAWN_INTERVAL_MS;
    
    private final UnitFactory factory = new UnitFactory();

    @SuppressWarnings("unchecked")
    public GamePanel(GameFrame frame) {
        this.parentFrame = frame;
        
        // Initialize unit arrays first (must be done before other initialization)
        this.playerUnits = new ArrayList[LANE_COUNT];
        this.enemyUnits = new ArrayList[LANE_COUNT];
        
        initializePanel();
        initializeLanes();
        initializeBases();
        initializeUI();
        setupInputBindings();
        startGameLoop();
        
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void initializePanel() {
        setPreferredSize(new Dimension(1000, 520));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (enemyBase != null) {
                    enemyBase.x = getWidth() - 68;
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void initializeLanes() {
        for (int i = 0; i < LANE_COUNT; i++) {
            playerUnits[i] = new ArrayList<>();
            enemyUnits[i] = new ArrayList<>();
        }
    }

    private void initializeBases() {
        int width = getPreferredSize().width;
        playerBase = new Base(20, 90, 48, 360, 100, new Color(60, 120, 200));
        enemyBase = new Base(width - 68, 90, 48, 360, 500, new Color(200, 80, 80));
    }

    private void initializeUI() {
        bottomBar = new JPanel(new GridLayout(1, 6, 10, 0));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        bottomBar.setBackground(new Color(245, 245, 245));
        bottomBar.setPreferredSize(new Dimension(getPreferredSize().width, 48));

        btnUnit1 = createUnitButton("Apple (20)", UnitType.UNIT1);
        btnUnit2 = createUnitButton("Banana (25)", UnitType.UNIT2);
        btnUnit3 = createUnitButton("Grape (30)", UnitType.UNIT3);
        btnUnit4 = createUnitButton("Cherry (40)", UnitType.UNIT4);
        btnUnit5 = createUnitButton("Orange (50)", UnitType.UNIT5);
        btnUpgrade = createUpgradeButton();

        bottomBar.add(btnUnit1);
        bottomBar.add(btnUnit2);
        bottomBar.add(btnUnit3);
        bottomBar.add(btnUnit4);
        bottomBar.add(btnUnit5);
        bottomBar.add(btnUpgrade);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JButton createUnitButton(String text, UnitType unitType) {
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            spawnPlayer(unitType);
            requestFocusInWindow();
        });
        return button;
    }

    private JButton createUpgradeButton() {
        JButton button = new JButton("Upgrade (" + upgradeCost + ")");
        button.addActionListener(e -> {
            handleUpgrade();
            requestFocusInWindow();
        });
        return button;
    }

    private void handleUpgrade() {
        if (goldUpgradeCount >= MAX_GOLD_UPGRADES || gold < upgradeCost) {
            return;
        }
        
        gold -= upgradeCost;
        goldUpgradeCount++;
        goldGenerationFrames += GOLD_PER_UPGRADE;
        upgradeCost *= 2;
        
        if (goldUpgradeCount >= MAX_GOLD_UPGRADES) {
            btnUpgrade.setText("Maxed");
            btnUpgrade.setEnabled(false);
        } else {
            btnUpgrade.setText("Upgrade (" + upgradeCost + ")");
        }
    }

    private void setupInputBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Lane switching
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "laneUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "laneDown");
        actionMap.put("laneUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedLane = Math.max(0, selectedLane - 1);
                repaint();
            }
        });
        actionMap.put("laneDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedLane = Math.min(LANE_COUNT - 1, selectedLane + 1);
                repaint();
            }
        });

        // Unit spawning
        setupUnitSpawnKey(inputMap, actionMap, '1', UnitType.UNIT1);
        setupUnitSpawnKey(inputMap, actionMap, '2', UnitType.UNIT2);
        setupUnitSpawnKey(inputMap, actionMap, '3', UnitType.UNIT3);
        setupUnitSpawnKey(inputMap, actionMap, '4', UnitType.UNIT4);
        setupUnitSpawnKey(inputMap, actionMap, '5', UnitType.UNIT5);
    }

    private void setupUnitSpawnKey(InputMap inputMap, ActionMap actionMap, 
                                   char key, UnitType unitType) {
        String actionName = "spawn" + key;
        inputMap.put(KeyStroke.getKeyStroke(key), actionName);
        actionMap.put(actionName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                spawnPlayer(unitType);
                requestFocusInWindow();
            }
        });
    }

    private void startGameLoop() {
        gameTimer = new Timer(TIMER_DELAY_MS, this);
        gameTimer.start();
    }

    public void stopTimers() {
        if (gameTimer != null) gameTimer.stop();
        if (enemySpawnTimer != null) enemySpawnTimer.stop();
    }

    private void spawnPlayer(UnitType type) {
        int cost = factory.cost(type);
        if (gold < cost) return;

        int spawnX = playerBase.getBounds().x + playerBase.getBounds().width + 8;
        int spawnY = getRandomYInLane(selectedLane);
        
        Unit unit = factory.create(type, Unit.Team.PLAYER, spawnX, spawnY, selectedLane);
        if (unit == null) return;

        gold -= cost;
        playerUnits[selectedLane].add(unit);
    }

    public void scheduleNextEnemySpawn() {
        int delay = MIN_ENEMY_SPAWN_DELAY + random.nextInt(MAX_ENEMY_SPAWN_DELAY - MIN_ENEMY_SPAWN_DELAY);
        enemySpawnTimer = new Timer(delay, e -> {
            spawnEnemy();
            enemySpawnTimer.stop();
            scheduleNextEnemySpawn();
        });
        enemySpawnTimer.setRepeats(false);
        enemySpawnTimer.start();
    }

    private void spawnEnemy() {
        int lane = random.nextInt(LANE_COUNT);
        int spawnX = enemyBase.getBounds().x - 60;
        int spawnY = getRandomYInLane(lane);
        
        Unit enemy = factory.randomEnemy(spawnX, spawnY, lane);
        if (enemy != null) {
            enemyUnits[lane].add(enemy);
        }
    }

    private void spawnElite() {
        int lane = random.nextInt(LANE_COUNT);
        int spawnX = enemyBase.getBounds().x - 80;
        int spawnY = getRandomYInLane(lane);
        
        Unit elite = factory.randomElite(spawnX, spawnY, lane);
        if (elite != null) {
            enemyUnits[lane].add(elite);
        }
    }

    private int getRandomYInLane(int lane) {
        int baseY = laneY[lane];
        int laneTop = baseY - LANE_HEIGHT / 4 + 5;
        int laneBottom = baseY + LANE_HEIGHT / 2 - 5;
        return random.nextInt(laneTop, laneBottom);
    }

    private boolean willIntersect(Rectangle a, int stepAx, Rectangle b, int stepBx) {
        if (a.intersects(b)) return true;
        
        Rectangle nextA = new Rectangle(a);
        nextA.x += stepAx;
        Rectangle nextB = new Rectangle(b);
        nextB.x += stepBx;
        
        return nextA.intersects(nextB);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        updateGoldGeneration();
        updateEliteSpawning();
        updateAllLanes();
        checkGameOver();
        
        repaint();
    }

    private void updateGoldGeneration() {
        frameCount++;
        if (frameCount >= (goldGenerationFrames / TIMER_DELAY_MS)) {
            gold += goldPerSecond;
            frameCount = 0;
        }

        if (btnUpgrade != null && goldUpgradeCount < MAX_GOLD_UPGRADES) {
            btnUpgrade.setEnabled(gold >= upgradeCost);
        }
    }

    private void updateEliteSpawning() {
        if (gameTimer != null) {
            elapsedMs += gameTimer.getDelay();
            while (elapsedMs >= nextEliteSpawnMs) {
                spawnElite();
                nextEliteSpawnMs += ELITE_SPAWN_INTERVAL_MS;
            }
        }
    }

    private void updateAllLanes() {
        for (int lane = 0; lane < LANE_COUNT; lane++) {
            updateLane(lane);
        }
    }

    private void updateLane(int lane) {
        ArrayList<Unit> players = playerUnits[lane];
        ArrayList<Unit> enemies = enemyUnits[lane];

        updatePlayerUnits(players, enemies);
        updateEnemyUnits(enemies, players);

        players.removeIf(u -> !u.alive());
        enemies.removeIf(u -> !u.alive());
    }

    private void updatePlayerUnits(ArrayList<Unit> players, ArrayList<Unit> enemies) {
        for (Unit player : players) {
            player.tickCooldown();
            
            Rectangle playerBounds = player.bounds();
            int playerStep = player.nextStep(SPEED_SCALE);
            boolean engaged = false;

            // Check combat with enemies
            for (Unit enemy : enemies) {
                if (!enemy.alive()) continue;
                
                Rectangle enemyBounds = enemy.bounds();
                int enemyStep = enemy.nextStep(SPEED_SCALE);
                
                if (willIntersect(playerBounds, playerStep, enemyBounds, enemyStep)) {
                    engaged = true;
                    if (player.canHit()) player.hit(enemy);
                    if (enemy.canHit()) enemy.hit(player);
                }
            }

            // Check attack on enemy base
            Rectangle baseBounds = enemyBase.getBounds();
            if (!engaged && willIntersect(playerBounds, playerStep, baseBounds, 0)) {
                engaged = true;
                if (player.canHit()) player.hitBase(enemyBase);
            }

            // Move if not engaged
            if (!engaged) {
                player.x += playerStep;
            }
        }
    }

    private void updateEnemyUnits(ArrayList<Unit> enemies, ArrayList<Unit> players) {
        for (Unit enemy : enemies) {
            enemy.tickCooldown();
            
            Rectangle enemyBounds = enemy.bounds();
            int enemyStep = enemy.nextStep(SPEED_SCALE);
            boolean engaged = false;

            // Check combat with players
            for (Unit player : players) {
                if (!player.alive()) continue;
                
                Rectangle playerBounds = player.bounds();
                int playerStep = player.nextStep(SPEED_SCALE);
                
                if (willIntersect(enemyBounds, enemyStep, playerBounds, playerStep)) {
                    engaged = true;
                    if (enemy.canHit()) enemy.hit(player);
                    if (player.canHit()) player.hit(enemy);
                }
            }

            // Check attack on player base
            Rectangle baseBounds = playerBase.getBounds();
            if (!engaged && willIntersect(enemyBounds, enemyStep, baseBounds, 0)) {
                engaged = true;
                if (enemy.canHit()) enemy.hitBase(playerBase);
            }

            // Move if not engaged
            if (!engaged) {
                enemy.x += enemyStep;
            }
        }
    }

    private void checkGameOver() {
        if (playerBase.hp <= 0 || enemyBase.hp <= 0) {
            gameOver = true;
            stopTimers();
            
            String finalMessage = (enemyBase.hp <= 0) ? "You Win!" : "You Lose!";
            parentFrame.showEndScreen(finalMessage);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        drawLanes(g);
        drawBases(g);
        drawUnits(g);
        drawUI(g);
    }

    private void drawLanes(Graphics g) {
        for (int i = 0; i < LANE_COUNT; i++) {
            int y = laneY[i];
            
            // Lane background
            g.setColor(new Color(255, 240, 200));
            g.fillRect(0, y - LANE_HEIGHT / 2, getWidth(), LANE_HEIGHT);
            
            // Lane center line
            g.setColor(new Color(210, 170, 80));
            g.drawLine(0, y, getWidth(), y);
        }

        // Highlight selected lane
        g.setColor(new Color(255, 220, 0, 90));
        g.fillRect(0, laneY[selectedLane] - LANE_HEIGHT / 2, getWidth(), LANE_HEIGHT);
    }

    private void drawBases(Graphics g) {
        playerBase.draw(g);
        enemyBase.draw(g);
    }

    private void drawUnits(Graphics g) {
        for (int lane = 0; lane < LANE_COUNT; lane++) {
            for (Unit player : playerUnits[lane]) {
                player.draw(g);
            }
            for (Unit enemy : enemyUnits[lane]) {
                enemy.draw(g);
            }
        }
    }

    private void drawUI(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        g.drawString("Gold: " + gold + " — Selected Lane: " + (selectedLane + 1), 12, 22);
    }
}