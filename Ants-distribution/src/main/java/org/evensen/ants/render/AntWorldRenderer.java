package org.evensen.ants.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;

public enum AntWorldRenderer {
    ;
    private static final int HOME_COLOR = convertToARGB(Color.color(1.0, 0.2, 0.1, 0.8));
    private static final int BASE_FOOD_CARRYING_SCENT_COLOR = convertToBaseRGB(Color.color(0.8, 0.8, 0.0));
    private static final int BASE_FORAGING_SCENT_COLOR = convertToBaseRGB(Color.color(0.0, 0.4, 0.7));

    private static int convertToBaseRGB(Color color) {
        return ((int) Math.round(255.0 * color.getRed()) << 16)
                | ((int) Math.round(255.0 * color.getGreen()) << 8)
                | ((int) Math.round(255.0 * color.getBlue()));
    }

    private static int convertToARGB(Color color) {
        return ((int) Math.round(255.0 * color.getOpacity()) << 24)
                | ((int) Math.round(255.0 * color.getRed()) << 16)
                | ((int) Math.round(255.0 * color.getGreen()) << 8)
                | ((int) Math.round(255.0 * color.getBlue()));
    }

    private static int pheromoneAlpha(float strength) {
        return Math.min(255, (Math.round(org.evensen.ants.GraphicsMath.bias(strength * 15f, 0.97f) * 255.0f))) << 24;
    }

    public static void render(GraphicsContext g, org.evensen.ants.AntWorld world) {
        g.save();
     //   g.setGlobalBlendMode(BlendMode.ADD);

        final int[] foodPheromonePixels = new int[world.getWidth() * world.getHeight()];
        final int[] foragingPheromonePixels = new int[world.getWidth() * world.getHeight()];
        final int[] homePixels = new int[world.getWidth() * world.getHeight()];
        int i = 0;
        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                final org.evensen.ants.Position p = new org.evensen.ants.Position(x, y);
                foodPheromonePixels[i] = BASE_FOOD_CARRYING_SCENT_COLOR | pheromoneAlpha(world.getFoodStrength(p));
                foragingPheromonePixels[i] = BASE_FORAGING_SCENT_COLOR | pheromoneAlpha(world.getForagingStrength(p));
                homePixels[i] = world.isHome(p) ? HOME_COLOR : 0x000000;
                ++i;
            }
        }

        for (int[] pixels : List.of(
                foragingPheromonePixels,
                foodPheromonePixels,
                homePixels)) {
            WritableImage raster = new WritableImage(world.getWidth(), world.getHeight());
            PixelWriter pixelWriter = raster.getPixelWriter();
            pixelWriter.setPixels(
                    0,
                    0,
                    world.getWidth(),
                    world.getHeight(),
                    PixelFormat.getIntArgbInstance(),
                    pixels,
                    0,
                    world.getWidth());
            g.drawImage(raster, 0, 0);
        }

        g.restore();
    }
}