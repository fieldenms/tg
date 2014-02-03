package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.jdesktop.jxlayer.JXLayer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.WhenNullMessage;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.Binder.WeakTrigger;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.development.SmartComponentUi;
import ua.com.fielden.platform.swing.utils.Utils2D;

import com.jgoodies.binding.value.Trigger;
import com.jgoodies.binding.value.ValueModel;

/**
 * Generalized ValidationLayer with opportunity to add/remove onCommitActions and flush/commit buffered values manually (only for OnFocusLost models). All create*() methods in
 * ComponentFactory returns BoundedValidationLayer
 *
 * @author TG Team
 *
 * @param <T>
 */
public class BoundedValidationLayer<T extends JComponent> extends ValidationLayer<T> implements IOnCommitActionable {
    private static final long serialVersionUID = -4862787312025734939L;

    public static final Color INVALID_COLOUR = new Color(190, 0, 0, 90), //
	    REQUIRED_COLOUR = new Color(120, 186, 244, 90), //
	    WARNING_COLOUR = new Color(255, 255, 65, 90);

    private IOnCommitActionable onCommitActionable;

    private ValueModel trigger;

    private IRebindable rebindable;

    private String originalToolTipText;

    private Color colourOnTop = null;

    public BoundedValidationLayer(final T component, final String originalToolTipText, final boolean selectAfterFocusGained) {
	super(component);
	this.setUI(new BoundedValidationUi(component));
	getIncapsulatedComponent().setToolTipText(this.originalToolTipText = originalToolTipText);
	if (getIncapsulatedComponent() instanceof JTextComponent && selectAfterFocusGained) {
	    getIncapsulatedComponent().addFocusListener(new FocusListener() {
		@Override
		public void focusGained(final FocusEvent e) {
		    ((JTextComponent) getIncapsulatedComponent()).selectAll();
		}

		@Override
		public void focusLost(final FocusEvent e) {
		    ((JTextComponent) getIncapsulatedComponent()).select(0, 0);
		}
	    });
	}
    }

    public BoundedValidationLayer(final T component, final String originalToolTipText) {
	this(component, originalToolTipText, true);
    }

    /**
     * Gets the insets of inner incapsulated component. In case of spinner incapsulated component, provides left inset as for inner text component in case when the editor of
     * spinner is a {@link DefaultEditor} instance.
     *
     * @return
     */
    public Insets getIncapsulatedInsets() {
	if (getIncapsulatedComponent() instanceof JSpinner) {
	    final JSpinner spinner = ((JSpinner) getIncapsulatedComponent());
	    final Insets spinnerInsets = spinner.getInsets();
	    if (spinner.getEditor() instanceof DefaultEditor) {
		final DefaultEditor defaultEditor = (DefaultEditor) spinner.getEditor();
		return defaultEditor.getTextField().getInsets();
		//new Insets(spinnerInsets.top, defaultEditor.getTextField().getInsets().left, spinnerInsets.bottom, spinnerInsets.right);
	    } else {
		return spinnerInsets;
	    }
	} else {
	    return getIncapsulatedComponent().getInsets();
	}
    }

    public Color getIncapsulatedBackground() {
	if (getIncapsulatedComponent() instanceof JSpinner) {
	    final JSpinner spinner = ((JSpinner) getIncapsulatedComponent());
	    if (spinner.getEditor() instanceof DefaultEditor) {
		//TODO for some reason background colour is grey... final Color color = ((DefaultEditor) spinner.getEditor()).getTextField().getBackground();
		return Color.WHITE;
	    } else {
		return spinner.getEditor().getBackground();
	    }
	} else {
	    return getIncapsulatedComponent().getBackground();
	}
    }

    /**
     * Adds a mouse listener to inner incapsulated component.
     *
     * @param listener
     */
    public synchronized void addIncapsulatedMouseListener(final MouseListener listener) {
	getIncapsulatedComponent().addMouseListener(listener);
    }

    private static <M extends JComponent> JComponent getIncapsulatedComponent(final JXLayer<M> layer) {
	return (layer.getView() instanceof JXLayer) ? getIncapsulatedComponent((JXLayer<?>) layer.getView()) : layer.getView();
    }

