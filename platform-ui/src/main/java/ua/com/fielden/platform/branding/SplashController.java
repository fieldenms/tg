package ua.com.fielden.platform.branding;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * Provides custom drawing of messages on the provided splash in order to provide a better feedback to the user of what is happening during the application loading.
 * 
 * TODO need to complete implementation of the running bubbles beneath the message.
 * 
 * @author TG Team
 * 
 */
public class SplashController implements ActionListener {
    private static final int X = 20, W = 800;
    private static final int TEXT_H = 10, BAR_H = 10;

    private int textY, barY;
    private int barPos = 0;

    private final SplashScreen splash;
    private Graphics2D graph;

    private final Logger logger = Logger.getLogger(SplashController.class);

    public SplashController(final int msgXOffset, final int msgYOffset) {
        splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            logger.warn("Error: no splash image specified on the command line");
        } else {
            // compute base positions for text and progress bar
            final Dimension splashSize = splash.getSize();
            textY = splashSize.height - msgYOffset;
            barY = splashSize.height - msgYOffset;

            graph = splash.createGraphics();
            //timer.start();
        }
    }

    public SplashController() {
        this(30, 30);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        drawSplashProgress(msg);
    }

    public void closeSplash() {
        if (splash != null) {
            splash.close();
        }
    }

    private String msg;

    public void drawSplashProgress(final String msg) {
        if (graph == null) {
            logger.warn("Requested splash update without an actual splash screen provided.");
        } else {
            SwingUtilitiesEx.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SplashController.this.msg = msg;
                    graph.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    graph.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    // clear what we don't need from previous state
                    graph.setComposite(AlphaComposite.Clear);

                    final FontMetrics fm = graph.getFontMetrics();
                    final Rectangle2D textsize = fm.getStringBounds(msg, graph);

                    graph.fillRect(X, textY - 1, W, (int) textsize.getHeight() + 5);
                    if (barPos == 0) {
                        graph.fillRect(X - 3, barY, W + 10, BAR_H);
                    }

                    // draw new state
                    graph.setPaintMode();

                    // draw message
                    graph.setColor(Color.BLACK);
                    graph.drawString(msg, X, textY + TEXT_H);
                    try {
                        splash.update();
                    } catch (final IllegalStateException e) {
                        logger.warn("Could not update the splash.", e);
                    }
                }
            });
        }
    }

    public static void main(final String args[]) throws Exception {
        final SplashController test = new SplashController();
        for (int i = 0; i < 50; i++) {
            test.drawSplashProgress("Progress step number " + i);
            Thread.sleep(250);
        }
        test.closeSplash();
    }
}