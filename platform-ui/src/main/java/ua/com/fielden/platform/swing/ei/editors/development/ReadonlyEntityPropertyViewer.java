/**
 *
 */
package ua.com.fielden.platform.swing.ei.editors.development;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.ReadOnlyLabel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;
import ua.com.fielden.platform.utils.Pair;

/**
 * This a {@link IPropertyEditor} wrapper for read only entity properties, which can be used for binding <i>far-bound</i> properties that require a display only functionality.
 *
 * @author TG Team
 */
public class ReadonlyEntityPropertyViewer implements IPropertyEditor {

    private AbstractEntity<?> entity;
    private final String propertyName;

    private final JLabel label;
    private final BoundedValidationLayer<ReadOnlyLabel> editor;

    public ReadonlyEntityPropertyViewer(final AbstractEntity<?> entity, final String propertyName) {
	this.entity = entity;
	this.propertyName = propertyName;

	final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

	label = DummyBuilder.label(titleAndDesc.getKey());
	label.setToolTipText(titleAndDesc.getValue());
	editor = ComponentFactory.createLabel(entity, propertyName, titleAndDesc.getValue(), ShowingStrategy.KEY_ONLY);
    }

    @Override
    public BoundedValidationLayer<ReadOnlyLabel> getEditor() {
	return editor;
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
	this.entity = entity;
	getEditor().rebindTo(entity);
    }

    @Override
    public AbstractEntity<?> getEntity() {
	return entity;
    }

    @Override
    public String getPropertyName() {
	return propertyName;
    }

    public JLabel getLabel() {
	return label;
    }

    @Override
    public JPanel getDefaultLayout() {
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	panel.add(label);
	panel.add(getEditor(), "growx");
	return panel;
    }

    @Override
    public IValueMatcher<?> getValueMatcher() {
	throw new UnsupportedOperationException("Value matcher are not applicable for readonly editors.");
    }
}
