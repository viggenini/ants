package org.evensen.ants.controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

/**
 * <p>
 * A {@code ResizableCanvas} is like a regular {@code Canvas} but implements the methods required in order to be
 * notified on size changes during layout and will resize itself accordingly.
 * </p>
 */
public class ResizeableCanvas extends Canvas {
    private final double aspectRatio;
    private final Camera camera;

    /**
     * Creates a {@code ResizableCanvas} with the given size. Even as the canvas is resized the aspect ratio of the
     * drawable region will <b>always</b> maintain the ratio between {@code width} and {@code height}.
     *
     * @param width Initial width of the canvas. Also used to calculate the aspect ratio.
     * @param height Initial height of the canvas. Also used to calculate the aspect ratio.
     */
    public ResizeableCanvas(double width, double height) {
        super(width, height);
        this.aspectRatio = width / height;
        this.camera = new Camera(1.0, 1.0);
        resize(width, height);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Camera.
    // -----------------------------------------------------------------------------------------------------------------

    public Camera getCamera() {
        return this.camera;
    }

    public double canvasCoordinateX(final double nodeX) {
        if (this.aspectRatio / (getWidth() / getHeight()) > 1.0) {
            return nodeX / getWidth() * this.camera.getWidth() - this.camera.offsetX();
        } else {
            //noinspection OverlyComplexArithmeticExpression
            return nodeX
                    / getWidth()
                    / this.aspectRatio
                    * (getWidth() / getHeight())
                    * this.camera.getWidth()
                    - this.camera.offsetX();
        }
    }

    public double canvasCoordinateY(final double nodeY) {
        if (this.aspectRatio / (getWidth() / getHeight()) > 1.0) {
            //noinspection OverlyComplexArithmeticExpression
            return nodeY
                    / getHeight()
                    / (1.0 / this.aspectRatio)
                    * (getHeight() / getWidth())
                    * this.camera.getHeight()
                    - this.camera.offsetY();
        } else {
            return nodeY / getHeight() * this.camera.getHeight() - this.camera.offsetY();
        }
    }

    public CameraController createCameraController() {
        return new CameraController(this, this.aspectRatio);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Making the view resizeable.
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(final double width, final double height) {
        setWidth(width);
        setHeight(height);

        final GraphicsContext g = getGraphicsContext2D();
        g.setTransform(new Affine());
        g.scale(width, height);
        g.scale(Math.min(1.0, this.aspectRatio / (width / height)),
                Math.min(1.0, (1.0 / this.aspectRatio) / (height / width)));

    }

    @Override
    public double minHeight(double width) {
        return 1.0;
    }

    @Override
    public double maxHeight(double width) {
        return Double.MAX_VALUE;
    }

    @Override
    public double prefHeight(double width) {
        return width * (1.0 / this.aspectRatio);
    }

    @Override
    public double minWidth(double height) {
        return 1.0;
    }

    @Override
    public double maxWidth(double height) {
        return Double.MAX_VALUE;
    }

    public double prefWidth(double height) {
        return height * this.aspectRatio;
    }
}
