/*
 * Copyright (c) 2005, romain guy (romain.guy@jext.org) and craig wickesser (craig@codecraig.com) and henry story
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ua.com.fielden.platform.swing.components.blocking;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * This component was originally created by romain guy and craig wickesser (refer copiright above), and later enhanced to provide access to the <code>dScale</code> property.
 * 
 * @author 01es
 * 
 */
public class InfiniteProgressPanel extends JComponent implements ActionListener {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_NUMBER_OF_BARS = 12;

    private int numBars;
    private final double dScale;

    private Area[] bars;
    private Rectangle barsBounds = null;
    private Rectangle barsScreenBounds = null;
    private AffineTransform centerAndScaleTransform = null;
    private final Timer timer = new Timer(1000 / 2, this);
    private Color[] colors = null;
    private int colorOffset = 0;

    public InfiniteProgressPanel() {
        this(1.2d);
    }

    public InfiniteProgressPanel(final double scale) {
        dScale = scale;
        this.numBars = DEFAULT_NUMBER_OF_BARS;

        colors = new Color[numBars * 2];

        // build bars
        bars = buildTicker(numBars);

        // calculate bars bounding rectangle
        barsBounds = new Rectangle();
        for (final Area bar : bars) {
            barsBounds = barsBounds.union(bar.getBounds());
        }

        // create colors
        for (int i = 0; i < bars.length; i++) {
            final int channel = 224 - 128 / (i + 1);
            colors[i] = new Color(channel, channel, channel);
            colors[numBars + i] = colors[i];
        }

        // set cursor
        //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // set opaque
        setOpaque(true);
    }

    /**
     * Called to animate the rotation of the bar's colours
     */
    public void actionPerformed(final ActionEvent e) {
        // rotate colours
        if (colorOffset == (numBars - 1)) {
            colorOffset = 0;
        } else {
            colorOffset++;
        }
        // repaint
        if (barsScreenBounds != null) {
            repaint(barsScreenBounds);
        } else {
            repaint();
        }
    }

    /**
     * Start and stops painting of the progress indication.
     * 
     * @param i_bIsVisible
     */
    protected void paintProgress(final boolean flag) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!isVisible() && flag) { // just in case
                    setVisible(true);
                }
                if (flag) {
                    timer.start(); // start anim
                } else {
                    timer.stop(); // stop anim
                }
            }
        });
    }

    /**
     * Show/Hide the pane
     */
    @Override
    public void setVisible(final boolean i_bIsVisible) {
        setOpaque(false);
        super.setVisible(i_bIsVisible);
    }

    /**
     * Recalc bars based on changes in size
     */
    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        super.setBounds(x, y, width, height);
        // update centering transform
        centerAndScaleTransform = new AffineTransform();
        centerAndScaleTransform.translate(getWidth() / 2d, getHeight() / 2d);
        centerAndScaleTransform.scale(dScale, dScale);
        // calc new bars bounds
        if (barsBounds != null) {
            final Area oBounds = new Area(barsBounds);
            oBounds.transform(centerAndScaleTransform);
            barsScreenBounds = oBounds.getBounds();
        }
    }

    /**
     * paint background dimed and bars over top
     */
    @Override
    protected void paintComponent(final Graphics g) {
        paintProgressTickers(g);
        super.paintComponent(g);
    }

    protected void superPaintComponent(final Graphics g) {
        super.paintComponent(g);
    }

    protected void paintProgressTickers(final Graphics g) {
        final Rectangle oClip = g.getClipBounds();

        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(oClip.x, oClip.y, oClip.width, oClip.height);
        }
        // move to center
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.transform(centerAndScaleTransform);
        // draw ticker
        for (int i = 0; i < bars.length; i++) {
            g2.setBackground(new Color(255, 255, 255, 255));
            g2.setColor(colors[i + colorOffset]);
            g2.fill(bars[i]);
        }
    }

    /**
     * Builds the circular shape and returns the result as an array of <code>Area</code>. Each <code>Area</code> is one of the bars composing the shape.
     */
    private static Area[] buildTicker(final int i_iBarCount) {
        final Area[] ticker = new Area[i_iBarCount];
        final Point2D.Double center = new Point2D.Double(0, 0);
        final double fixedAngle = 2.0 * Math.PI / (i_iBarCount);

        for (double i = 0.0; i < i_iBarCount; i++) {
            final Area primitive = buildPrimitive();

            final AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
            final AffineTransform toBorder = AffineTransform.getTranslateInstance(2.0, -0.4);
            final AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

            final AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);

            primitive.transform(toWheel);
            primitive.transform(toCircle);

            ticker[(int) i] = primitive;
        }

        return ticker;
    }

    /**
     * Builds a bar.
     */
    private static Area buildPrimitive() {
        /*final Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
        final Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
        final Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);*/

        /* final Rectangle2D.Double body = new Rectangle2D.Double(3, 0, 15, 6);
        final Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 6, 6);
        final Ellipse2D.Double tail = new Ellipse2D.Double(15, 0, 6, 6);*/

        /*final Rectangle2D.Double body = new Rectangle2D.Double(2, 0, 10, 4);
        final Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 4, 4);
        final Ellipse2D.Double tail = new Ellipse2D.Double(10, 0, 4, 4);*/

        final Rectangle2D.Double body = new Rectangle2D.Double(0, 0, 6, 1);
        //final Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 2, 2);
        //final Ellipse2D.Double tail = new Ellipse2D.Double(5, 0, 2, 2);

        final Area tick = new Area(body);
        //tick.add(new Area(head));
        //tick.add(new Area(tail));
        return tick;
    }

    /**
     * Simply invokes paintProgress(true).
     */
    public void start() {
        paintProgress(true);
    }

    public void stop() {
        setVisible(false);
        paintProgress(false);
    }
}