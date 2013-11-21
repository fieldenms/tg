/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JTextField;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.egi.models.mappings.AbstractLabelPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Class, representing mapping of some {@link String} property of some {@link AbstractEntity} class to column of related {@link EntityGridInspector}. Uses
 * {@link ComponentFactory#createOnFocusLostAutocompleter(AbstractEntity, String, String, Class, String, String, String, ua.com.fielden.platform.basic.IValueMatcher, String, boolean, ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction...)}
 * method to bound editors to properties. Extends and reuses functionality from {@link AbstractLabelPropertyColumnMapping}, so in order to provide custom logic, one may override
 * particular methods.
 *
 * @author Yura
 *
 * @param <T>
 * @param <K>
 * @param <PropertyClass>
 */
@SuppressWarnings("unchecked")
public class BoundedStringMapping<T extends AbstractEntity> extends AbstractLabelPropertyColumnMapping<T> {

    /**
     * Need this one in order to correctly created bounded autocompleter. Specifically, if the type of the property is AbstractEntity then property name is modified to
     * propertyName.key. This however is not good for creation of the bounded autocompleter.
     */
    private final String originalPropertyName;

    private final Class valueClass;

    private final IValueMatcher valueMatcher;

    private final IOnCommitAction<T>[] onCommitActions;

    private final boolean stringBinding;

    /**
     * Creates instance of mapping, where editors are bounded to properties using
     * {@link ComponentFactory#createOnFocusLostAutocompleter(AbstractEntity, String, String, Class, String, String, String, ua.com.fielden.platform.basic.IValueMatcher, String, boolean, ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction...)}
     * method. Most of the properties are used directly in that method, so view JavaDocs for it please. Array of {@link IOnCommitAction}s would be added to each created
     * autocompleter. Please note, that {@link IOnCommitAction} is just a wrapper around {@link ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction} that
     * provides implementing classes with additional information.<br>
     * <br>
     * Note : much more preferred is usage of {@link PropertyTableModelBuilder} class as it requires less parameters and resolves a lot of them by itself avoiding inconsistency
     * between parameters
     *
     * @param columnName
     * @param prefSize
     * @param headerTooltip
     * @param tooltipGetter
     *            - if null, then {@link DefaultTooltipGetter} is used
     * @param propertyName
     * @param valueClass
     * @param possibleValues
     * @param stringBinding
     *            - true should be passed here if one needs mapping from {@link String} property to column, but with enabled auto-completer selection
     * @param onCommitActions
     */
    public BoundedStringMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Class valueClass, final IValueMatcher valueMatcher, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final boolean stringBinding, final IOnCommitAction<T>... onCommitActions) {
	super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);

	originalPropertyName = propertyName;
	this.valueClass = valueClass;
	this.valueMatcher = valueMatcher;
	this.stringBinding = stringBinding;
	this.onCommitActions = onCommitActions;
    }

    @Override
    public EditorComponent<BoundedValidationLayer<AutocompleterTextFieldLayer>, JTextField> createBoundedEditorFor(final T entity) {
	final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = EgiUtilities.convert(entity, getEntityGridInspector(), onCommitActions);

	final BoundedValidationLayer<AutocompleterTextFieldLayer> boundedLayer = ComponentFactory.createOnFocusLostAutocompleter(entity, originalPropertyName, "", valueClass, "key", secondaryExpressions(valueClass), highlightProperties(valueClass), (String) null, valueMatcher, (String) null, stringBinding, onCommitActionWrappers); //
	boundedLayer.getView().getView().addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		    getEntityGridInspector().removeEditor();
		}
	    }
	});

	return new EditorComponent<BoundedValidationLayer<AutocompleterTextFieldLayer>, JTextField>(boundedLayer, (JTextField) boundedLayer.getView().getView());
    }

    private Set<String> highlightProperties(final Class entityType) {
	final List<Field> keyMembers = Finder.getKeyMembers(entityType);
	final Set<String> highlightProps = new HashSet<>();
	highlightProps.add("key");
	for(final Field keyMember : keyMembers) {
	    highlightProps.add(keyMember.getName());
	}
	return highlightProps;
    }

    @SuppressWarnings("unchecked")
    private Pair<String, String>[] secondaryExpressions(final Class entityType) {
	final List<Pair<String, String>> props = new ArrayList<>();
	final List<Field> keyMembers = Finder.getKeyMembers(entityType);
	if(keyMembers.size() > 1) {
	    for (final Field keyMember : keyMembers) {
		props.add(new Pair<String, String>(TitlesDescsGetter.getTitleAndDesc(keyMember.getName(), entityType).getKey(), keyMember.getName()));
	    }
	}
	if (EntityUtils.hasDescProperty(entityType)) {
	    props.add(new Pair<String, String>(TitlesDescsGetter.getTitleAndDesc("desc", entityType).getKey(), "desc"));
	}
	return props.toArray(new Pair[0]);
    }

}
