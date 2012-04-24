package ua.com.fielden.platform.swing.components.bind.development;

import java.beans.PropertyChangeEvent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyConnectorAdapter;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyValidationResultsChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.ReadOnlyLabel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;

/**
 * Temporary class, which behaves very similar to {@link LabelConnector}, but uses {@link HierarchicalPropertyChangeListener} and thus enables read-only far-binding for properties
 *
 * @author Yura
 */
public final class LabelConnector extends PropertyConnectorAdapter implements IRebindable {
    private final ReadOnlyLabel label;
    private final BoundedValidationLayer<? extends ReadOnlyLabel> boundedValidationLayer;
    private HierarchicalPropertyChangeListener topListener = null;
    private final ShowingStrategy showingStrategy;

    LabelConnector(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends ReadOnlyLabel> boundedValidationLayer, final ShowingStrategy showingStrategy) {
	// initiate Entity and PropertyName
	super(entity, propertyName);

	// initiate boundedValidationLayer
	if (boundedValidationLayer == null) {
	    throw new NullPointerException("The validationLayer must not be null.");
	}
	this.showingStrategy = showingStrategy;
	this.boundedValidationLayer = boundedValidationLayer;

	// this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer);

	// initiateEditableComponent
	this.label = boundedValidationLayer.getView();
	if (label == null) {
	    throw new NullPointerException("The label must not be null.");
	}

	addOwnEntitySpecificListeners();
	Rebinder.initiateReconnectables(entity, this, boundedValidationLayer);

	updateStates();
    }

    @Override
    public void addOwnEntitySpecificListeners() {
	topListener = HierarchicalPropertyChangeListener.addListenersToPropertyHierarchy(Rebinder.getActualEntity(entity), propertyName, new SubjectValueChangeHandler(), new PropertyValidationResultsChangeListener(this.boundedValidationLayer));
	// if (!this.propertyName.contains(".")) {
	// Rebinder.addMetaPropertySpecificListeners(this.entity, this.propertyName, null, null, this.requiredChangeListener);
	// }
    }

    @Override
    public void removeOwnEntitySpecificListeners() {
	// if (!this.propertyName.contains(".")) {
	// Rebinder.removeMetaPropertySpecificListeners(this.entity, this.propertyName, null, null, this.requiredChangeListener);
	// }
	HierarchicalPropertyChangeListener.removeListenersFromHierarchy(topListener);
	topListener = null;
    }

    // Synchronization ********************************************************

    private class MissingConverterException extends Exception {
	private static final long serialVersionUID = 1L;
    }

    /**
     * Returns first bounded Property on which validation has failed(gets from BufferedPropertyWrapper or straight from Connector)
     *
     * @return
     */
    @Override
    public MetaProperty boundedMetaProperty() {
	return entity instanceof AbstractEntity ? EntityUtils.findFirstFailedMetaProperty((AbstractEntity<?>) Rebinder.getActualEntity(entity), propertyName)
		: null;
    }

    @Override
    public void updateByActualOrLastIncorrectValue() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		try {
		    final MetaProperty boundedMetaProperty = boundedMetaProperty();
		    if (boundedMetaProperty == null || boundedMetaProperty.isValid()) {
			// TODO doesn't work for non-AE "entity" !!!
			label.setTextFromBinding(EntityUtils.getLabelText((AbstractEntity<?>) entity, propertyName, showingStrategy)); // TODO metaProperty dependent code!
		    } else {
			final Object lastInvalidValue = boundedMetaProperty.getLastInvalidValue();
			final Converter converter = EntityUtils.chooseConverterBasedUponPropertyType(boundedMetaProperty, showingStrategy);
			if (lastInvalidValue != null && (!lastInvalidValue.getClass().equals(String.class)) && converter == null) {
			    throw new MissingConverterException();
			}
			final String str = (converter != null) ? converter.convertToString(lastInvalidValue) : (String) lastInvalidValue;// .toString();
			label.setTextFromBinding(str == null ? "" : str);
		    }
		} catch (final MissingConverterException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    //    @Override
    //    public void updateToolTip() {
    //	try {
    //	    // TODO doesn't work for non-AE "entity" !!!
    //	    final String newText = EntityUtils.getLabelText((AbstractEntity<?>) entity, propertyName, ShowingStrategy.DESC_ONLY); // previously was KEY_AND_DESC, but now should be
    //																  // DESC_ONLY.
    //	    SwingUtilitiesEx.invokeLater(new Runnable() {
    //		public void run() {
    //		    if (boundedMetaProperty() == null) {
    //			label.setToolTipText((!StringUtils.isEmpty(newText)) ? newText : boundedValidationLayer.getOriginalToolTipText());
    //			return;
    //		    }
    //		    label.setToolTipText(boundedMetaProperty().isValid() ? (boundedMetaProperty().hasWarnings() ? boundedMetaProperty().getFirstWarning().getMessage()
    //			    : ((!StringUtils.isEmpty(newText)) ? newText : boundedValidationLayer.getOriginalToolTipText())) : boundedMetaProperty().getFirstFailure().getMessage());
    //		}
    //	    });
    //	} catch (final Exception e) {
    //	    e.printStackTrace();
    //	}
    //    }

    @Override
    public void updateToolTip() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		label.setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), true));
	    }
	});
    }

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    @Override
    public void updateEditable() {
	final MetaProperty property = boundedMetaProperty();
	if (property != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    label.setEnabled(property.isEditable());
		}
	    });
	}
    }

    /**
     * updates the "required" state of the component based on the "required" state of the bound Property
     */
    public void updateRequired() {
	// no updating by "requirement" is needed for Labels!
    }

    @Override
    public void updateValidationResult() {
	Binder.updateValidationUIbyMetaPropertyValidationState(boundedMetaProperty(), this.boundedValidationLayer);
    }

    @Override
    public void rebindTo(final IBindingEntity entity) {
	if (entity == null) {
	    new IllegalArgumentException("the component cannot be reconnected to the Null entity!!").printStackTrace();
	} else {
	    unbound();
	    setEntity(entity);
	    addOwnEntitySpecificListeners();
	    updateStates();
	}
    }

    @Override
    public void unbound() {
	removeOwnEntitySpecificListeners();
    }

    /**
     * Handles changes in the subject value and updates this document - if necessary.
     * <p>
     *
     * Document changes update the subject text and result in a subject property change. Most of these changes will just reflect the former subject change. However, in some cases
     * the subject may modify the text set, for example to ensure upper case characters. This method reduces the number of document updates by checking the old and new text. If the
     * old and new text are equal or both null, this method does nothing.
     * <p>
     *
     * Since subject changes as a result of a document change may not modify the write-locked document immediately, we defer the update if necessary using
     * <code>SwingUtilities.invokeLater</code>.
     * <p>
     *
     * See the TextComponentConnector's JavaDoc class comment for the limitations of the deferred document change.
     */
    private final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {

	/**
	 * The subject value has changed; updates the document
	 *
	 * @param evt
	 *            the event to handle
	 * @throws
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		public void run() {
		    updateByActualOrLastIncorrectValue();
		    if (boundedMetaProperty() != null) {
			updateValidationResult();
		    }
		    updateToolTip();
		}
	    });
	}

    }
}
