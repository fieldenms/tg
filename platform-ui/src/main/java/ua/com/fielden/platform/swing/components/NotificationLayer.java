package ua.com.fielden.platform.swing.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.Utils2D;

import com.jidesoft.swing.StyledLabelBuilder;

/**
 * This is a notification layer. It does the following:
 * <ul>
 * <li>Hides managed component (the one passed into the constructor) by wrapping it in a sub-layer, which has the background colour of the component.
 * <li>Paint the component in the specified colour and draws a message on top with gradual fading out of both colour and the message.
 * <li>Animates (fades in) the component back once the colour and message completely faded.
 * </ul>
 * 
 * @author 01es
 */
public class NotificationLayer<T extends JComponent> extends JXLayer<NotificationLayer.FadingInLayer<T>> {
    private static final long serialVersionUID = 1L;

    /**
     * Defines notification message types.
     * 
     * @author 01es
     * 
     */
    public enum MessageType {
        WARNING(new Color(255, 255, 110), JOptionPane.WARNING_MESSAGE, "Warning"), ERROR(new Color(250, 94, 94), JOptionPane.ERROR_MESSAGE, "Error"), INFO(new Color(94, 250, 94),
                JOptionPane.INFORMATION_MESSAGE, "Information"), NONE(null, null, null);

        public final Color colour;
        public final Integer jopMessageType;
        public final String msgTitle;

        MessageType(final Color colour, final Integer jopMessageConst, final String msgTitle) {
            this.colour = colour;
            this.jopMessageType = jopMessageConst;
            this.msgTitle = msgTitle;
        }
    }

    public NotificationLayer(final T component) {
        super(new FadingInLayer<T>(component));
        setUI(new FadingUi(getView()));
    }

    @Override
    public FadingUi getUI() {
        return (FadingUi) super.getUI();
    }

    public NotificationLayer<T> setMessage(final String msg, final MessageType msgType) {
        getUI().setMessage(msg, msgType);
        return this;
    }

