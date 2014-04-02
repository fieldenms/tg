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
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;

/**
 * Abstract property editor for entities. Do not contains editor, because it could be created in several ways (i.e. with string binding or without etc.)
 * 
 * @author TG Team
 */
public abstract class AbstractEntityPropertyEditor implements IPropertyEditor {

    private AbstractEntity<?> entity;
    private final String propertyName;

    private final IValueMatcher<?> valueMatcher;

    private final JLabel label;

    public AbstractEntityPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher) {
        this.entity = entity;
        this.propertyName = propertyName;
        this.valueMatcher = valueMatcher;

        final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

        label = DummyBuilder.label(titleAndDesc.getKey());
        label.setToolTipText(titleAndDesc.getValue());
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

    public abstract BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor();

    @Override
    public JPanel getDefaultLayout() {
        final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
        panel.add(label);
        panel.add(getEditor(), "growx");
        return panel;
    }

    @Override
    public IValueMatcher<?> getValueMatcher() {
        return valueMatcher;
    }
}
