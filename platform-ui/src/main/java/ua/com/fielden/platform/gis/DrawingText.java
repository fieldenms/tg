package ua.com.fielden.platform.gis;

import java.util.Random;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 
 * @author cdea
 */
public class DrawingText extends Application {
    /**
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("Chapter 1-3 Drawing Text");
        final Group root = new Group();
        final Scene scene = new Scene(root, 800, 600, Color.WHITE);

        scene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(final ScrollEvent event) {
                System.out.println(event.getClass() + " has been performed.");
            }
        });

        scene.setOnZoom(new EventHandler<ZoomEvent>() {

            @Override
            public void handle(final ZoomEvent event) {
                System.out.println(event.getClass() + " has been performed.");
            }

        });

        final Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < 100; i++) {
            final int x = rand.nextInt((int) scene.getWidth());
            final int y = rand.nextInt((int) scene.getHeight());
            final int red = rand.nextInt(255);
            final int green = rand.nextInt(255);
            final int blue = rand.nextInt(255);
            final Text text = new Text(x, y, "JavaFX 2.0");

            text.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent e) {
                    text.setScaleX(1.5);
                    text.setScaleY(1.5);
                }
            });

            text.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent e) {
                    text.setScaleX(1);
                    text.setScaleY(1);
                }
            });

            final int rot = rand.nextInt(360);
            text.setFill(Color.rgb(red, green, blue, .99));
            text.setRotate(rot);
            root.getChildren().add(text);
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
