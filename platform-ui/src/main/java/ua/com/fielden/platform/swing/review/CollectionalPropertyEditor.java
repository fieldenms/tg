/**
 *
 */
package ua.com.fielden.platform.swing.review;

import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

/**
 * Creates editor and label for collection properties specifically used as selection criteria.
 * 
 * @author TG Team
 */
public class CollectionalPropertyEditor implements IPropertyEditor {
    private final Logger logger = Logger.getLogger(getClass());

    private final String propertyName;
    private final AbstractEntity<?> entity;
    private final BoundedValidationLayer<?> editor;
    private final JLabel label;
    private final IValueMatcher<?> valueMatcher;

    /**
     * Creates OnFocusLost auto-completer for DynamicEntityQueryCriteiraProperty.
     * 
     * @param entity
     * @param propertyName
     */
    public CollectionalPropertyEditor(//
    final DynamicEntityQueryCriteria<?, ?> entity, //
    final String propertyName) {
	final Date curr = new Date();
	logger.debug("Autocompleter editor creation started...");
	this.entity = entity;
	this.propertyName = propertyName;
	final DynamicProperty dynamicProperty = entity.getEditableProperty(propertyName);
	if (!dynamicProperty.isEntityProperty() || dynamicProperty.isSingle()) {
	    throw new IllegalArgumentException("The " + propertyName + " proeprty of the " + dynamicProperty.getType() + " type "
		    + (dynamicProperty.isEntityProperty() ? " is single" : (" is not entity " + (dynamicProperty.isSingle() ? "" : " and is single"))));
	}
	this.editor = ComponentFactory.createOnFocusLostAutocompleter(entity, propertyName, LabelAndTooltipExtractor.createCaption(dynamicProperty.getTitle()), dynamicProperty.getType(), "key", "desc", dynamicProperty.isSingle() ? null
		: ",", entity.getValueMatcher(propertyName), LabelAndTooltipExtractor.createTooltip(dynamicProperty.getDesc()), PropertyDescriptor.class != dynamicProperty.getType());
	this.label = DummyBuilder.label(dynamicProperty.getTitle());
	this.label.setToolTipText(dynamicProperty.getDesc());
	this.valueMatcher = entity.getValueMatcher(propertyName);
	logger.debug("Autocompleter editor created in..." + (new Date().getTime() - curr.getTime()) + "ms");
    }

    /**
     * creates OnFocusLost auto-completer for list of string properties and label
     * 
     * @param <T>
     * @param entity
     * @param propertyName
     * @param elementType
     * @param caption
     * @param label
     * @param valueMatcher
     * @param isSingle
     *            TODO
     */
    public <T extends AbstractEntity> CollectionalPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final Class<T> elementType, final String caption, final String label, final String tooltip, final IValueMatcher<T> valueMatcher, final boolean isSingle) {
	final Date curr = new Date();
	logger.debug("Autocompleter editor creation started...");
	this.entity = entity;
	this.propertyName = propertyName;
	String valueSeparator = ",";
	if (isSingle) {
	    valueSeparator = null;
	}
	this.editor = ComponentFactory.createOnFocusLostAutocompleter(entity, propertyName, LabelAndTooltipExtractor.createCaption(caption), elementType, "key", "desc", valueSeparator, valueMatcher, LabelAndTooltipExtractor.createTooltip(tooltip), PropertyDescriptor.class != elementType);
	this.label = DummyBuilder.label(label);
	this.label.setToolTipText(tooltip);
	this.valueMatcher = valueMatcher;
	logger.debug("Autocompleter editor created in..." + (new Date().getTime() - curr.getTime()) + "ms");
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
	editor.rebindTo(entity);
    }

    @Override
    public JPanel getDefaultLayout() {
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	panel.add(label);
	panel.add(editor, "growx");
	return panel;
    }

    @Override
    public JComponent getEditor() {
	return editor;
    }

    @Override
    public AbstractEntity<?> getEntity() {
	return entity;
    }

    @Override
    public JLabel getLabel() {
	return label;
    }

    @Override
    public String getPropertyName() {
	return propertyName;
    }

    @Override
    public IValueMatcher<?> getValueMatcher() {
	return valueMatcher;
    }

    @Override
    public boolean isIgnored() {
	if (entity instanceof DynamicEntityQueryCriteria) {
	    final DynamicEntityQueryCriteria dynamicEntity = (DynamicEntityQueryCriteria) entity;
	    return dynamicEntity.isEmptyValue(propertyName);
	} else {
	    return false;
	}
    }
}
