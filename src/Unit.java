import java.awt.*;

public class Unit {
    public enum Team { PLAYER, ENEMY }

    public int x, y;            
    public int lane = -1;

    private final Team team;
    private final UnitStats stats;
    private final Image sprite;

    private int hp;
    private int cd = 0;        

    public Unit(int x, int y, Team team, UnitStats stats, Image sprite) {
        this.x = x; this.y = y;
        this.team = team;
        this.stats = stats;
        this.sprite = sprite;
        this.hp = stats.hp;
    }

    public Team getTeam() { return team; }
    public boolean alive() { return hp > 0; }
    public int width()  { return stats.width; }
    public int height() { return stats.height; }
    public Rectangle bounds() { return new Rectangle(x, y - height(), width(), height()); }
    public int cost() { return stats.cost; }

    public void tickCooldown() { if (cd > 0) cd--; }
    public boolean canHit() { return cd == 0; }
    public void onHit() { cd = stats.attackInterval; }

    public int nextStep(double speedScale) {
        int base = Math.max(1, (int)Math.round(stats.speed * speedScale));
        return (team == Team.PLAYER) ? +base : -base;
    }

    public void hit(Unit other) {
        if (!canHit()) return;
        other.hp -= Math.max(1, stats.damage);
        onHit();
    }

    public void hitBase(Base base) {
        if (!canHit()) return;
        base.hp -= Math.max(1, stats.damage);
        onHit();
    }

    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y - height(), width(), height(), null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(x, y - height(), width(), height());
        }
        
        g.setColor(Color.BLACK);
        g.drawRect(x, y - height() - 7, width(), 6);
        int hpw = (int)Math.max(0, Math.round((hp / (double)stats.hp) * width()));
        g.setColor(new Color(0,180,60));
        g.fillRect(x+1, y - height() - 6, Math.max(0, hpw-2), 5);
    }
}
