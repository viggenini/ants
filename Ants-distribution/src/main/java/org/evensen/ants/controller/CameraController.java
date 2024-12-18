package org.evensen.ants.controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Offers input event handlers that transforms user input into smooth camera movement.
 */
public class CameraController {
    private static final double FRICTION_TRANSLATE = 0.89;
    private static final double ZOOM_SPEED = 0.0075;
    private static final double TRANSLATION_SPEED = 0.003;
    private final ResizeableCanvas canvas;
    private double translateDeltaX;
    private double translateDeltaY;
    private double translateX;
    private double translateY;
    private final double aspectRatio;

    /**
     * Initializes a {@code CameraController} with a certain camera as target.
     *
     * <h2>NOTE:</h2>
     * <p>
     * Do not call this method directly. Instead use {@code ResizeableCanvas::createCameraController}.
     * </p>
     *
     * @param canvas Parent {@code ResizeableCanvas}.
     */
    public CameraController(final ResizeableCanvas canvas, final double aspectRatio) {
        this.canvas = canvas;
        this.translateDeltaX = 0.0;
        this.translateDeltaY = 0.0;
        this.translateX = 0.0;
        this.translateY = 0.0;
        this.aspectRatio = aspectRatio;
    }

    /**
     * Call {@code step} once each frame to update the camera position and zoom level.
     */
    public void step() {
        this.translateX += this.translateDeltaX * TRANSLATION_SPEED * (1.0 / this.aspectRatio);
        this.translateX *= FRICTION_TRANSLATE;
        this.translateY += this.translateDeltaY * TRANSLATION_SPEED;
        this.translateY *= FRICTION_TRANSLATE;
        this.canvas.getCamera().move(this.translateX, this.translateY);
    }

    /**
     * Attach this handler to listen after the key presses in a scene.
     *
     * @return A key pressed handler.
     */
    public EventHandler<KeyEvent> getKeyPressedEventHandler() {
        return event -> {
            switch (event.getCode()) {
                /* -- Works well with Java 14 and upwards.
                case W -> CameraController.this.translateDeltaY = 1.0;
                case S -> CameraController.this.translateDeltaY = -1.0;
                case A -> CameraController.this.translateDeltaX = 1.0;
                case D -> CameraController.this.translateDeltaX = -1.0;
                 */
                case W:
                case UP:
                    CameraController.this.translateDeltaY = 1.0;
                    break;
                case S:
                case DOWN:
                    CameraController.this.translateDeltaY = -1.0;
                    break;
                case A:
                case LEFT:
                    CameraController.this.translateDeltaX = 1.0;
                    break;
                case D:
                case RIGHT:
                    CameraController.this.translateDeltaX = -1.0;
                    break;
            }
        };
    }

    /**
     * Attach this handler to listen after the key releases in a scene.
     *
     * @return A key release handler.
     */
    public EventHandler<KeyEvent> getKeyReleasedEventHandler() {
        return event -> {
            switch (event.getCode()) {
                case W:
                case S:
                case UP:
                case DOWN:
                    CameraController.this.translateDeltaY = 0.0;
                    break;
                case A:
                case D:
                case LEFT:
                case RIGHT:
                    CameraController.this.translateDeltaX = 0.0;
                    break;
            }
        };
    }

    /**
     * Attach this handler to listen after the scroll events in a {@code ResizeableCanvas}.
     *
     * @return A scroll event handler.
     */
    public EventHandler<ScrollEvent> getScrollEventHandler() {
        return event -> {
            final double dx = this.canvas.canvasCoordinateX(event.getX());
            final double dy = this.canvas.canvasCoordinateY(event.getY());
            this.canvas.getCamera().move(dx, dy);
            this.canvas.getCamera().zoom(1.0 - event.getDeltaY() * ZOOM_SPEED);
            this.canvas.getCamera().move(-dx, -dy);
        };
    }
}
