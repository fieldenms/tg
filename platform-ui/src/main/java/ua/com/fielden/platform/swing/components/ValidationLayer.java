package ua.com.fielden.platform.swing.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

/**
 * This is a layer, which serves as a validation feedback indicator.
 * 
 * The current implementation simply changes the foreground colour of the component and updates its tool tip. Later modifications may introduce glyph painting.
 * 
 * IMPORTANT: once layer painted its state, the result is set to null.
 * 
 * @author TG Team
 * 
 */
public class ValidationLayer<T extends JComponent> extends JXLayer<T> {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTYNAME_RESULT = "result";

    private final Map<String, ? extends ValidationLayer> boundedLayers = new HashMap<String, ValidationLayer>();

    public ValidationLayer(final T component) {
        super(component);
        setUI(new ValidationUi(component, boundedLayers));
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
    public ValidationUi getUI() {
        return (ValidationUi) super.getUI();
    }

    public Result getResult() {
        return getUI().getResult();
    }

    public ValidationLayer<T> setResult(final Result result) {
        getUI().setResult(result);
        return this;
    }

    /*
     * Methods providing bindining between validation layers
     */

    /**
     * Bounds passed layer to this one. Binding means unidirectional binding from this layer to passed one between {@link Result} properties on their UI's.
     * 
     * @param key
     *            - {@link String} key, by which this layer could be obtained later
     * @param validationLayer
     */
    @SuppressWarnings("unchecked")
    public void boundLayer(final String key, final ValidationLayer validationLayer) {
        ((HashMap<String, ? super ValidationLayer>) boundedLayers).put(key, validationLayer);
        validationLayer.getUI().setResult(getUI().getResult());
    }

    /**
     * Unbounds passed layer, if it was bounded
     * 
     * @param validationLayer
     */
    @SuppressWarnings("unchecked")
    public void unboundLayer(final ValidationLayer validationLayer) {
        // searching for a String key with such validation layer
        String key = null;
        for (final Map.Entry<String, ? extends ValidationLayer> entry : boundedLayers.entrySet()) {
            if (entry.getValue().equals(validationLayer)) {
                key = entry.getKey();
                break;
            }
        }
        if (key != null) {
            boundedLayers.remove(key);
        }
    }

    /**
     * Returns unmodifiable list of layers, bounded to this one
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, ValidationLayer> getBoundedLayers() {
        return Collections.unmodifiableMap(boundedLayers);
    }

    /*
     * END : Methods providing bindining between validation layers
     */

    /**
     * Provides painting logic.
     * 
     * @author 01es
     * 
     */
    public class ValidationUi extends AbstractLayerUI<T> {
        private final JComponent component;

        private final Map<String, ? extends ValidationLayer> boundedLayers;

        private String originalToolTip;

        private Result result;

        public ValidationUi(final JComponent component) {
            this(component, new HashMap<String, ValidationLayer>());
        }

        public ValidationUi(final JComponent component, final Map<String, ? extends ValidationLayer> boundedLayers) {
            this.component = component;
            this.boundedLayers = boundedLayers;
        }

        /**
         * additional method for BoundeValidationUi descendant
         * 
         * @param g2
         * @param layer
         */
        protected void paintAbstractLayer(final Graphics2D g2, final JXLayer<T> layer) {
            super.paintLayer(g2, layer);
        }

        /**
         * Paint the state and set result to null.
         */
        @Override
        @SuppressWarnings("unchecked")
        protected void paintLayer(final Graphics2D g2, final JXLayer<T> layer) {
            super.paintLayer(g2, layer); // this paints layer as is
            // paint the result indication
            if (getResult() != null) {
                if (getResult().isSuccessfulWithoutWarning()) {
                    if (originalToolTip != null && component != null) {
                        if (component instanceof JXLayer) {
                            // if ValidationLayer wraps another JXLayer like in
                            // ComponentFactory autocompleter creation
                            ((JXLayer<JComponent>) component).getView().setToolTipText(originalToolTip);
                        } else {
                            component.setToolTipText(originalToolTip); // return the original tool tip back if any
                        }
                        originalToolTip = null;
                        setResult(null);
                    }
                } else {
                    // here result is either warning or error, displaying result message as tool-tip
                    if (component != null) {
                        if (originalToolTip == null) {
                            if (component instanceof JXLayer) {
                                // if ValidationLayer wraps another JXLayer like in
                                // ComponentFactory autocompleter creation
                                originalToolTip = ((JXLayer<?>) component).getView().getToolTipText();
                            } else {
                                originalToolTip = component.getToolTipText(); // return the original tool tip back if any
                            }
                            if (originalToolTip == null) {
                                originalToolTip = "";
                            }
                        }
                        if (component instanceof JXLayer) {
                            // if ValidationLayer wraps another JXLayer like in
                            // ComponentFactory autocompleter creation
                            ((JXLayer<?>) component).getView().setToolTipText(getResult().getMessage()); // set error message as a tool tip
                        } else {
                            component.setToolTipText(getResult().getMessage()); // set error message as a tool tip
                        }
                    }
                    // checking whether it is error or warning and choosing color
                    g2.setColor(determineColor(getResult()));
                    g2.fillRect(0, 0, layer.getWidth(), layer.getHeight());
                }
            }
        }

        /**
         * Determines color based on result state.
         * 
         * @param result
         * @return
         */
        protected Color determineColor(final Result result) {
            if (!ValidationLayer.this.isEnabled()) {
                return null;
            }
            return result.isWarning() ? new Color(255, 255, 65, 90) : new Color(190, 0, 0, 90);
        }

        public Result getResult() {
            return result;
        }

        /**
         * Sets the result and triggers layer repainting.
         * 
         * @param result
         */
        @SuppressWarnings("unchecked")
        public void setResult(final Result result) {
            final Result oldResult = this.result;

            this.result = result;
            setDirty(true); // trigger repainting

            ValidationLayer.this.firePropertyChange(ValidationLayer.PROPERTYNAME_RESULT, oldResult, result);

            for (final ValidationLayer boundedLayer : boundedLayers.values()) {
                boundedLayer.getUI().setResult(result);
            }
        }

        public JComponent getComponent() {
            return component;
        }

        @SuppressWarnings("unchecked")
        protected Map<String, ? extends ValidationLayer> getBoundedLayers() {
            return boundedLayers;
        }
    }

    public static void main(final String[] args) {
        final JPanel panel = new JPanel(new MigLayout("fill", "[:250:]"));
        final ValidationLayer<JTextField> layer = new ValidationLayer<JTextField>(new JTextField());
        layer.getView().setToolTipText("original tool tip text");
        panel.add(layer, "growx, wrap");

        final ValidationLayer<JLabel> boundedLayer = new ValidationLayer<JLabel>(new JLabel("Bounded Layer"));
        final String boundedLayerKey = "boundedLayerWithLabel";
        layer.boundLayer(boundedLayerKey, boundedLayer);
        panel.add(boundedLayer, "growx, wrap");

        panel.add(new JButton(new Command<Boolean>("Refer validation result") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Boolean action(final ActionEvent e) throws Exception {
                if (layer.getUI().getResult() == null || layer.getUI().getResult().isSuccessful()) {
                    layer.getUI().setResult(new Result(null, "failed validation", new Exception()));
                } else {
                    layer.getUI().setResult(new Result(null, "all ok"));
                }
                return true;
            }

        }), "growx, wrap");

        final JButton boundLayerButton = new JButton();
        final Action boundLayerAction = new Command<Boolean>("Unbound layer") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Boolean action(final ActionEvent e) throws Exception {
                @SuppressWarnings("unchecked")
                final ValidationLayer bounded = layer.getBoundedLayers().get(boundedLayerKey);
                if (bounded != null) {
                    layer.unboundLayer(bounded);
                    boundLayerButton.setText("Bound Layer");
                } else {
                    layer.boundLayer(boundedLayerKey, boundedLayer);
                    boundLayerButton.setText("Unbound Layer");
                }
                return true;
            }

        };
        boundLayerButton.setAction(boundLayerAction);
        panel.add(boundLayerButton, "growx");

        SimpleLauncher.show("Show off the caption", panel);
    }
}
