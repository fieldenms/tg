/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.builders;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase.UPPER_CASE;
import static ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder.EditingPolicy.ALWAYS_EDITABLE;
import static ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder.EditingPolicy.BY_META_PROPERTY;
import static ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder.NavigationPolicy.ALWAYS_NAVIGABLE;
import static ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals.NO_TOTALS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.components.textfield.UpperCaseTextField;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.EgiColoringScheme;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;
import ua.com.fielden.platform.swing.egi.models.mappings.PropertyColumnMappingByExpression;
import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.BoundedBooleanMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.BoundedDateMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.BoundedDecimalMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.BoundedIntegerMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.BoundedPlainStringMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.BoundedStringMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.IOnCommitAction;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * {@link PropertyTableModel} builder with convenience methods for creating editable mappings for {@link AbstractEntity} <br>
 * <br>
 * Note : some features are not implemented well, not tested yet, so it may not work properly
 * 
 * @author Yura
 * 
 * @param <T>
 * @param <K>
 */
@SuppressWarnings("unchecked")
public class PropertyTableModelBuilder<T extends AbstractEntity> extends AbstractTableModelBuilder<T, PropertyTableModel<T>, PropertyTableModelBuilder<T>> {
    private final Logger logger = Logger.getLogger(getClass());

    /**
     * Plain constructor directly calling {@link PropertyTableModelBuilder#PropertyTableModelBuilder4Ae(Class)}
     * 
     * @param entityClass
     */
    public PropertyTableModelBuilder(final Class<T> entityClass) {
        super(entityClass);
    }

