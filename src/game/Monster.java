package game;

public class Monster {
    private final String name;
    private int health;
    private final int attackDamage;
    private final boolean boss;

    public Monster(String name, int health, int attackDamage, boolean boss) {
        this.name = name;
        this.health = health;
        this.attackDamage = attackDamage;
        this.boss = boss;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public boolean isBoss() {
        return boss;
    }

    public void damage(int amount) {
        health = Math.max(0, health - amount);
    }

    public boolean isDead() {
        return health <= 0;
    }
}


