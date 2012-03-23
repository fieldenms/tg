package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.BoundedJXDatePicker;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.mappings.AbstractLabelPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;

/**
 * Class, that should simplify mapping between {@link Date} property and {@link EntityGridInspector}s column
 *
 * @author Yura
 *
 * @param <T>
 * @param <K>
 */
@SuppressWarnings("unchecked")
public class BoundedDateMapping<T extends AbstractEntity> extends AbstractLabelPropertyColumnMapping<T> {

    private final IOnCommitAction<T>[] onCommitActions;

    /**
     * Creates mapping between {@link Date} property and column. In editor mode it is represented as {@link BoundedJXDatePicker}, while in renderer mode it is simple {@link JLabel}
     * either with {@link ValidationLayer} or without one if validation passed
     *
     * @param propertyName
     * @param columnName
     * @param prefSize
     * @param headerTooltip
     */
    public BoundedDateMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
	super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);
	this.onCommitActions = onCommitActions;
    }

    @Override
    public EditorComponent<? extends BoundedValidationLayer<BoundedJXDatePicker>, JTextField> createBoundedEditorFor(final T entity) {
	final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = EgiUtilities.convert(entity, getEntityGridInspector(), onCommitActions);
	final BoundedValidationLayer<BoundedJXDatePicker> dateComponent = ComponentFactory.createBoundedJXDatePicker(entity, getPropertyName(), "", false, 0L, onCommitActionWrappers);
	dateComponent.getView().getEditor().addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		    getEntityGridInspector().removeEditor();
		}
	    }
	});
	dateComponent.getView().getEditor().addFocusListener(new FocusAdapter() {
	    @Override
	    public void focusGained(final FocusEvent e) {
		dateComponent.getView().getEditor().selectAll();
	    }
	});

	return new EditorComponent<BoundedValidationLayer<BoundedJXDatePicker>, JTextField>(dateComponent, dateComponent.getView().getEditor());
    }

}
