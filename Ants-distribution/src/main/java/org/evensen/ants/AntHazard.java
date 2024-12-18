package org.evensen.ants;

public interface AntHazard {
    void update(AntWorld w);
    Position getPosition();
    float getRadius();

    void damage(Ant a);
}
