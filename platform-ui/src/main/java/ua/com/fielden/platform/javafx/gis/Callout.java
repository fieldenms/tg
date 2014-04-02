package ua.com.fielden.platform.javafx.gis;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * A javaFx node to represent "callout" for some other node. It may contain any description or images that should be embedded in arbitrary node.
 * 
 * @author TG Team
 * 
 */
public class Callout extends Group {
    private final Node originationNode;
    private final Group informationNode;
    private final Scene scene;
    private final Group parentGroup;
    private final Runnable closeAction;

    public Callout(final Node originationNode, final Group informationNode, final Scene scene, final Group parentGroup, final Runnable closeAction) {
        this.closeAction = closeAction;
        this.parentGroup = parentGroup;
        this.originationNode = originationNode;
        this.informationNode = informationNode;
        this.scene = scene;

        setLayoutX(this.originationNode.getLayoutX()); // callout knows exact place where it should be
        setLayoutY(this.originationNode.getLayoutY()); // TODO should be based on originationNode place in context of scene viewport

        final Position position = determinePosition();
        final WrapperRectangle wr = new WrapperRectangle(this.informationNode, position, this);
        getChildren().add(wr);
    }

    private Position determinePosition() {
        final double x = this.originationNode.getLayoutX();
        final double y = this.originationNode.getLayoutY();
        final double halfW = scene.getWidth() / 2.0;
        final double halfH = scene.getHeight() / 2.0;
        return (x <= halfW && y <= halfH) ? mirror(Position.NORTH_WEST) : (halfW <= x && y <= halfH) ? mirror(Position.NORTH_EAST)
                : (halfW <= x && halfH <= y) ? mirror(Position.SOUTH_EAST) : mirror(Position.SOUTH_WEST);
    }

    private static Position mirror(final Position position) {
        switch (position) {
        case SOUTH_WEST:
            return Position.NORTH_EAST;
        case SOUTH_EAST:
            return Position.NORTH_WEST;
        case NORTH_WEST:
            return Position.SOUTH_EAST;
        case NORTH_EAST:
            return Position.SOUTH_WEST;
        default:
            return null;
        }
    }

    public Group getParentGroup() {
        return parentGroup;
    }

    public Runnable getCloseAction() {
        return closeAction;
    }

    private static enum Position {
        NORTH_WEST, NORTH_EAST, SOUTH_WEST, SOUTH_EAST
    }

    public void close() {
        if (!getParentGroup().getChildren().contains(this)) {
            throw new IllegalStateException("Parent [" + getParentGroup() + "] does not contain " + this + " as child.");
        }
        getParentGroup().getChildren().remove(this);
    }

    private static class WrapperRectangle extends Group {
        private final static double EXTENDING_FACTOR = 0.15;
        private final Callout callout;

        public WrapperRectangle(final Group nodeToBeWrapped, final Position position, final Callout callout) {
            this.callout = callout;
            final double nodeToBeWrappedW = nodeToBeWrapped.getBoundsInLocal().getWidth();
            final double nodeToBeWrappedH = nodeToBeWrapped.getBoundsInLocal().getHeight();
            final double nodeToBeWrappedExtW = (1.0 + EXTENDING_FACTOR) * nodeToBeWrappedW;
            final double nodeToBeWrappedExtH = (1.0 + EXTENDING_FACTOR) * nodeToBeWrappedH;

            nodeToBeWrapped.setTranslateX(nodeToBeWrapped.getTranslateX() + 0.5 * EXTENDING_FACTOR * nodeToBeWrappedW);
            nodeToBeWrapped.setTranslateY(nodeToBeWrapped.getTranslateY() + 0.5 * EXTENDING_FACTOR * nodeToBeWrappedH);

            final double deltaX = 0.3;
            final double deltaY = 0.3;
            final double transX = Position.NORTH_EAST == position || Position.SOUTH_EAST == position ? (0 - deltaX * nodeToBeWrappedExtW) : (-nodeToBeWrappedExtW + deltaX
                    * nodeToBeWrappedExtW);
            final double transY = Position.SOUTH_WEST == position || Position.SOUTH_EAST == position ? (0 + deltaY * nodeToBeWrappedExtH) : (-nodeToBeWrappedExtH - deltaY
                    * nodeToBeWrappedExtH);
            nodeToBeWrapped.setTranslateX(nodeToBeWrapped.getTranslateX() + transX);
            nodeToBeWrapped.setTranslateY(nodeToBeWrapped.getTranslateY() + transY);

            final Rectangle rect = createRectangle(nodeToBeWrappedExtW, nodeToBeWrappedExtH, transX, transY);
            final Polygon poly = createTriangle(position, nodeToBeWrappedExtW, nodeToBeWrappedExtH, transX, transY);

            final Shape rectAndPoly = Path.union(rect, poly);
            rectAndPoly.setFill(new RadialGradient(225, 0.5, 0, 0, (nodeToBeWrappedW + nodeToBeWrappedH) / 2, false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.LIGHTGRAY), new Stop(1.0, Color.WHITE)));
            rectAndPoly.setStrokeWidth(1.0);
            rectAndPoly.setStroke(Color.DARKGREY);

