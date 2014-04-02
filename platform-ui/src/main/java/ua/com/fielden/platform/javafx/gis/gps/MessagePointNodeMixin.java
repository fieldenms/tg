package ua.com.fielden.platform.javafx.gis.gps;

import java.awt.geom.Point2D;

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import ua.com.fielden.platform.javafx.gis.Callout;
import ua.com.fielden.platform.javafx.gis.IPoint;

/**
 * 
 * An "mixin" implementation of {@link MessagePoint} view for machine's message.
 * 
 * @author Developers
 * 
 */
public class MessagePointNodeMixin implements IMessagePointNode {
    private final static double SCALE = 1.4;

    private final Shape referenceNode;

    private final double initialHalfSize;
    private final int initialZoom;

    private final double hoverDeltaSize;

    private final MessagePoint messagePoint;
    private final IPoint<MessagePoint> contract;
    private final double initialSize;

    // private Tooltip tooltip;
    private boolean selected = false;
    private int zoom;

    private final Scene scene;

    private final Color selectedColor;
    private final Group path;
    private Callout currentCallout;

    public MessagePointNodeMixin(final Shape referenceNode, final IPoint<MessagePoint> contract, final MessagePoint messagePoint, final double initialHalfSize, final int initialZoom, final double hoverDeltaSize, final Scene scene, final Color selectedColor, final Group path) {
        if (scene == null) {
            throw new NullPointerException();
        }
        this.path = path;
        this.scene = scene;
        this.hoverDeltaSize = hoverDeltaSize;
        this.referenceNode = referenceNode;
        this.initialHalfSize = initialHalfSize;
        this.initialZoom = initialZoom;
        this.selectedColor = selectedColor;

        this.messagePoint = messagePoint;
        this.contract = contract;
        this.initialSize = getSize();

        ////////////////////////// MOUSE PRESSED //////////////////////////
        referenceNode().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                referenceNode().setEffect(new InnerShadow(BlurType.GAUSSIAN, contract.getColor(messagePoint), 15, 0, 0, 0));
            }
        });
        referenceNode().setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                referenceNode().setEffect(null);
                clickedAction();
            }
        });

        ////////////////////////// MOUSE HOVERED //////////////////////////
        referenceNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                updateTooltip();

                updateSizesAndCentre(getSize() - MessagePointNodeMixin.this.hoverDeltaSize);
                referenceNode().setStrokeWidth(1.0);
            }
        });
        referenceNode().setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                updateSizesAndCentre(getSize() + MessagePointNodeMixin.this.hoverDeltaSize);
                referenceNode().setStrokeWidth(0.0);
            }
        });
    }

    protected Shape referenceNode() {
        return referenceNode;
    }

    private static double tranformByZoom(final int zoom, final double initialHalfSize, final int initialZoom) {
        final double scale = initialHalfSize / initialZoom;
        return zoom * scale + Math.abs(zoom - initialZoom);
    }

    private static double getSize(final int zoom, final double initialHalfSize, final int initialZoom) {
        return zoom < initialZoom ? tranformByZoom(initialZoom, initialHalfSize, initialZoom) : tranformByZoom(zoom, initialHalfSize, initialZoom);
    }

    @Override
    public double getSize() {
        return getSize(zoom, initialHalfSize, initialZoom);
    }

    @Override
    public void updateSizesAndCentre(final double x0) {
        final double x1 = selected ? x0 * SCALE : x0;

        referenceNode().setScaleX(x1 / initialSize);
        referenceNode().setScaleY(x1 / initialSize);
    }

    @Override
    public MessagePoint messagePoint() {
        return messagePoint;
    }

    @Override
    public void updateTooltip() {
        //	if (tooltip == null) {
        //	    tooltip = new Tooltip();
        //	    // tooltip.setAutoHide(false);
        //	}
        //	// HEAVY-WEIGHT!
        //	tooltip.setText(contract.getTooltip(messagePoint));
        //	Tooltip.install(referenceNode(), tooltip);
    }

    @Override
    public void updateColor() {
        if (selected && selectedColor != null) {
            updateColor(selectedColor);
        } else {
            updateColor(contract.getColor(messagePoint));
        }
    }

    private void updateColor(final javafx.scene.paint.Color color) {
        referenceNode().setFill(new RadialGradient(225, 0.5, 0, 0, getSize(), false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.WHITE), new Stop(1.0, color)));
        referenceNode().setStrokeWidth(0.0);
        referenceNode().setStroke(color.darker());
    }

    @Override
    public void clickedAction() {
        contract.clickedAction(messagePoint);
    }

    @Override
    public void select() {
        if (!selected) {
            selected = true;

            // bring to front
            path.getChildren().remove(referenceNode);

            updateSizesAndCentre(getSize());
            updateColor();

            path.getChildren().add(referenceNode);

            updateTooltip();
        } else {
            // TODO
        }
    }

    @Override
    public void createAndAddCallout(final Group path) {
        final Group infoNode = new Group();
        final Text textNode = new Text(contract.getTooltip(messagePoint).trim());
        textNode.setFill(Color.BLACK);
        // textNode.setStroke(Color.BLACK);
        // textNode.setStrokeWidth(1);
        textNode.textOriginProperty().set(VPos.TOP);
        infoNode.getChildren().add(textNode);

        currentCallout = new Callout(referenceNode(), infoNode, scene, path, createCalloutCloseAction());
        path.getChildren().add(currentCallout);
    }

    @Override
    public void unselect() {
        if (selected) {
            selected = false;

            updateSizesAndCentre(getSize());
            updateColor();
        }
    }

    @Override
    public void closeCallout() {
        if (currentCallout != null) {
            currentCallout.close();
            currentCallout = null;
        }
    }

    private Runnable createCalloutCloseAction() {
        return new Runnable() {
            @Override
            public void run() {
                contract.turnOffCallout();
            }
        };
    }

    @Override
    public void update() {
        updateSizesAndCentre(getSize());
        updateColor();
        updateDirection(messagePoint().getVectorAngle());
    }

    @Override
    public void updateDirection(final double vectorAngle) {
        referenceNode().setRotate(vectorAngle - 180);
    }

    @Override
    public void setZoom(final int zoom) {
        this.zoom = zoom;
    }

    protected double initialSize() {
        return initialSize;
    }

    public static javafx.scene.paint.Color teltonikaColor(final MessagePoint start) {
        return start.getSpeed() == 0 ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN;
    }

    @Override
    public void makeVisibleAndUpdate(final Point2D xY, final Group parent, final int zoom) {
        setZoom(zoom);
        update();

        referenceNode.setLayoutX(xY.getX());
        referenceNode.setLayoutY(xY.getY());

        referenceNode.setVisible(true);
    }

    @Override
    public void add(final Group parent) {
        parent.getChildren().add(referenceNode);
    }

    @Override
    public void makeInvisible() {
        referenceNode.setVisible(false);
    }

    @Override
    public boolean selected() {
        return selected;
    }
}