package ua.com.fielden.platform.web.gis;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebViewSample extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        final StackPane root = new StackPane();

        final WebView view = new WebView();

        System.out.println(view.isCache());

        final WebEngine engine = view.getEngine();
        engine.load(GisViewPanel2.class.getResource("map.html").toString());
        // engine.load("http://www.fielden.com.ua");
        final Scene scene = new Scene(root, 800, 600);
        root.getChildren().add(view);
        stage.setScene(scene);
        stage.show();

        //        while (true) {
        //            final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        //            final MemoryUsage usage = mbean.getHeapMemoryUsage();
        //            final MemoryUsage nonHeapusage = mbean.getNonHeapMemoryUsage();
        //            final long total = (usage.getUsed() + nonHeapusage.getUsed()) / 1024;
        //            System.out.println("total = " + total);
        //        }

        //view.setDisable(true);

        //        view.setOnScrollStarted(new EventHandler<ScrollEvent>() {
        //            @Override
        //            public void handle(final ScrollEvent event) {
        //                System.out.println(event);
        //            }
        //        });

        //        view.setOnScroll(new EventHandler<ScrollEvent>() {
        //            @Override
        //            public void handle(final ScrollEvent event) {
        //                System.out.println(event);
        //            }
        //        });

        view.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(final ScrollEvent e) {
                System.out.println("EventFilter:" + e);

                // System.out.println("e.getMultiplierX() == " + e.getMultiplierX());
                // System.out.println("e.getMultiplierY() == " + e.getMultiplierY());

                if (Math.abs(e.getDeltaY()) >= 20.0) {
                    // if (e.getDeltaY() == 40.0 || e.getDeltaY() == -40.0) {
                    e.consume();

                    final ScrollEvent adjustedEvent = new ScrollEvent(e.getEventType(), e.getX(), e.getY(), e.getScreenX(), e.getScreenY(), e.isShiftDown(), //
                            e.isControlDown(), e.isAltDown(), e.isMetaDown(), e.isDirect(), e.isInertia(), e.getDeltaX(), //
                            e.getDeltaY() / e.getMultiplierY(), // here the value for y delta is turning back to 1.0 or -1.0 instead of multiplied 40.0 or -40.0
                            e.getTotalDeltaX(), e.getTotalDeltaY(), //
                            e.getMultiplierX(), e.getMultiplierY(), // these values do not make any changes
                            e.getTextDeltaXUnits(), e.getTextDeltaX(), e.getTextDeltaYUnits(), e.getTextDeltaY(), e.getTouchCount(), e.getPickResult());
                    // view.fireEvent(e.copyFor(e.getSource(), e.getTarget()));

                    view.fireEvent(adjustedEvent);
                }
            }
        });

        view.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(final ScrollEvent event) {
                System.out.println("EventHandler: " + event);
            }
        });

        System.out.println("view.onScrollProperty().isBound() == " + view.onScrollProperty().isBound());
        System.out.println("view.onScrollProperty() == " + view.onScrollProperty());

        //        view.setOnScrollFinished(new EventHandler<ScrollEvent>() {
        //            @Override
        //            public void handle(final ScrollEvent event) {
        //                System.out.println(event);
        //            }
        //        });
    }

    public static void main(final String[] args) throws IOException {
        Application.launch(args);
    }
}