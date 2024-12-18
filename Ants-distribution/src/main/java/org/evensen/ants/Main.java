package org.evensen.ants;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Affine;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.evensen.ants.controller.Camera;
import org.evensen.ants.controller.CameraController;
import org.evensen.ants.controller.ResizeableCanvas;
import org.evensen.ants.render.AntColonyRenderer;
import org.evensen.ants.render.AntWorldRenderer;
import org.evensen.ants.render.FoodRenderer;
import org.evensen.ants.render.ObstacleRenderer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {
    private static final int WORLD_WIDTH = 400;
    private static final int WORLD_HEIGHT = 200;
    private static final int ANTS = 10000;
    private static final float PHEROMONE_RATE = 0.7f;
    private static final long MODEL_UPDATE_INTERVAL = 10_000_000L; // Update every 10 ms.
    private static final long VIEW_UPDATE_INTERVAL = 40_000_000L; // Update every 25 ms.
    private static final double MARGIN = 20.0;
    private static final int FOOD_SOURCES = Math.max(2, (WORLD_WIDTH * WORLD_HEIGHT / 50000));

    private final AntWorld world;
    //
    // Pencil radius is 2% of the world size.
    //
    private static final float PENCIL_RADIUS = ((WORLD_WIDTH * 0.02f) + (WORLD_HEIGHT * 0.02f)) / 4.0f;


    private final AntColony colony;
    private final boolean drawAnts;
    private final ObstacleRenderer obstacleRenderer;
    private final FoodRenderer foodRenderer;
    private long epochCounter;
    private long lastModelUpdate;
    private long lastViewUpdate;
    private int frame;
    private long lastFrameReset;
    private final AtomicBoolean modelShouldBeRunning;
    private Collection<AntHazard> hazards;

    public Main() {
        // Create a new world with size WORLD_WIDTH * WORLD_HEIGHT and 4 food sources.
        this.world = new MyAntWorld(WORLD_WIDTH, WORLD_HEIGHT, 4);

        this.colony = new AntColony(ANTS, PHEROMONE_RATE, this.world);
        this.drawAnts = true;
        this.obstacleRenderer = new ObstacleRenderer();
        this.foodRenderer = new FoodRenderer();
        this.epochCounter = 1;
        this.hazards = new LinkedList<>();
        this.hazards.add(new AntHazard() {
            private int x = WORLD_WIDTH / 2;
            private int y = WORLD_HEIGHT / 2;

            @Override
            public void update(final AntWorld w) {
                // this.x = (this.x + 1) % WORLD_WIDTH;
            }

            @Override
            public Position getPosition() {
                return new Position(this.x, this.y);
            }

            @Override
            public float getRadius() {
                return 10;
            }

            @Override
            public void damage(final Ant a) {
                if (!a.isDead()) {
                    System.out.println("Hit at " + a.getPosition());
                    a.damage(10);
                }
            }
        });
        this.modelShouldBeRunning = new AtomicBoolean(false);
    }

    private void startModelTimer() {
        final Timer timer = new Timer();
        this.modelShouldBeRunning.set(true);

        final TimerTask modelUpdate = new java.util.TimerTask() {

            @Override
            public void run() {
                if (Main.this.modelShouldBeRunning.get()) {
                    final long now = System.nanoTime();
                    final long elapsedModelNanos = now - Main.this.lastModelUpdate;
                    if (MODEL_UPDATE_INTERVAL < elapsedModelNanos) {
                        synchronized (Main.this.colony) {
                            Main.this.colony.updateAnts(Main.this.world);
                        }
                        if (0 == Main.this.epochCounter % 5) {
                            Main.this.world.dispersePheromones();
//                            Main.this.obstacleRenderer.dirty();
                        }
                        //   updateHazards();
                        Main.this.lastModelUpdate = now;
                        Main.this.epochCounter++;
                    }
                } else {
                    cancel();
                }
            }
        };

        timer.scheduleAtFixedRate(modelUpdate, 0, MODEL_UPDATE_INTERVAL / 1_000_000);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        // Build a scene graph
        final ResizeableCanvas canvas = new ResizeableCanvas(this.world.getWidth(), this.world.getHeight());
        AnchorPane.setTopAnchor(canvas, 0.0);
        AnchorPane.setBottomAnchor(canvas, 0.0);
        AnchorPane.setLeftAnchor(canvas, 0.0);
        AnchorPane.setRightAnchor(canvas, 0.0);
        final StackPane root = new StackPane(new AnchorPane(canvas));
        root.setPadding(new Insets(MARGIN));

        final GraphicsContext g = canvas.getGraphicsContext2D();
        final Camera camera = canvas.getCamera();
        final CameraController cameraController = canvas.createCameraController();

        startModelTimer();

        final EventHandler<MouseEvent> mouseEventHandler = (event) -> {

            //
            // Transform into world coordinates.
            //
            final float worldX = (float) canvas.canvasCoordinateX(event.getX()) * WORLD_WIDTH;
            final float worldY = (float) canvas.canvasCoordinateY(event.getY()) * WORLD_HEIGHT;
            final Position p = new Position(worldX, worldY);

            //
            // Add/erase points within the radius.
            //
            for (float x = -PENCIL_RADIUS; PENCIL_RADIUS > x; x += 1.0f) {
                for (float y = -PENCIL_RADIUS; PENCIL_RADIUS > y; y += 1.0f) {
                    if (PENCIL_RADIUS * PENCIL_RADIUS > x * x + y * y) {
                        Main.this.world.setObstacle(p.offset(x, y), !event.isShiftDown());
                    }
                }
            }

            //
            // Mark obstacle renderer as dirty.
            //
            Main.this.obstacleRenderer.dirty();
        };

        canvas.setOnMouseDragged(mouseEventHandler);
        canvas.setOnMouseClicked(mouseEventHandler);

        // Create a timer
        final AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            @Override
            public void handle(final long now) {
                final long elapsedModelNanos = now - Main.this.lastModelUpdate;
                final long elapsedViewNanos = now - Main.this.lastViewUpdate;
                if (VIEW_UPDATE_INTERVAL < elapsedViewNanos) {
                    ++Main.this.frame;
                    if (1_000_000_000 < now - Main.this.lastFrameReset) {
                        final float antEfficiency = (float) (
                                Main.this.world.getFoodCount() / (float) Main.this.epochCounter / ANTS *
                                        Math.hypot(Main.this.world.getWidth(), Main.this.world.getHeight()));
                        System.out.println(
                                "frames per second: " + Main.this.frame + ", ant efficiency: " + antEfficiency + " (" +
                                        Main.this.epochCounter + " epochs)");
                        Main.this.frame = 0;
                        Main.this.lastFrameReset = now;
                    }

                    //
                    // Reset canvas transform so that it can be ensured that every pixel is cleared.
                    //
                    g.save();
                    g.setTransform(new Affine());
                    g.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
                    g.restore();

                    g.save();
                    cameraController.step();
                    camera.setCoordinateSpaceOf(g);

                    //
                    // Remap the drawing coordinates from [0.0, 1.0] to the world coordinates [0, WORLD_WIDTH]
                    // and [0.0, WORLD_HEIGHT].
                    //
                    g.scale(1.0 / WORLD_WIDTH, 1.0 / WORLD_HEIGHT);
                    // Main.this.camera.adjust(g, WORLD_WIDTH, WORLD_HEIGHT);

                    //
                    // Draw background.
                    //
                    g.setFill(javafx.scene.paint.Color.BEIGE.darker().saturate());
                    g.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

                    //
                    // Draw world and ants.
                    //
                    g.save();
                    g.setGlobalAlpha(0.75);

                    synchronized (Main.this.world) {
                        Main.this.obstacleRenderer.render(g, Main.this.world);
                        g.restore();
                        AntWorldRenderer.render(g, Main.this.world);
                        Main.this.foodRenderer.dirty();
                        Main.this.foodRenderer.render(g, Main.this.world);
                        AntColonyRenderer.render(g, Main.this.colony);
                    }

                    g.restore();
                    Main.this.lastViewUpdate = now;
                }
            }
        };

        final Scene scene = new Scene(root);

        scene.setOnKeyPressed(cameraController.getKeyPressedEventHandler());
        scene.setOnKeyReleased(cameraController.getKeyReleasedEventHandler());
        canvas.setOnScroll(cameraController.getScrollEventHandler());

        stage.setScene(scene);
        stage.setTitle("Pelles & Felix myrfarm");
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        float scale = Math.min((float) (screenBounds.getWidth() - 2 * MARGIN) / WORLD_WIDTH,
                (float) (screenBounds.getHeight() - 2 * MARGIN) / WORLD_HEIGHT) * 0.5f;
        stage.setWidth(WORLD_WIDTH * scale + 2 * MARGIN);
        stage.setHeight(WORLD_HEIGHT * scale + 2 * MARGIN);
        stage.setOnCloseRequest(event -> this.modelShouldBeRunning.set(false));
        stage.show();

        this.lastViewUpdate = System.nanoTime() + 500_000_000L;
        this.lastModelUpdate = System.nanoTime() + 5_000_000_000L;
        timer.start();  // Start simulation
    }

    private void updateHazards() {
        synchronized (this.colony) {
            for (AntHazard h : this.hazards) {
                h.update(this.world);

                for (Ant a : this.colony.getAnts()) {
                    if (a.getPosition().isWithinRadius(h.getPosition(), h.getRadius())) {
                        h.damage(a);
                    }
                }
            }
        }
    }
}