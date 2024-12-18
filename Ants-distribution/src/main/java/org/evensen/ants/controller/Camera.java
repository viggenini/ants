package org.evensen.ants.controller;

public class Camera {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public Camera(final double minX, final double maxX, final double minY, final double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public Camera(final double width, final double height) {
        this(0.0, width, 0.0, height);
    }

    public void setCoordinateSpaceOf(final javafx.scene.canvas.GraphicsContext g) {
        g.scale(1.0 / getWidth(), 1.0 / getHeight());
        g.translate(offsetX(), offsetY());
    }

    public void move(final double x, final double y) {
        this.minX += x;
        this.maxX += x;
        this.minY += y;
        this.maxY += y;
    }

    public void zoom(final double scale) {
        zoom(scale, scale);
    }

    public void zoom(final double x, final double y) {
        this.minX *= x;
        this.maxX *= x;
        this.minY *= y;
        this.maxY *= y;
    }

    public double getWidth() {
        return this.maxX - this.minX;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public double offsetX() {
        return this.minX;
    }

    public double offsetY() {
        return this.minY;
    }
}