            final Button closeButton = createCloseButton(nodeToBeWrappedExtW, transX, transY, callout, callout.getCloseAction());

            getChildren().add(rectAndPoly);
            getChildren().add(nodeToBeWrapped);
            getChildren().add(closeButton);
        }

        protected static Button createCloseButton(final double nodeToBeWrappedExtW, final double transX, final double transY, final Callout callout, final Runnable closeAction) {
            final Button closeButton = new RoundCloseButton(nodeToBeWrappedExtW);
            closeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent event) {
                    closeAction.run();
                }
            });

            closeButton.setTranslateX(closeButton.getTranslateX() + transX + nodeToBeWrappedExtW - closeButton.getPrefWidth() - 3.0);
            closeButton.setTranslateY(closeButton.getTranslateY() + transY + 3.0);
            return closeButton;
        }

        protected static Polygon createTriangle(final Position position, final double nodeToBeWrappedExtW, final double nodeToBeWrappedExtH, final double transX, final double transY) {
            final double oppositeX = Position.NORTH_EAST == position || Position.SOUTH_EAST == position ? transX + nodeToBeWrappedExtW : transX;
            final double oppositeY = Position.SOUTH_WEST == position || Position.SOUTH_EAST == position ? transY + nodeToBeWrappedExtH : transY;
            final double dx = 0.3 * min(nodeToBeWrappedExtH, nodeToBeWrappedExtW);
            final int leftLineCoefX = Position.NORTH_WEST == position ? 0 : Position.NORTH_EAST == position ? -1 : Position.SOUTH_EAST == position ? 0 : +1;
            final int leftLineCoefY = Position.NORTH_WEST == position ? +1 : Position.NORTH_EAST == position ? 0 : Position.SOUTH_EAST == position ? -1 : 0;
            final int rightLineCoefX = Position.NORTH_WEST == position ? +1 : Position.NORTH_EAST == position ? 0 : Position.SOUTH_EAST == position ? -1 : 0;
            final int rightLineCoefY = Position.NORTH_WEST == position ? 0 : Position.NORTH_EAST == position ? +1 : Position.SOUTH_EAST == position ? 0 : -1;
            final Polygon poly = new Polygon();
            poly.getPoints().addAll(new Double[] { 0.0, 0.0, oppositeX + leftLineCoefX * dx, oppositeY + leftLineCoefY * dx, oppositeX + rightLineCoefX * dx,
                    oppositeY + rightLineCoefY * dx });
            return poly;
        }

        protected static Rectangle createRectangle(final double width, final double height, final double transX, final double transY) {
            final Rectangle rect = new Rectangle(0, 0, width, height);
            rect.setArcHeight(20);
            rect.setArcWidth(20);

            rect.setTranslateX(rect.getTranslateX() + transX);
            rect.setTranslateY(rect.getTranslateY() + transY);
            return rect;
        }

        private static double min(final double x, final double y) {
            return x > y ? y : x;
        }

        private static class RoundCloseButton extends Button {
            public RoundCloseButton(final double rectangleWidth) {
                super("", pathCloseIcon());

                //-fx-background-color: #f0ff35;

                // -fx-background-color: grey;
                // setStyle("-fx-background-insets: 0, 0, 0, 0; -fx-padding: 1, 1, 1, 1;"); //
                // setStyle("-fx-border-radius: 15 0 0 0; -fx-padding: 1, 1, 1, 1;"); //
                // setStyle("-fx-background-insets: 0, 0, 0, 0;");
                // setStyle("-fx-padding: 1, 1, 1, 1;"); //

                setStyle("-fx-base: white; -fx-border-radius: 15; -fx-background-insets: 0, 0, 0, 0; -fx-padding: 1, 1, 1, 1;"); //

                setPrefHeight(15.0);
                setPrefWidth(15.0);

                // setGraphicTextGap(0);

                // setAlignment(Pos.CENTER);
                // setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            private static Node pathCloseIcon() {
                final Path path = new Path();
                final double w = 6;
                path.setFill(Color.RED);
                path.getElements().addAll(
                //		new MoveTo( 9, 9),
                //		new LineTo(21, 21),
                //		new MoveTo( 9, 21),
                //		new LineTo(21, 9)
                new MoveTo(0, 0), new LineTo(w, w), new MoveTo(0, w), new LineTo(w, 0));
                // path.getElements().add(new Circle(0.0, 0.0, 3.0);
                return path;
            }
        }
    }
}