    /**
     * Refer to
     * {@link #addEditable(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, List, IOnCommitAction...)}
     * method JavaDocs (parameters are set as it is mentioned in "by default").
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditable(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final IValueMatcher valueMatcher, final Action clickAction, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditable(propertyName, columnName, prefSize, headerTooltip, null, BY_META_PROPERTY, ALWAYS_NAVIGABLE, valueMatcher, clickAction, NO_TOTALS, null, onCommitActions);
    }

    /**
     * Refer to
     * {@link #addEditable(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, List, IOnCommitAction...)}
     * method JavaDocs (parameters are set as it is mentioned in "by default").
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditable(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditable(propertyName, columnName, prefSize, headerTooltip, null, BY_META_PROPERTY, ALWAYS_NAVIGABLE, null, null, NO_TOTALS, null, onCommitActions);
    }

    /**
     * Refer to
     * {@link #addEditable(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, List, IOnCommitAction...)}
     * method JavaDocs (parameters are set as it is mentioned in "by default").
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditable(final String propertyName, final Integer prefSize, final IOnCommitAction<T>... onCommitActions) {
        // notSurrogatedMappingsCount++;
        return (BUILDER_TYPE) addEditable(propertyName, null, prefSize, null, null, BY_META_PROPERTY, ALWAYS_NAVIGABLE, null, null, NO_TOTALS, null, onCommitActions);
    }

    /**
     * Refer to
     * {@link #addEditable(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, List, IOnCommitAction...)}
     * method JavaDocs (parameters are set as it is mentioned in "by default").
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditable(final String propertyName, final String columnName, final String headerTooltip, final IValueMatcher valueMatcher, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditable(propertyName, columnName, null, headerTooltip, null, BY_META_PROPERTY, ALWAYS_NAVIGABLE, valueMatcher, null, NO_TOTALS, null, onCommitActions);
    }

    /**
     * Refer to
     * {@link #addEditable(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, List, IOnCommitAction...)}
     * method JavaDocs (parameters are set as it is mentioned in "by default").
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditable(final String propertyName, final String columnName, final String headerTooltip, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditable(propertyName, columnName, null, headerTooltip, null, BY_META_PROPERTY, ALWAYS_NAVIGABLE, null, null, NO_TOTALS, null, onCommitActions);
    }

    /**
     * Creates editable mapping, depending on the property type it could be {@link BoundedStringMapping}, {@link BoundedBooleanMapping} or {@link BoundedDateMapping}. <br>
     * <br>
     * Note : if property is of {@link AbstractEntity} class, then
     * {@link BoundedStringMapping#BoundedStringMapping(String, String, Integer, String, ITooltipGetter, Class, List, boolean, IOnCommitAction...)} constructor will be with invoked
     * 'stringBinding' parameter set to 'false' to create mapping with auto-completer for {@link AbstractEntity}.<br>
     * But when property is of {@link String} class, then
     * {@link BoundedStringMapping#BoundedStringMapping(String, String, Integer, String, ITooltipGetter, Class, List, boolean, IOnCommitAction...)} constructor will be invoked with
     * 'stringBinding' parameter set to 'true' to create mapping from {@link String} property but with auto-completer selection of values.
     * 
     * @param propertyName
     * @param columnName
     *            - if empty, this builder will attempt to take columnName from {@link Title#value()}
     * @param prefSize
     *            - null by default
     * @param headerTooltip
     *            - null by default. If empty, this builder will attempt to take headerTooltip from {@link Title#desc()}
     * @param tooltipGetter
     *            - null by default
     * @param editingPolicy
     *            - {@link EditingPolicy#BY_META_PROPERTY} by default
     * @param navigationPolicy
     *            - {@link NavigationPolicy#ALWAYS_NAVIGABLE} by default
     * @param possibleValues
     *            - used only in case of mapping for {@link AbstractEntity} with autocompleter editor. Empty by default.
     * @param onCommitActions
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditable(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final IValueMatcher valueMatcher, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("Empty property name should not be used to create editable property mapping.");
        }
        if (PropertyTypeDeterminator.isDotNotation(propertyName)) {
            throw new IllegalArgumentException("Dot-notation property name should not be used to create editable property mapping.");
        }
        if (propertyName.contains("()")) {
            throw new IllegalArgumentException("Function name should not be used to create editable property mapping.");
        }
        final Class<?> propertyClass = PropertyTypeDeterminator.determineClass(getEntityClass(), propertyName, true, false);

        AbstractPropertyColumnMapping<T> propertyColumnMapping = null;
        if (AbstractEntity.class.isAssignableFrom(propertyClass)) {
            propertyColumnMapping = createStringPropertyMapping(propertyName, columnName, prefSize, headerTooltip, tooltipGetter, editingPolicy, navigationPolicy, valueMatcher, false, clickAction, columnTotals, aggregationFunction, propertyClass, onCommitActions);
        } else if (String.class.isAssignableFrom(propertyClass)) {
            propertyColumnMapping = createStringPropertyMapping(propertyName, columnName, prefSize, headerTooltip, tooltipGetter, editingPolicy, navigationPolicy, valueMatcher, true, clickAction, columnTotals, aggregationFunction, propertyClass, onCommitActions);
        } else if (Boolean.class.isAssignableFrom(propertyClass) || boolean.class.isAssignableFrom(propertyClass)) {
            propertyColumnMapping = createBooleanPropertyMapping(propertyName, columnName, prefSize, headerTooltip, tooltipGetter, editingPolicy, navigationPolicy, clickAction, columnTotals, aggregationFunction, onCommitActions);
        } else if (Date.class.isAssignableFrom(propertyClass)) {
            propertyColumnMapping = createDatePropertyMapping(propertyName, columnName, prefSize, headerTooltip, tooltipGetter, editingPolicy, navigationPolicy, clickAction, columnTotals, aggregationFunction, onCommitActions);
        } else if (Money.class.isAssignableFrom(propertyClass) || BigDecimal.class == propertyClass || double.class == propertyClass) {
            propertyColumnMapping = createDecimalPropertyMapping(propertyName, columnName, prefSize, headerTooltip, tooltipGetter, editingPolicy, navigationPolicy, clickAction, columnTotals, aggregationFunction, onCommitActions);
        } else if (Integer.class == propertyClass || int.class == propertyClass) {
            propertyColumnMapping = createIntegerPropertyMapping(propertyName, columnName, prefSize, headerTooltip, tooltipGetter, editingPolicy, navigationPolicy, clickAction, columnTotals, aggregationFunction, onCommitActions);
        } else {
            throw new UnsupportedOperationException("property of type " + propertyClass.getName() + " is not supported yet");
        }
        return (BUILDER_TYPE) add(propertyColumnMapping);
    }

    private AbstractPropertyColumnMapping<T> createStringPropertyMapping(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final IValueMatcher valueMatcher, final boolean stringBinding, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final Class<?> propertyClass, final IOnCommitAction<T>... onCommitActions) {
        return new BoundedStringMapping<T>(getEntityClass(), propertyName, columnName, prefSize, headerTooltip, tooltipGetter, !stringBinding ? propertyClass
                : AbstractEntity.class, valueMatcher, clickAction, columnTotals, aggregationFunction, stringBinding, onCommitActions) {
            @Override
            public boolean isPropertyEditable(final T entity) {
                if (!stringBinding) {
                    return editingPolicy.isEditable(entity, propertyName);
                } else {
                    return BY_META_PROPERTY.equals(editingPolicy) ? true : editingPolicy.isEditable(entity, propertyName);
                }
            }

            @Override
            public boolean isNavigableTo(final T entity) {
                return navigationPolicy.isNavigable();
            }
        };
    }

    private AbstractPropertyColumnMapping<T> createBooleanPropertyMapping(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
        return new BoundedBooleanMapping<T>(getEntityClass(), propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, onCommitActions) {
            @Override
            public boolean isPropertyEditable(final T entity) {
                return editingPolicy.isEditable(entity, propertyName);
            }

            @Override
            public boolean isNavigableTo(final T entity) {
                return navigationPolicy.isNavigable();
            }
        };
    }

    private AbstractPropertyColumnMapping<T> createDatePropertyMapping(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
        return new BoundedDateMapping<T>(getEntityClass(), propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, onCommitActions) {
            @Override
            public boolean isPropertyEditable(final T entity) {
                return editingPolicy.isEditable(entity, propertyName);
            }

            @Override
            public boolean isNavigableTo(final T entity) {
                return navigationPolicy.isNavigable();
            }
        };
    }

    private AbstractPropertyColumnMapping<T> createDecimalPropertyMapping(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
        return new BoundedDecimalMapping<T>(getEntityClass(), propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, onCommitActions) {
            @Override
            public boolean isPropertyEditable(final T entity) {
                return editingPolicy.isEditable(entity, propertyName);
            }

            @Override
            public boolean isNavigableTo(final T entity) {
                return navigationPolicy.isNavigable();
            }
        };
    }

    private AbstractPropertyColumnMapping<T> createIntegerPropertyMapping(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final IOnCommitAction<T>... onCommitActions) {
        return new BoundedIntegerMapping<T>(getEntityClass(), propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, onCommitActions) {
            @Override
            public boolean isPropertyEditable(final T entity) {
                return editingPolicy.isEditable(entity, propertyName);
            }

            @Override
            public boolean isNavigableTo(final T entity) {
                return navigationPolicy.isNavigable();
            }
        };
    }

    /**
     * Adds mapping for plain {@link String} property which uses {@link JTextField} as editor
     * 
     * @param propertyName
     * @param columnName
     *            - if empty, this builder will attempt to take columnName from {@link Title#value()}
     * @param prefSize
     * @param headerTooltip
     *            - if empty, this builder will attempt to take headerTooltip from {@link Title#desc()}
     * @param tooltipGetter
     * @param editingPolicy
     *            - {@link EditingPolicy#ALWAYS_EDITABLE} by default
     * @param navigationPolicy
     *            - {@link NavigationPolicy#ALWAYS_NAVIGABLE} by default
     * @param upperCase
     *            - indicates whether bounded {@link UpperCaseTextField} or simple {@link JTextField} should be created
     * @param onCommitActions
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditableString(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final EditingPolicy editingPolicy, final NavigationPolicy navigationPolicy, final EditorCase editorCase, final IOnCommitAction<T>... onCommitActions) {
        // final Pair<String, String> titleAndDesc = getFullTitleAndDesc(propertyName, getEntityClass());
        // columnName = !isEmpty(columnName) ? columnName : titleAndDesc.getKey();
        // headerTooltip = !isEmpty(headerTooltip) ? headerTooltip : titleAndDesc.getValue();
        return (BUILDER_TYPE) add(new BoundedPlainStringMapping<T>(getEntityClass(), propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, editorCase, onCommitActions) {
            @Override
            public boolean isNavigableTo(final T entity) {
                return navigationPolicy.isNavigable();
            }

            @Override
            public boolean isPropertyEditable(final T entity) {
                return editingPolicy.isEditable(entity, propertyName);
            }
        });
    }

    /**
     * Directly calls
     * {@link #addEditableString(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, IOnCommitAction...)}
     * with some parameters set by default
     * 
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditableString(final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditableString(propertyName, columnName, prefSize, headerTooltip, null, null, NO_TOTALS, null, ALWAYS_EDITABLE, ALWAYS_NAVIGABLE, UPPER_CASE, onCommitActions);
    }

    /**
     * Directly calls
     * {@link #addEditableString(String, String, Integer, String, ITooltipGetter, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.EditingPolicy, ua.com.fielden.platform.swing.inspector.models.builders.PropertyTableModelBuilder.NavigationPolicy, IOnCommitAction...)}
     * with some parameters set by default
     * 
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditableString(final String propertyName, final String columnName, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditableString(propertyName, columnName, null, null, null, null, NO_TOTALS, null, ALWAYS_EDITABLE, ALWAYS_NAVIGABLE, UPPER_CASE, onCommitActions);
    }

    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addEditableString(final String propertyName, final int prefSize, final IOnCommitAction<T>... onCommitActions) {
        return (BUILDER_TYPE) addEditableString(propertyName, null, prefSize, null, null, null, ColumnTotals.NO_TOTALS, null, EditingPolicy.BY_META_PROPERTY, NavigationPolicy.ALWAYS_NAVIGABLE, EditorCase.MIXED_CASE, onCommitActions);
    }

    // ====================================================================
    // Methods for adding readonly mappings
    // ====================================================================

    /**
     * Same as {@link #addReadonly(String, String, Integer, String)}, but with provided propertyType.
     * 
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final String columnName, final Integer preferredSize, final String headerTooltip) {
        return (BUILDER_TYPE) addReadonly(propertyName, columnName, preferredSize, headerTooltip, null, null, NO_TOTALS, null);
    }

    /**
     * Adds readonly mapping using {@link #addReadonly(String, String, Integer, String)} method and associates specified {@link IColouringScheme}
     * 
     * @param <BUILDER_TYPE>
     * @param propertyName
     * @param columnName
     * @param preferredSize
     * @param headerTooltip
     * @param propertyColoringScheme
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final String columnName, final Integer preferredSize, final String headerTooltip, final IColouringScheme<T> propertyColoringScheme) {
        addPropertyColoring(propertyName, propertyColoringScheme);
        return (BUILDER_TYPE) addReadonly(propertyName, columnName, preferredSize, headerTooltip, null, null, NO_TOTALS, null);
    }

    /**
     * Same as {@link #addReadonly(String, String, Integer, String)}, but with provided propertyType.
     * 
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final String columnName, final Integer preferredSize, final String headerTooltip, final Action clickAction) {
        return (BUILDER_TYPE) addReadonly(propertyName, columnName, preferredSize, headerTooltip, null, clickAction, NO_TOTALS, null);
    }

    /**
     * Same as {@link #addReadonly(String, String)}, but with provided propertyType.
     * 
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final String columnName) {
        return (BUILDER_TYPE) addReadonly(propertyName, columnName, null, null, null, null, NO_TOTALS, null);
    }

    /**
     * Add a readonly column where column name and a hint are determined from property annotations.
     * 
     * @param propertyName
     * @param preferredSize
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final Integer preferredSize) {
        return (BUILDER_TYPE) addReadonly(propertyName.trim(), null, preferredSize, /*
                                                                                    * StringUtils.
                                                                                    * isEmpty
                                                                                    * (
                                                                                    * headerTooltip
                                                                                    * )
                                                                                    * ?
                                                                                    * columnName
                                                                                    * :
                                                                                    * headerTooltip
                                                                                    */null, null, null, NO_TOTALS, null);
    }

    /**
     * Adds readonly column using {@link #addReadonly(String, Integer)} method and also associates specified {@link IColouringScheme} with specified property.
     * 
     * @param <BUILDER_TYPE>
     * @param propertyName
     * @param preferredSize
     * @param propertyColoringScheme
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final Integer preferredSize, final IColouringScheme<T> propertyColoringScheme) {
        addPropertyColoring(propertyName, propertyColoringScheme);
        return (BUILDER_TYPE) addReadonly(propertyName, preferredSize);
    }

    /**
     * Adds a readonly column where column name and a hint are determined from property annotations as defined for the propertyOwnerType.
     * <p>
     * This method is useful where property is not present on the target table model entity type (e.g. {@link EntityAggregates}).
     * 
     * @param <BUILDER_TYPE>
     * @param propertyName
     * @param preferredSize
     * @param propertyOwnerType
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final Integer preferredSize, final Class<? extends AbstractEntity> propertyOwnerType) {
        // final Pair<String, String> titleAndDesc = getFullTitleAndDesc(propertyName.trim(), propertyOwnerType);
        // final String columnName = titleAndDesc.getKey();
        // final String headerTooltip = titleAndDesc.getValue();
        // notSurrogatedMappingsCount++;
        // final String columnName="XXXXXXXXXXXXXXXXXX", headerTooltip = "YYYYYYYYYYYYYYYYY";
        return (BUILDER_TYPE) addReadonly(propertyName.trim(), /* columnName */null, preferredSize, /*
                                                                                                     * StringUtils.
                                                                                                     * isEmpty
                                                                                                     * (
                                                                                                     * headerTooltip
                                                                                                     * )
                                                                                                     * ?
                                                                                                     * columnName
                                                                                                     * :
                                                                                                     * headerTooltip
                                                                                                     */null, null, null, NO_TOTALS, null);
    }

    /**
     * Adds a readonly column where column name and a hint are determined from property annotations as defined for the propertyOwnerType. This column will be displayed with totals
     * as specified by {@link ColumnTotals} parameter.
     * <p>
     * This method is useful where property is not present on the target table model entity type (e.g. {@link EntityAggregates}).
     * 
     * @param <BUILDER_TYPE>
     * @param propertyName
     * @param preferredSize
     * @param propertyOwnerType
     * @return
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final Integer preferredSize, final Class<? extends AbstractEntity> propertyOwnerType, final ColumnTotals columnTotals) {
        return (BUILDER_TYPE) addReadonly(propertyName.trim(), null, preferredSize, null, null, null, columnTotals, null);
    }

    /**
     * Determines type of property, and creates instance of either {@link ReadonlyPropertyColumnMapping} (if passed propertyName represents {@link Observable} property) or
     * {@link PropertyColumnMappingByExpression} (if passed propertyName is not {@link Observable}, then trying to calculate values as expressions) for this property using passed
     * parameters. Then adds this instance to mappings list and returns itself.
     * 
     * @param propertyName
     * @param columnName
     *            - if empty, this builder will attempt to take columnName from {@link Title#value()}
     * @param preferredSize
     *            - could be null
     * @param headerTooltip
     *            - - null by default. If empty, this builder will attempt to take headerTooltip from {@link Title#desc()}
     * @param tooltipGetter
     *            - could be null
     * @param clickAction
     *            - action which will occur upon double-click on column. Entity, which double-click occurred on, can be obtained via {@link ActionEvent#getSource()} method (
     *            {@link ActionEvent} instance is passed to {@link ActionListener#actionPerformed(ActionEvent)} method).
     * 
     * @return itself
     */
    public <BUILDER_TYPE extends PropertyTableModelBuilder<T>> BUILDER_TYPE addReadonly(final String propertyName, final String columnName, final Integer preferredSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction) {
        return (BUILDER_TYPE) add(createReadonly(propertyName, columnName, preferredSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction));

    }

    protected AbstractPropertyColumnMapping<T> createReadonly(final String propertyName, final String columnName, final Integer preferredSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction) {
        if (isEmpty(propertyName)) {
            // empty property name means that class's values should be used as column values.
            return new ReadonlyPropertyColumnMapping<T>(propertyName, getEntityClass(), columnName, preferredSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, true);
        }

        Field field;
        try {
            field = Finder.findFieldByName(getEntityClass(), propertyName);
        } catch (final IllegalArgumentException e) {
            // propertyName dot-notation was incorrectly constructed. No field or method could be found.
            logger.error("Could not find field/method " + propertyName + " in class " + getEntityClass().getSimpleName()
                    + ". Please correct dot-notation property/method name in accordance with real domain model.");
            e.printStackTrace(System.out);
            return null;
        } catch (final Finder.MethodFoundException e) {
            // in this case the method was founded - so we should use PropertyColumnMappingByExpression.
            return new PropertyColumnMappingByExpression<T>(getEntityClass(), propertyName, columnName, preferredSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);
        }

        // the field was found (not null).
        if (AnnotationReflector.isAnnotationPresent(field, IsProperty.class)) { // we should use simple property column mapping for it - field is property.
            return new ReadonlyPropertyColumnMapping<T>(getEntityClass(), propertyName, columnName, preferredSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, true);
        } else { // we should use property column mapping by expression for it - field is not property.
            return new PropertyColumnMappingByExpression<T>(getEntityClass(), propertyName, columnName, preferredSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);
        }
    }

    @Override
    public PropertyTableModel<T> build(final List<T> instances) {
        enhanceColumnTitlesAndTooltips();
        return new PropertyTableModel<T>(instances, //
        getPropertyColumnMappings(), //
        getGroupingAlgo(), //
        new EgiColoringScheme<T>(getRowColoringScheme(), getPropertyColoringSchemes()));
    }

    /**
     * Enhances header tooltips and captions for general way defined for AE's representation in EGI. See documentation {@link EntityDescriptor} for more details.
     * 
     * Important : header tooltips and captions modifies only for builder that contains no mappings with explicitly specified "propertyTitle" and "headerToolTip".
     */
    void enhanceColumnTitlesAndTooltips() {
        final EntityDescriptor ed = new EntityDescriptor(getEntityClass(), propertyNames());
        for (final AbstractPropertyColumnMapping mapping : getPropertyColumnMappings()) {
            final Pair<String, String> tad = ed.getTitleAndDesc(isEmpty(mapping.getPropertyName()) ? "key" : mapping.getPropertyName());

            if (tad != null) {
                if (mapping.getPropertyTitle() == null) {
                    mapping.setPropertyTitle(tad.getKey());
                }
                if (mapping.getHeaderTooltip() == null) {
                    mapping.setHeaderTooltip(tad.getValue());
                }
            } else {
                logger.debug("propertyTitle == " + mapping.getPropertyTitle());
                logger.debug("headerTooltip == " + mapping.getHeaderTooltip());
                logger.info("There is no title and desc retrieved from property [" + mapping.getPropertyName() + "] in klass [" + getEntityClass()
                        + "] using unified TG algorithm.");
            }
        }
    }

    private List<String> propertyNames() {
        final List<String> names = new ArrayList<String>();
        for (final AbstractPropertyColumnMapping mapping : getPropertyColumnMappings()) {
            names.add(isEmpty(mapping.getPropertyName()) ? "key" : mapping.getPropertyName());
        }
        return names;
    }

    public void clearColumnTitlesAndTooltips(final List<AbstractPropertyColumnMapping> mappings) {
        for (final AbstractPropertyColumnMapping mapping : mappings) {
            mapping.setPropertyTitle(null);
            mapping.setHeaderTooltip(null);
        }
    }

    /**
     * Enumeration, defining policies for cells (from column) editing in {@link EntityGridInspector}
     * 
     * @author Yura
     */
    public static enum EditingPolicy {
        /**
         * Indicates that cells in column are always editable
         */
        ALWAYS_EDITABLE {
            @Override
            public boolean isEditable(final AbstractEntity entity, final String propertyName) {
                return true;
            }
        },
        /**
         * Indicates that cells are non-editable (or read-only)
         */
        NEVER_EDITABLE {
            @Override
            public boolean isEditable(final AbstractEntity entity, final String propertyName) {
                return false;
            }
        },
        /**
         * Indicates that mutability of cell is determined by value returned from {@link MetaProperty#isEditable()} method invoked on related property
         */
        BY_META_PROPERTY {
            @Override
            public boolean isEditable(final AbstractEntity entity, final String propertyName) {
                return entity.getProperty(propertyName).isEditable();
            }
        };

        public abstract boolean isEditable(AbstractEntity entity, String propertyName);
    }

    /**
     * Enumeration, defining policies for navigation over {@link EntityGridInspector}'s cells
     * 
     * @author Yura
     */
    public static enum NavigationPolicy {
        /**
         * Indicates that cells in column is always "navigable"
         */
        ALWAYS_NAVIGABLE {
            @Override
            public boolean isNavigable() {
                return true;
            }
        },
        /**
         * Indicates that cells in column are not "navigable"
         */
        NEVER_NAVIGABLE {
            @Override
            public boolean isNavigable() {
                return false;
            }
        };

        public abstract boolean isNavigable();
    }

}
