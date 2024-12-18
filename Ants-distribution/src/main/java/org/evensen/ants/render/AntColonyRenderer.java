package org.evensen.ants.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.evensen.ants.AntColony;
import org.evensen.ants.Ant;

import java.util.List;
import java.util.ArrayList;


public enum AntColonyRenderer {
    ;
    private static final Color FORAGING_ANT_COLOR = Color.color(0.4, 0.4, 1.0, 0.5);
    private static final Color FOOD_CARRYING_ANT_COLOR = Color.color(0.7, 0.7, 0.0, 0.5);
    private static final Color DEAD_ANT_COLOR = Color.color(0.8, 0.0, 0.0, 0.5);
    private static long renderingTime = 0;
    private static long renders = 0;

    public static void render(final GraphicsContext g, final AntColony colony) {
        long startTime = System.nanoTime();
        renders++;
        g.save();

        g.setLineWidth(0.5);
        g.setFill(Color.BLACK);
        List<Ant> colonyView = new ArrayList<>(colony.getAnts());

        for (final Ant ant : colonyView) {
            final float antX1 = (float) (Math.cos(ant.getDirection()) + ant.getPosition().getX());
            final float antX2 = (float) (Math.cos(ant.getDirection() + Math.PI) + ant.getPosition().getX());
            final float antY1 = (float) (Math.sin(ant.getDirection()) + ant.getPosition().getY());
            final float antY2 = (float) (Math.sin(ant.getDirection() + Math.PI) + ant.getPosition().getY());
            Color tailColor;
            if(ant.isDead()) {
                tailColor = DEAD_ANT_COLOR;
            } else {
                tailColor = ant.hasFood() ? FOOD_CARRYING_ANT_COLOR : FORAGING_ANT_COLOR;
            }
            g.setStroke(tailColor);
            g.strokeLine(antX1, antY1, antX2, antY2);
            g.fillOval((antX1 - 0.5f), (antY1 - 0.5f), 1, 1);
        }
        g.restore();
        long lastRenderingTime = (System.nanoTime() - startTime);
        renderingTime += lastRenderingTime;
        /*
        System.out.println(
                "Mean ant rendering time: " + renderingTime / 1_000_000_000.0 / renders + "\tlast render time: " +
                        lastRenderingTime / 1_000_000_000.0);

         */
    }
}
