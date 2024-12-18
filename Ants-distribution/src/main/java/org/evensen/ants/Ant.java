package org.evensen.ants;

public interface Ant {
    public void move(final AntWorld w);

    public float getDirection();

    public Position getPosition();

    public boolean hasFood();

    public void damage(int strength);

    public boolean isDead();

}
