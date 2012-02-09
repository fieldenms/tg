package ua.com.fielden.platform.swing.ei.editors;

import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;

/**
 * Editor for ranges or boolean properties that consists of range-specific label and double-editor ("from" -> "to" or "is" -> "is not").
 *
 * @author TG Team
 *
 */
public class RangePropertyEditor implements IPropertyEditor {

    private static final long serialVersionUID = 1L;

    private AbstractEntity<?> entity;
    private final String propertyName;

    private final JLabel label;
    private final JComponent editor;

    private final IPropertyEditor fromEditor, toEditor;

    private boolean singleSelection;
    private final boolean bool, date;

    public RangePropertyEditor(final IPropertyEditor fromEditor, final IPropertyEditor toEditor) {
	final AbstractEntity<?> fe = fromEditor.getEntity(), te = toEditor.getEntity();
	if (!fe.equals(te) || !fe.getPropertyType(fromEditor.getPropertyName()).equals(te.getPropertyType(toEditor.getPropertyName()))) {
	    throw new RuntimeException("Entity or propertyType is not exactly the same for two editors that form Range/Boolean editor.");
	}
	this.entity = fromEditor.getEntity();
	this.propertyName = EntityDescriptor.removeSuffixes(fromEditor.getPropertyName());

	final String conventionalPropertyName = EntityDescriptor.getPropertyNameWithoutKeyPart(EntityDescriptor.enhanceDynamicCriteriaPropertyEditorKey(fromEditor.getPropertyName(), ((DynamicEntityQueryCriteria) entity).getEntityClass()));

	this.fromEditor = fromEditor;
	this.toEditor = toEditor;

	label = fromEditor.getLabel();
	label.setToolTipText(fromEditor.getLabel().getToolTipText());

	final Class<?> propertyType = fromEditor.getEntity().getPropertyType(fromEditor.getPropertyName());
	bool = (Boolean.class == propertyType) || (boolean.class == propertyType);
	date = (Date.class.isAssignableFrom(propertyType));
	editor = createEditor(bool, conventionalPropertyName);
    }

    private JComponent createEditor(final boolean bool, final String conventionalPropertyName) {
	if (bool) {
	    final JCheckBox yes = ((BoundedValidationLayer<JCheckBox>) fromEditor.getEditor()).getView(), no = ((BoundedValidationLayer<JCheckBox>) toEditor.getEditor()).getView();
	    yes.setText("yes");
	    no.setText("no");
	    no.setMinimumSize(yes.getMinimumSize());
	}
	// constructs singleSelection editor or editor with both left and right criteria (FROM and TO).
	final CritOnly critOnly = AnnotationReflector.getPropertyAnnotation(CritOnly.class, ((DynamicEntityQueryCriteria) entity).getEntityClass(), conventionalPropertyName);
	this.singleSelection = !bool && critOnly != null && Type.SINGLE.equals(critOnly.value());

	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]" + (!singleSelection ? "5[]5[grow]" : ""), "[]"));
	panel.add(fromEditor.getEditor(), "growx");
	if (!singleSelection) {
	    panel.add(createSeparationLabel(bool));
	    panel.add(toEditor.getEditor(), "growx");
	}
	return panel;
    }

    /**
     * Indicates whether "double" property editor has "boolean" nature.
     *
     * @return
     */
    public boolean isBool(){
	return bool;
    }

    /**
     * Indicates whether "double" property editor has "date" nature.
     *
     * @return
     */
    public boolean isDate(){
	return date;
    }

    /**
     * Indicates whether "range" property editor has "single" nature.
     *
     * @return
     */
    public boolean isSingle(){
	return singleSelection;
    }

    /**
     * Creates label to separate double-editor "from <-> to" or "is <-> is not".
     *
     * @param bool
     * @return
     */
    private JLabel createSeparationLabel(final boolean bool) {
	final JLabel toLabel = new JLabel("to");
	final JLabel emptyLabel = new JLabel("");
	emptyLabel.setPreferredSize(toLabel.getPreferredSize());
	emptyLabel.setMinimumSize(toLabel.getMinimumSize());
	final JLabel label = bool ? emptyLabel : toLabel;
	return label;
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
	this.entity = entity;
	fromEditor.bind(entity);
	toEditor.bind(entity);
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
	throw new UnsupportedOperationException("Value matcher are not applicable for ordinary properties.");
    }

    @Override
    public boolean isIgnored() {
	// if both editors are ignored - range editor will be ignored:
	return fromEditor.isIgnored() && toEditor.isIgnored() || (isBool() && !fromEditor.isIgnored() && !toEditor.isIgnored());
    }

    public IPropertyEditor getFromEditor() {
	return fromEditor;
    }

    public IPropertyEditor getToEditor() {
	return toEditor;
    }

}
