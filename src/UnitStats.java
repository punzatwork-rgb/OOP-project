public final class UnitStats {
    public final int hp;
    public final int damage;
    public final int speed;            
    public final int attackInterval;   
    public final int width, height;
    public final int cost;             

    public UnitStats(int hp, int damage, int speed, int attackInterval, int width, int height, int cost) {
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.width = width;
        this.height = height;
        this.cost = cost;
    }
}
