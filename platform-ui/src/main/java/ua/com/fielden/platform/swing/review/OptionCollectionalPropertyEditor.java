/**
 *
 */
package ua.com.fielden.platform.swing.review;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * Creates editor and label for collection properties specifically used as selection criteria. Please notice that editor is OptionAutocompleter.
 * 
 * @author TG Team
 */
public class OptionCollectionalPropertyEditor implements IPropertyEditor {

    private final String propertyName;
    private final AbstractEntity entity;
    private final BoundedValidationLayer editor;
    private final JLabel label;
    private final IValueMatcher valueMatcher;

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
     * @param single
     *            TODO
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OptionCollectionalPropertyEditor(//
    final DynamicEntityQueryCriteria<?, ?> entity, //
    final String propertyName, //
    final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController,//
    final ILocatorConfigurationRetriever locatorRetriever) {
	this.entity = entity;
	this.propertyName = propertyName;
	final DynamicProperty dynamicProperty = entity.getEditableProperty(propertyName);
	if (!dynamicProperty.isEntityProperty() || dynamicProperty.isSingle()) {
	    throw new IllegalArgumentException("The " + propertyName + " proeprty of the " + dynamicProperty.getType() + " type "
		    + (dynamicProperty.isEntityProperty() ? " is single" : (" is not entity " + (dynamicProperty.isSingle() ? "" : " and is single"))));
	}
	this.editor = ComponentFactory.createOnFocusLostOptionAutocompleter(entity, propertyName, LabelAndTooltipExtractor.createCaption(dynamicProperty.getTitle()), dynamicProperty.getType(), "key", "desc", dynamicProperty.isSingle() ? null
		: ",", entity.getValueMatcher(propertyName), entityMasterFactory, LabelAndTooltipExtractor.createTooltip(dynamicProperty.getDesc()), !PropertyDescriptor.class.equals(dynamicProperty.getType()), entity.getCriteriaEntityFactory(), entity.getValueMatcherFactory(), entity.getDaoFactory(), locatorController, locatorRetriever);
	this.label = DummyBuilder.label(dynamicProperty.getTitle());
	this.label.setToolTipText(dynamicProperty.getDesc());
	this.valueMatcher = entity.getValueMatcher(propertyName);
    }

    public OptionCollectionalPropertyEditor(//
    final AbstractEntity entity, final IValueMatcherFactory valueMatcherFactory, final IDaoFactory daoFactory, final String propertyName, //
    final Class elementType,//
    final String caption,//
    final String label, //
    final String tooltip,//
    final IValueMatcher valueMatcher,//
    final IEntityMasterManager entityMasterFactory, //
    final ILocatorConfigurationController locatorController, //
    final ILocatorConfigurationRetriever locatorRetriever, final boolean single) {
	this.entity = entity;
	this.propertyName = propertyName;
	this.editor = ComponentFactory.createOnFocusLostOptionAutocompleter(entity, propertyName, caption, elementType, "key", "desc", single ? null : ",", valueMatcher, entityMasterFactory, tooltip, !PropertyDescriptor.class.equals(elementType), entity.getEntityFactory(), valueMatcherFactory, daoFactory, locatorController, locatorRetriever);
	this.label = DummyBuilder.label(label);
	this.label.setToolTipText(tooltip);
	this.valueMatcher = valueMatcher;
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
