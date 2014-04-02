package ua.com.fielden.platform.events;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.PFrame;

public class DemoApp extends PFrame {

    public DemoApp() {
        initCanvas();
        getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
        getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler());
        MultipleSelectionHandler selectionEventHandler = new MultipleSelectionHandler(getCanvas().getLayer(), getCanvas().getLayer());
        getCanvas().addInputEventListener(selectionEventHandler);
    }

    private void initCanvas() {
        for (int rowCounter = 0; rowCounter < 5; rowCounter++) {
            for (int colCounter = 0; colCounter < 5; colCounter++) {
                RectWidget rect = new RectWidget();
                rect.translate(colCounter * (rect.getWidth() + 10), rowCounter * (rect.getHeight() + 10));
                getCanvas().getLayer().addChild(rect);

            }
        }
        RectWidget rect1 = new RectWidget();
        rect1.setBounds(new Rectangle2D.Double(0, 0, 50, 50));
        RectWidget rect2 = new RectWidget();
        rect1.addChild(rect2);
        getCanvas().getLayer().addChild(rect1);
        rect1.translate(300, 300);
        rect2.scale(0.5);
        rect2.setChildrenPickable(true);

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new DemoApp();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(640, 480));
                frame.setTitle("multiple Selection Example");
                frame.pack();
                frame.setVisible(true);
            }

        });
    }

    private static class RectWidget extends PPath implements IDecorable {

        private Paint selectedPaint;
        private Stroke selectedStroke;

        public RectWidget() {
            super();
            selectedPaint = Color.RED;
            selectedStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0);
            initNode();
        }

        private void initNode() {
            setPathTo(new Rectangle2D.Float(0, 0, 50, 50));
            setPaint(Color.GREEN);
        }

        @Override
        public void Decorate() {
            changeSelection();
            moveToFront();
        }

        @Override
        public void Undecorate() {
            changeSelection();
        }

        private void changeSelection() {
            Paint tempPaint = getPaint();
            Stroke tempStroke = getStroke();
            setPaint(selectedPaint);
            setStroke(selectedStroke);
            selectedPaint = tempPaint;
            selectedStroke = tempStroke;
        }
    }

}