    private JComponent getIncapsulatedComponent() {
	return getIncapsulatedComponent(this);
    }

    @Override
    public boolean requestFocusInWindow() {
	return getIncapsulatedComponent().requestFocusInWindow(); // should be overridden to provide automatic refocusing from validation layer to its wrapped component
    }

    @Override
    public BoundedValidationUi getUI() {
	return (BoundedValidationUi) super.getUI();
    }

    /**
     * Sets a colour to be painted on top of bounded validation layer.
     *
     * @param colour
     */
    public void setColour(final Color colour) {
	this.colourOnTop = colour;
    }

    class BoundedValidationUi extends ValidationUi {

	public BoundedValidationUi(final JComponent component) {
	    this(component, new HashMap<String, ValidationLayer>());
	}

	public BoundedValidationUi(final JComponent component, final Map<String, ? extends ValidationLayer> boundedLayers) {
	    super(component, boundedLayers);
	}

	private boolean required = false;

	/**
	 * Paint the state and set result to null.
	 *
	 * Important : tooltips is not updated directly by the ValidationLayer! This work should be done in SubjectValueChangeHandlers!
	 */
	@Override
	protected void paintLayer(final Graphics2D g2, final JXLayer<T> layer) {
	    paintAbstractLayer(g2, layer); // this paints layer as is

	    final Color c = determineColor(getResult());

	    showEntityDescription(g2, c);

	    if (c != null) {
		g2.setColor(c);
	    }
	    // paint the result indication
	    if (getResult() != null) {
		if (getResult().isSuccessful()) {
		    if (getComponent() != null && !getResult().isWarning()) {
			setResult(null);
		    }
		}
	    }

	    if (c != null) {
		g2.fillRect(0, 0, layer.getWidth(), layer.getHeight());
	    }

	    if (colourOnTop != null) {
		g2.setColor(colourOnTop);
		g2.fillRect(0, 0, layer.getWidth(), layer.getHeight());
	    }
	}

	private void showEntityDescription(final Graphics2D g2, final Color color) {
	    if (color != INVALID_COLOUR && !getIncapsulatedComponent().hasFocus()) {
		final boolean autocompl = getView() instanceof AutocompleterTextFieldLayer;
		final boolean label = getView() instanceof JLabel;

		if (rebindable != null && rebindable.getSubjectBean() != null &&
		    Rebinder.getActualEntity(rebindable.getSubjectBean()) != null && rebindable.getPropertyName() != null) {
		    final IBindingEntity entity = Rebinder.getActualEntity(rebindable.getSubjectBean());
		    final String propertyName = rebindable.getPropertyName();

		    if (entity.get(propertyName) == null) {
			final AbstractEntity<?> entityAs =  (AbstractEntity<?>) entity;
			if (propertyName.equals(AbstractEntity.KEY) && AnnotationReflector.isAnnotationPresentForClass(WhenNullMessage.class, entityAs.getType())) {
			    final String msg = AnnotationReflector.getAnnotation(entityAs.getType(), WhenNullMessage.class).value();
			    drawMessage(g2, msg);
			} else if (!propertyName.equals(AbstractEntity.KEY) && AnnotationReflector.isPropertyAnnotationPresent(WhenNullMessage.class, entityAs.getType(), propertyName)) {
			    final String msg = AnnotationReflector.getPropertyAnnotation(WhenNullMessage.class, entityAs.getType(), propertyName).value();
			    drawMessage(g2, msg);
			}
		    } else if ((autocompl || label) && entity.get(propertyName) != null && entity.get(propertyName) instanceof AbstractEntity) {
			final AbstractEntity<?> value = (AbstractEntity<?>) entity.get(propertyName);
			if (AnnotationReflector.isAnnotationPresentForClass(DisplayDescription.class, value.getType())) {
			    final String desc = (String) value.get("desc");
			    final String prev = autocompl ? ((JTextField) getIncapsulatedComponent()).getText()
				    : TitlesDescsGetter.removeHtml(((JLabel) getIncapsulatedComponent()).getText());

			    drawMessage(g2, prev + " \u2012 " + desc);
			}
		    }
		}
	    }
	}

