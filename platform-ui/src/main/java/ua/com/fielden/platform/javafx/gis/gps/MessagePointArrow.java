package ua.com.fielden.platform.javafx.gis.gps;

import java.awt.geom.Point2D;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import ua.com.fielden.platform.javafx.gis.IPoint;

/**
 * An "arrow" implementation of {@link MessagePoint} view for moving machine.
 * 
 * @author Developers
 * 
 */
public class MessagePointArrow extends Polygon implements IMessagePointNode {
    private final MessagePointNodeMixin mixin;

    public MessagePointArrow(final IPoint<MessagePoint> contract, final MessagePoint messagePoint, final double initialHalfSize, final int initialZoom, final Scene scene, final Group path) {
        super();

        this.mixin = new MessagePointNodeMixin(this, contract, messagePoint, initialHalfSize, initialZoom, 0.1, scene, Color.GREEN, path);

        final double x1 = this.mixin.initialSize();
        getPoints().clear();
        final double y1 = 5 * x1;
        final double y2 = 1.3 * y1;
        getPoints().addAll(new Double[] { 0.0, 0.0, x1, 0.0, x1, y1, 2 * x1, y1, 0.0, y2, -2 * x1, y1, -x1, y1, -x1, 0.0 });
        setTranslateY(-y2 / 2.0);
    }

    @Override
    public double getSize() {
        return mixin.getSize();
    }

    @Override
    public void updateSizesAndCentre(final double x1) {
        mixin.updateSizesAndCentre(x1);
    }

    @Override
    public MessagePoint messagePoint() {
        return mixin.messagePoint();
    }

    @Override
    public void updateTooltip() {
        mixin.updateTooltip();
    }

    @Override
    public void updateColor() {
        mixin.updateColor();
    }

    @Override
    public void clickedAction() {
        mixin.clickedAction();
    }

    @Override
    public void select() {
        mixin.select();
    }

    @Override
    public boolean selected() {
        return mixin.selected();
    }

    @Override
    public void unselect() {
        mixin.unselect();
    }

    @Override
    public void update() {
        mixin.update();
    }

    @Override
    public void setZoom(final int zoom) {
        mixin.setZoom(zoom);
    }

    @Override
    public void updateDirection(final double vectorAngle) {
        mixin.updateDirection(vectorAngle);
    }

    @Override
    public void makeVisibleAndUpdate(final Point2D xY, final Group parent, final int zoom) {
        mixin.makeVisibleAndUpdate(xY, parent, zoom);
    }

    @Override
    public void createAndAddCallout(final Group parent) {
        mixin.createAndAddCallout(parent);
    }

    @Override
    public void closeCallout() {
        mixin.closeCallout();
    }

    @Override
    public void makeInvisible() {
        mixin.makeInvisible();
    }

    @Override
    public void add(final Group parent) {
        mixin.add(parent);
    }
}
