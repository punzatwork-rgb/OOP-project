import java.awt.Image;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class UnitFactory {

    private static final String ASSETS = "C:\\Users\\My computer\\.vscode\\Oop\\Project\\src\\images\\";

    private final Map<UnitType, UnitStats> playerStats = new EnumMap<>(UnitType.class);
    private final Map<UnitType, String>    playerSprite = new EnumMap<>(UnitType.class);
    
    private final Map<UnitType, UnitStats> enemyStats = new EnumMap<>(UnitType.class);
    private final Map<UnitType, String>    enemySprite = new EnumMap<>(UnitType.class);

    private final Random rng = new Random();

    public UnitFactory() {
        // ===== PLAYER UNITS =====
        playerStats.put(UnitType.UNIT1, new UnitStats(50, 100, 2, 100, 40, 50, 1));
        playerSprite.put(UnitType.UNIT1, ASSETS + "unit1.png");

        playerStats.put(UnitType.UNIT2,  new UnitStats( 150, 1, 1, 50, 50, 60, 25));
        playerSprite.put(UnitType.UNIT2,  ASSETS + "unit2.png");

        playerStats.put(UnitType.UNIT3,    new UnitStats(30, 7, 2, 40, 40, 35, 30));
        playerSprite.put(UnitType.UNIT3,    ASSETS + "unit3.png");

        playerStats.put(UnitType.UNIT4,   new UnitStats( 80, 5, 2, 35, 40, 40, 40));
        playerSprite.put(UnitType.UNIT4,   ASSETS + "unit4.png");

        playerStats.put(UnitType.UNIT5,   new UnitStats(140, 10, 1, 60, 60, 60, 50));
        playerSprite.put(UnitType.UNIT5,   ASSETS + "unit5.png");

        // ===== ENEMIES =====
        enemyStats.put(UnitType.ENEMY1, new UnitStats(40, 4, 2, 35, 40, 40, 0));
        enemySprite.put(UnitType.ENEMY1, ASSETS + "enemy1.png");

        enemyStats.put(UnitType.ENEMY2, new UnitStats(80, 2, 1, 50, 40, 50, 0));
        enemySprite.put(UnitType.ENEMY2, ASSETS + "enemy2.png");

        enemyStats.put(UnitType.ELITE1, new UnitStats(10, 30, 3, 150, 40, 40, 0));
        enemySprite.put(UnitType.ELITE1, ASSETS + "elite1.png");

        enemyStats.put(UnitType.ELITE2, new UnitStats(300, 15, 1, 75, 40, 60, 0));
        enemySprite.put(UnitType.ELITE2, ASSETS + "elite2.png");
    }

    public int cost(UnitType type) {
        UnitStats s = playerStats.get(type);
        return (s != null) ? s.cost : 0;
    }

    public Unit create(UnitType type, Unit.Team team, int x, int y, int lane) {
        final UnitStats stats;
        final String spritePath;

        if (team == Unit.Team.PLAYER) {
            stats = playerStats.get(type);
            spritePath = playerSprite.get(type);
        } else {
            stats = enemyStats.get(type);
            spritePath = enemySprite.get(type);
        }
        if (stats == null) return null;

        Image img = (spritePath != null) ? SpriteStore.get(spritePath) : null;
        Unit u = new Unit(x, y, team, stats, img);
        u.lane = lane;
        return u;
    }

    public Unit randomEnemy(int x, int y, int lane) {
        UnitType t = rng.nextBoolean() ? UnitType.ENEMY1 : UnitType.ENEMY2;
        return create(t, Unit.Team.ENEMY, x, y, lane);
    }

    public Unit randomElite(int x, int y, int lane) {
        UnitType t = rng.nextBoolean() ? UnitType.ELITE1 : UnitType.ELITE2;
        return create(t, Unit.Team.ENEMY, x, y, lane);
    }
}