	private void drawMessage(final Graphics2D g2, final String msg) {
	    JComponent component = getIncapsulatedComponent();
	    component = component instanceof JSpinner ? ((DefaultEditor) ((JSpinner) component).getEditor()).getTextField() : component;

	    final Insets insets = getIncapsulatedInsets();

	    g2.setColor(getIncapsulatedBackground());
	    int w = component.getSize().width - (insets.left + insets.right);
	    int h = component.getSize().height - (insets.top + insets.bottom);
	    g2.fillRect(insets.left, insets.top, w, h);
	    // pain the caption
	    g2.setColor(new Color(0f, 0f, 0f, 1.0f));
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

	    // define how many characters in the caption can be drawn
	    final String textToDisplay = Utils2D.abbreviate(g2, msg, w);

	    w = component.getSize().width;
	    h = component.getSize().height;
	    final double xPos = insets.left;
	    final Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(textToDisplay, g2);
	    final double yPos = (h - textBounds.getHeight()) / 2. + g2.getFontMetrics().getAscent();
	    g2.drawString(textToDisplay, (float) xPos, (float) yPos);
	}

	/**
	 * Determines color based on result state and requiredness.
	 *
	 * @param result
	 * @return
	 */
	@Override
	protected Color determineColor(final Result result) {
	    // show red & yellow alerts regardless of whether property is enabled / disabled
	    if (result != null && !result.isSuccessful()) {
		return INVALID_COLOUR; // red in case of error result, that exists on entity property (e.g. manually populated in definer)
	    } else if (result != null && result.isWarning()) {
		return WARNING_COLOUR; // yellow in case of warning result, that exists on entity property (e.g. manually populated in definer)
	    } else {
		if (!BoundedValidationLayer.this.isEnabled()) {
		    return null; // if property is disabled (can not be edited) -- do not show whether it is required or not
		} else {
		    return required ? REQUIRED_COLOUR : null; // blue in case of enabled for editing and required property
		}
	    }
	}

	/**
	 * Sets the "required" property and triggers rapinting. If the result is successful, "required" ValidationLayer paints blue
	 *
	 * @param required
	 */
	@SuppressWarnings("unchecked")
	public void setRequired(final boolean required) {
	    this.required = required;
	    setDirty(true); // trigger repainting

	    // maybe need to add property change support to "required" property

	    for (final BoundedValidationLayer boundedLayer : ((Map<String, ? extends BoundedValidationLayer>) getBoundedLayers()).values()) {
		boundedLayer.getUI().setRequired(required);
	    }
	}

    }

    /**
     * used internally (in binding API) to set the OnCommitActionable, to add the opportunity to add/remove OnCommitActions to boundedComponent
     *
     * @param onCommitActionable
     * @return
     */
    public BoundedValidationLayer<T> setOnCommitActionable(final IOnCommitActionable onCommitActionable) {
	this.onCommitActionable = onCommitActionable;
	return this;
    }

    /**
     * Returns {@link IOnCommitActionable} associated with this instance
     *
     * @return
     */
    public IOnCommitActionable getOnCommitActionable() {
	return onCommitActionable;
    }

    /**
     * used internally (in binding API) to set the trigger, to add the opportunity to manually commit/flush triggered models
     *
     * @param trigger
     * @return
     */
    public BoundedValidationLayer<T> setTrigger(final ValueModel trigger) {
	this.trigger = trigger;
	return this;
    }

    /**
     * returns true if this validation layer has trigger
     *
     * @return
     */
    public boolean canCommit() {
	return trigger != null;
    }

    /**
     * commits the trigger assigned to its BoundedValidationLayer. If the trigger wasn't set->throws Exception
     */
    public void commit() {
	if (!canCommit()) {
	    throw new RuntimeException("BoundedValidationLayer cannot flush its buffer with not initialized 'trigger'. use setTrigger(...) method!!!! ");
	} else {
	    if (trigger instanceof WeakTrigger) {
		((WeakTrigger) trigger).triggerCommit();
	    } else {
		((Trigger) trigger).triggerCommit();
	    }
	}
    }

