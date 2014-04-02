package ua.com.fielden.platform.swing.ei.editors.development;

import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

/**
 * Editor for an entity property represented by a collection.
 * 
 * TODO The current implementation is rather a stub serving as a read-only place holder.
 * 
 * @author 01es
 * 
 */
public class CollectionalPropertyEditor implements IPropertyEditor {

    private AbstractEntity<?> entity;
    private final String propertyName;

    private final JLabel label;
    private final JTextField editor;

    public CollectionalPropertyEditor(final AbstractEntity<?> entity, final String propertyName) {
        this.entity = entity;
        this.propertyName = propertyName;

        final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

        label = DummyBuilder.label(titleAndDesc.getKey());
        label.setToolTipText(titleAndDesc.getValue());
        editor = createEditor(entity, entity.getProperty(propertyName));
    }

    private JTextField createEditor(final AbstractEntity<?> entity, final MetaProperty metaProperty) {
        final JTextField editor = new JTextField();
        editor.setText(((Collection<?>) entity.get(propertyName)).size() + "");
        editor.setEditable(false);
        editor.setHorizontalAlignment(SwingConstants.RIGHT);
        editor.setToolTipText(metaProperty.getDesc());
        return editor;
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
        this.entity = entity;
        SwingUtilitiesEx.invokeLater(new Runnable() {
            @Override
            public void run() {
                editor.setText(((Collection<?>) entity.get(propertyName)).size() + "");
            }
        });
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

    public JComponent getEditor() {
        return editor;
    }

    @Override
    public JPanel getDefaultLayout() {
        final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
        panel.add(label);
        panel.add(editor, "growx");
        return panel;
    }

    @Override
    public IValueMatcher<?> getValueMatcher() {
        throw new UnsupportedOperationException("Value matcher are not applicable for collectional properties.");
    }
}
