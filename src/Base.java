import java.awt.*;

public class Base {
    int x, y, width, height, hp;
    Color color;

    public Base(int x, int y, int width, int height, int hp, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.color = color;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawString("HP: " + hp, x + 5, y - 5);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