    /**
     * flushes the trigger assigned to its BoundedValidationLayer. If the trigger wasn't set->throws Exception
     */
    public void flush() {
	if (!canCommit()) {
	    throw new RuntimeException("BoundedValidationLayer cannot flush its buffer with not initialized 'trigger'. use setTrigger(...) method!!!! ");
	} else {
	    if (trigger instanceof WeakTrigger) {
		((WeakTrigger) trigger).triggerFlush();
	    } else {
		((Trigger) trigger).triggerFlush();
	    }
	}
    }

    /**
     * throws Exception when the OnCommitActionalble wasn't set before
     */
    private void throwNotSettedException() {
	if (onCommitActionable == null) {
	    throw new RuntimeException("BoundedValidationLayer cannot add/remove/get actions with not initialized 'onCommitActionable'. use setOnCommitActionable(...) method!!!! ");
	}
    }

    @Override
    public boolean addOnCommitAction(final IOnCommitAction onCommitAction) {
	throwNotSettedException();
	return onCommitActionable.addOnCommitAction(onCommitAction);
    }

    @Override
    public List<IOnCommitAction> getOnCommitActions() {
	throwNotSettedException();
	return onCommitActionable.getOnCommitActions();
    }

    @Override
    public boolean removeOnCommitAction(final IOnCommitAction onCommitAction) {
	throwNotSettedException();
	return onCommitActionable.removeOnCommitAction(onCommitAction);
    }

    public void setRebindable(final IRebindable rebindable) {
	this.rebindable = rebindable;
    }

    public void rebindTo(final IBindingEntity entity) {
	if (rebindable == null) {
	    throw new RuntimeException("rebindible property of the BoundedValidationLayer is not initialized!! ");
	} else {
	    rebindable.rebindTo(entity);
	}
    }

    public void unbound() {
	if (rebindable == null) {
	    throw new RuntimeException("rebindible property of the BoundedValidationLayer is not initialized!! ");
	} else {
	    rebindable.unbound();
	}
    }

    public String getOriginalToolTipText() {
	return originalToolTipText;
    }

    @Override
    public void setEnabled(final boolean enabled) {
	searchAndDisable(getView(), enabled);
	if (JSpinner.class.isAssignableFrom(getView().getClass())) {
	    searchAndDisable(((JSpinner) getView()).getEditor(), enabled);
	}
	super.setEnabled(enabled);
    }

    private void searchAndDisable(final Component topComponent, final boolean enabled) {
	final JComponent jtopComponent = (JComponent) topComponent;
	try {
	    final Method method = Reflector.getMethod(topComponent.getClass(), "setEditable", boolean.class);
	    if (method != null) {
		method.invoke(topComponent, enabled);
	    } else {
		topComponent.setEnabled(enabled);
	    }
	} catch (final Exception e) {
	    topComponent.setEnabled(enabled);
	}

	for (final Component component : jtopComponent.getComponents()) {
	    searchAndDisable(component, enabled);
	}

    }

    /**
     * Provides a tooltip for non-specific bounded validation layer behaviour.
     *
     * @param toolTip
     */
    public void setToolTip(final String toolTip) {
	this.originalToolTipText = toolTip;
	if (rebindable != null) {
	    rebindable.updateToolTip();
	}
    }

    /**
     * Provides a caption for non-specific empty bounded validation layer. Currently caption is only used in autocompleters.
     *
     * @param caption
     */
    public void setCaption(final String caption) {
	if (getView() instanceof JXLayer) {
	    final JXLayer<?> layer = (JXLayer) getView();
	    if (layer.getUI() instanceof SmartComponentUi) {
		final SmartComponentUi smartComponentUi = (SmartComponentUi) layer.getUI();
		smartComponentUi.setCaption(caption);
	    }
	}
    }

    public void updateTooltip() {
	rebindable.updateToolTip();
    }

}