    /**
     * Provides painting logic.
     * 
     * @author 01es
     * 
     */
    private class FadingUi extends AbstractLayerUI<FadingInLayer<T>> implements ActionListener {
        private final FadingInLayer<T> subLayer;
        private Color originalColour;
        private Color colour;
        private boolean shouldFadingResume = false;
        private String message;
        private MessageType msgType = MessageType.NONE;
        private static final int ALPHA = 255;
        private static final int FADING_SPEED = 2;
        private int alpha = ALPHA;
        private final int pause = 3000;
        private final Timer fadingTimer = new Timer(pause / (ALPHA / FADING_SPEED), this);
        private final Timer holdTimer = new Timer(pause, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                fadingTimer.stop();
                fadingTimer.start();

                holdTimer.stop();
            }
        });
        private Font font = new StyledLabelBuilder().add("text", "bold,f:darkgray").createLabel().getFont();

        public FadingUi(final FadingInLayer<T> subLayer) {
            this.subLayer = subLayer;

        }

        /**
         * Controls the behaviour of the notification panel with respect to mouse events:
         * <ul>
         * <li>Mouse enter stops fading and returns message to its original intensity.
         * <li>Mouse exit restarts fading.
         * <li>Mouse click hides the message and displays a modal dialog with a message.
         * </ul>
         */
        @Override
        protected void processMouseEvent(final MouseEvent event, final JXLayer<NotificationLayer.FadingInLayer<T>> layer) {
            super.processMouseEvent(event, layer);
            if (event.getID() == MouseEvent.MOUSE_ENTERED) {
                if (fadingTimer.isRunning()) {
                    resetFading();
                }
            } else if (event.getID() == MouseEvent.MOUSE_EXITED) {
                if (shouldFadingResume) {
                    setColour(originalColour);
                }
            } else if (event.getID() == MouseEvent.MOUSE_CLICKED) {
                if (msgType != MessageType.NONE && !StringUtils.isEmpty(getMessage())) {
                    // need to have a local copy of the following values since they are nulled out in the stop() method
                    final String title = msgType.msgTitle;
                    final int type = msgType.jopMessageType;
                    final String msg = getMessage();
                    stop();
                    event.consume();
                    JOptionPane.showMessageDialog(layer.getTopLevelAncestor(), msg, title, type);
                }
            }

        }

        public void actionPerformed(final ActionEvent e) {
            subLayer.setVisible(false);
            alpha = alpha - FADING_SPEED;
            colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);

            setDirty(true); // trigger repainting
            if (alpha < FADING_SPEED) {
                stop();
            }
        }

        private void stop() {
            msgType = MessageType.NONE;
            colour = msgType.colour;
            holdTimer.stop();
            subLayer.display();
            alpha = ALPHA;
            subLayer.setVisible(true);
            fadingTimer.stop();
            shouldFadingResume = false;
        }

        /**
         * Painting...
         */
        @Override
        protected void paintLayer(final Graphics2D g2, final JXLayer<FadingInLayer<T>> layer) {
            super.paintLayer(g2, layer); // this paints layer as is
            if (colour != null) { // just in case
                g2.setColor(colour);
                g2.fillRect(0, 0, layer.getWidth(), layer.getHeight());
                if (!StringUtils.isEmpty(message)) {

                    g2.setFont(font);
                    g2.setColor(new Color(40, 40, 40, alpha));
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                    int w = subLayer.getSize().width - (subLayer.getInsets().left + subLayer.getInsets().right);
                    int h = subLayer.getSize().height - (subLayer.getInsets().top + subLayer.getInsets().bottom);
                    // define how many characters in the caption can be drawn
                    final String textToDisplay = Utils2D.abbreviate(g2, getMessage(), w);

                    w = subLayer.getSize().width;
                    h = subLayer.getSize().height;
                    //final double xPos = component.getInsets().left;

                    final Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(textToDisplay, g2);
                    final double xPos = (w - textBounds.getWidth()) / 2.;
                    final double yPos = (h - textBounds.getHeight()) / 2. + g2.getFontMetrics().getAscent();
                    g2.drawString(textToDisplay, (float) xPos, (float) yPos);
                }
            }
        }

        private void setColour(final Color colour) {
            shouldFadingResume = false;
            subLayer.suppress();
            alpha = ALPHA;
            this.colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);
            originalColour = this.colour;
            setDirty(true); // trigger repainting
            holdTimer.stop();
            holdTimer.start();
        }

        private void resetFading() {
            fadingTimer.stop();
            subLayer.suppress();
            alpha = ALPHA;
            this.colour = new Color(originalColour.getRed(), originalColour.getGreen(), originalColour.getBlue(), alpha);
            originalColour = this.colour;
            setDirty(true); // trigger repainting
            shouldFadingResume = true;
        }

        public JComponent getSubLayer() {
            return subLayer;
        }

        protected String getMessage() {
            return message;
        }

        protected void setMessage(final String message, final MessageType msgType) {
            if (msgType != MessageType.NONE || this.msgType != msgType) {
                this.message = message;
                this.msgType = msgType;
                if (msgType == MessageType.NONE) {
                    stop();
                } else {
                    setColour(msgType.colour);
                }
            }
        }

    }

    /**
     * This layer is responsible for re-appearance of the managed component.
     * 
     * @author 01es
     * 
     * @param <T>
     */
    protected static class FadingInLayer<T extends JComponent> extends JXLayer<T> {
        private static final long serialVersionUID = 1L;

        public FadingInLayer(final T component) {
            super(component);
            setUI(new FadingInUi<T>(component));
            if (component != null) {
                component.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(final FocusEvent e) {
                        repaint();
                    }

                    @Override
                    public void focusLost(final FocusEvent e) {
                        repaint();
                    }
                });
            }
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(final FocusEvent e) {
                    if (getView() != null) {
                        getView().requestFocusInWindow();
                    }
                }
            });
        }

        @Override
        public FadingInUi<T> getUI() {
            return (FadingInUi<T>) super.getUI();
        }

        public FadingInLayer<T> suppress() {
            getUI().suppress();
            return this;
        }

        public FadingInLayer<T> display() {
            getUI().display();
            return this;
        }

        /**
         * Provides painting logic for re-appearance of the managed component.
         * 
         * @author 01es
         * 
         */
        private static class FadingInUi<T extends JComponent> extends AbstractLayerUI<T> implements ActionListener {
            private final JComponent component;
            private Color colour;
            private static final int ALPHA = 255;
            private static final int FADING_SPEED = 10;
            private int alpha = ALPHA;
            private final Timer timer = new Timer(1000 / 30, this);

            public FadingInUi(final JComponent component) {
                this.component = component;
            }

            public void actionPerformed(final ActionEvent e) {
                alpha = alpha - FADING_SPEED;
                colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);
                setDirty(true); // trigger repainting
                if (alpha < FADING_SPEED) {
                    colour = null;
                    alpha = ALPHA;
                    timer.stop();
                }
            }

            /**
             * Painting...
             */
            @Override
            protected void paintLayer(final Graphics2D g2, final JXLayer<T> layer) {
                super.paintLayer(g2, layer); // this paints layer as is
                if (colour != null) { // just in case
                    g2.setColor(colour);
                    g2.fillRect(0, 0, layer.getWidth(), layer.getHeight());
                }
            }

            public void suppress() {
                alpha = ALPHA;
                this.colour = component.getBackground();
                setDirty(true); // trigger repainting
                timer.stop();
            }

            public void display() {
                timer.start();
            }

            public JComponent getComponent() {
                return component;
            }
        }
    }

    public static void main(final String[] args) {
        for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(laf.getName())) {
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        final JPanel panel = new JPanel(new MigLayout("fill", "[]", "[grow,fill,c,:30:]"));
        final JLabel label = new JLabel("Work Order Master");
        label.setFont(new Font(label.getFont().getFamily(), Font.BOLD, 16)); //
        panel.add(label);
        final NotificationLayer<JPanel> notifLayer = new NotificationLayer<JPanel>(panel);
        final JPanel panelWithBevel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]"));
        panelWithBevel.setBorder(new LineBorder(new Color(140, 140, 140)));
        panelWithBevel.add(notifLayer);

        final JButton buttonYellow = new JButton(new AbstractAction("Warning") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                notifLayer.setMessage("This is a warning message, warning message, warning message.", MessageType.WARNING);
            }
        });
        final JButton buttonRed = new JButton(new AbstractAction("Error") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                notifLayer.setMessage("This is an error message, an error message, an error message.", MessageType.ERROR);
            }
        });
        final JButton buttonGreen = new JButton(new AbstractAction("Info") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                notifLayer.setMessage("This is an info message, an info message, an info message.", MessageType.INFO);
            }
        });
        final JPanel actionPanel = new JPanel(new MigLayout("", "[fill, :100:][fill, :100:]"));
        actionPanel.add(buttonYellow);
        actionPanel.add(buttonRed);
        actionPanel.add(buttonGreen);
        final JPanel holder = new JPanel(new MigLayout("fill", "[grow,fill,:600:]", "[grow,fill][]"));
        holder.add(panelWithBevel, "wrap");
        holder.add(actionPanel);
        SimpleLauncher.show("Show off the fading notification layer", holder);
    }
}
