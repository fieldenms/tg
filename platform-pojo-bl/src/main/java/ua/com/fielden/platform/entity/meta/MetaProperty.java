package ua.com.fielden.platform.entity.meta;

import static java.lang.String.format;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.proxy.StrictProxyException;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 *
 * @author TG Team
 *
 */
public class MetaProperty<T> implements Comparable<MetaProperty<T>> {
    
    public static final String EDITABLE_PROPERTY_NAME = "editable";
    public static final String REQUIRED_PROPERTY_NAME = "required";
    public static final String VALIDATION_RESULTS_PROPERTY_NAME = "validationResults";
    
    protected final AbstractEntity<?> entity;
    protected final String name;
    protected Class<?> type;
    private final boolean isEntity;
    protected final boolean key;
    protected final boolean retrievable;
    private final boolean activatable;
    
    private final String[] dependentPropertyNames;
    private MetaProperty<?> parentMetaPropertyOnDependencyPath;

    
    protected String title;
    protected String desc;

    
    /**
     * Indicated whether a corresponding property is a proxy.
     */
    private final boolean proxy;

    public MetaProperty(
            final AbstractEntity<?> entity,
            final Field field,
            final Class<?> type,
            final boolean isKey,
            final boolean isProxy,
            final String[] dependentPropertyNames
            ) {
        this.entity = entity;
        this.name = field.getName();
        this.type = type;
        this.isEntity = AbstractEntity.class.isAssignableFrom(type);
        this.proxy = isProxy;
        this.key = isKey;
        
        final Pair<String, String> titleDesc = TitlesDescsGetter.getTitleAndDesc(name, entity.getType());
        setTitle(titleDesc.getKey());
        setDesc(titleDesc.getValue());
        
        this.retrievable = Reflector.isPropertyRetrievable(entity, field);
        this.dependentPropertyNames = dependentPropertyNames != null ? Arrays.copyOf(dependentPropertyNames, dependentPropertyNames.length) : new String[] {};
        
        // let's identify whether property represents an activatable entity in the current context
        final SkipEntityExistsValidation seevAnnotation = field.getAnnotation(SkipEntityExistsValidation.class);
        boolean skipActiveOnly;
        if (seevAnnotation != null) {
            skipActiveOnly = seevAnnotation.skipActiveOnly();
        } else {
            skipActiveOnly = false;
        }
        this.activatable = ActivatableAbstractEntity.class.isAssignableFrom(type) && !skipActiveOnly;
    }

    public Result validate(final T newValue, final T oldValue, final Set<Annotation> applicableValidationAnnotations, final boolean ignoreRequiredness) {
        throw new StrictProxyException(format("Invalid call [validate] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Result revalidate(final boolean ignoreRequiredness) {
        throw new StrictProxyException(format("Invalid call [revalidate] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));    
    }

    public Map<ValidationAnnotation, Map<IBeforeChangeEventHandler<T>, Result>> getValidators() {
        throw new StrictProxyException(format("Invalid call [getValidators] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    /**
     * Returns the name of the represented property.
     * 
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the type of the entity property represented by this meta-property.
     *
     * @return
     */
    public final Class<?> getType() {
        return type;
    }

    /**
     * This setter was introduces specifically to set property type in cases where it is not possible to determine during the creation of meta-property. 
     * The specific case is the
     * <code>key</code> property in {@link AbstractEntity}, where the type can be determined only when the actual value is assigned.
     *
     * This setter should be used extremely judiciously.
     *
     * @param type
     */
    public final void setType(final Class<?> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return format(format("Meta-property for property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setValidationResult(final ValidationAnnotation key, final IBeforeChangeEventHandler<T> handler, final Result validationResult) {
        throw new StrictProxyException(format("Invalid call [setValidationResult] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setRequiredValidationResult(final Result validationResult) {
        throw new StrictProxyException(format("Invalid call [setRequiredValidationResult] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setEntityExistsValidationResult(final Result validationResult) {
        throw new StrictProxyException(format("Invalid call [setEntityExistsValidationResult] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setDomainValidationResult(final Result validationResult) {
        throw new StrictProxyException(format("Invalid call [setDomainValidationResult] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Result getValidationResult(final ValidationAnnotation va) {
        throw new StrictProxyException(format("Invalid call [getValidationResult] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isValid() {
        throw new StrictProxyException(format("Invalid call [isValid] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean hasWarnings() {
        throw new StrictProxyException(format("Invalid call [hasWarnings] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Result getFirstWarning() {
        throw new StrictProxyException(format("Invalid call [getFirstWarning] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isValidWithRequiredCheck() {
        throw new StrictProxyException(format("Invalid call [isValidWithRequiredCheck] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Result getFirstFailure() {
        throw new StrictProxyException(format("Invalid call [getFirstFailure] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public int numberOfValidators() {
        throw new StrictProxyException(format("Invalid call [numberOfValidators] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void addValidationResultsChangeListener(final PropertyChangeListener listener) {
        throw new StrictProxyException(format("Invalid call [addValidationResultsChangeListener] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void removeValidationResultsChangeListener(final PropertyChangeListener listener) {
        throw new StrictProxyException(format("Invalid call [removeValidationResultsChangeListener] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void addEditableChangeListener(final PropertyChangeListener listener) {
        throw new StrictProxyException(format("Invalid call [addEditableChangeListener] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void removeEditableChangeListener(final PropertyChangeListener listener) {
        throw new StrictProxyException(format("Invalid call [removeEditableChangeListener] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void addRequiredChangeListener(final PropertyChangeListener listener) {
        throw new StrictProxyException(format("Invalid call [addRequiredChangeListener] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void removeRequiredChangeListener(final PropertyChangeListener listener) {
        throw new StrictProxyException(format("Invalid call [removeRequiredChangeListener] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public PropertyChangeSupport getChangeSupport() {
        throw new StrictProxyException(format("Invalid call [getChangeSupport] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public T getOriginalValue() {
        throw new StrictProxyException(format("Invalid call [getOriginalValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setCollectionOriginalValue(final Number size) {
        throw new StrictProxyException(format("Invalid call [setCollectionOriginalValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public MetaProperty<T> setOriginalValue(final T value) {
        throw new StrictProxyException(format("Invalid call [setOriginalValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public int getValueChangeCount() {
        throw new StrictProxyException(format("Invalid call [getValueChangeCount] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Object getPrevValue() {
        throw new StrictProxyException(format("Invalid call [getPrevValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public T getValue() {
        throw new StrictProxyException(format("Invalid call [getValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setValue(final Object value) {
        throw new StrictProxyException(format("Invalid call [setValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    /**
     * Returns <code>true</code> if the property value is a proxy.
     *
     * @return
     */
    public final boolean isProxy() {
        return proxy;
    }

    public MetaProperty<T> setPrevValue(final T prevValue) {
        throw new StrictProxyException(format("Invalid call [setPrevValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isChangedFromOriginal() {
        throw new StrictProxyException(format("Invalid call [isChangedFromOriginal] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isChangedFromPrevious() {
        throw new StrictProxyException(format("Invalid call [isChangedFromPrevious] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isEditable() {
        throw new StrictProxyException(format("Invalid call [isEditable] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setEditable(final boolean editable) {
        throw new StrictProxyException(format("Invalid call [setEditable] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public MetaProperty<T> define(final T entityPropertyValue) {
        throw new StrictProxyException(format("Invalid call [define] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public MetaProperty<T> defineForOriginalValue() {
        throw new StrictProxyException(format("Invalid call [defineForOriginalValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    /** Returns the owning entity instance. */
    public final AbstractEntity<?> getEntity() {
        return entity;
    }

    public boolean isCollectional() {
        throw new StrictProxyException(format("Invalid call [isCollectional] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Class<?> getPropertyAnnotationType() {
        throw new StrictProxyException(format("Invalid call [getPropertyAnnotationType] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    /**
     * Meta-property comparison gives preference to properties, which represent a key member. In case where both meta-properties are key members or both are non-key members the
     * property names are used for comparison.
     *
     * Comparison between key and non-key properties happens in reverse order to ensure that key properties are at the top.
     */
    @Override
    public final int compareTo(final MetaProperty<T> mp) {
        if (isKey() && mp.isKey() || !isKey() && !mp.isKey()) {
            return getName().compareTo(mp.getName());
        }
        return isKey() ? -1 : 1;
    }

    public final boolean isKey() {
        return key;
    }

    public boolean isVisible() {
        throw new StrictProxyException(format("Invalid call [isVisible] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setVisible(final boolean visible) {
        throw new StrictProxyException(format("Invalid call [setVisible] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public T getLastInvalidValue() {
        throw new StrictProxyException(format("Invalid call [getLastInvalidValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setLastInvalidValue(final T lastInvalidValue) {
        throw new StrictProxyException(format("Invalid call [setLastInvalidValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean hasValidators() {
        throw new StrictProxyException(format("Invalid call [hasValidators] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public T getLastAttemptedValue() {
        throw new StrictProxyException(format("Invalid call [getLastAttemptedValue] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public final String getTitle() {
        return title;
    }

    public final void setTitle(final String title) {
        this.title = title;
    }

    public final String getDesc() {
        return desc;
    }

    public final void setDesc(final String desc) {
        this.desc = desc;
    }

    public boolean isRequired() {
        throw new StrictProxyException(format("Invalid call [isRequired] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setRequired(final boolean required) {
        throw new StrictProxyException(format("Invalid call [setRequired] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void resetState() {
        throw new StrictProxyException(format("Invalid call [resetState] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void resetValues() {
        throw new StrictProxyException(format("Invalid call [resetValues] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isCalculated() {
        throw new StrictProxyException(format("Invalid call [isCalculated] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean containsRequiredValidator() {
        throw new StrictProxyException(format("Invalid call [containsRequiredValidator] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean containsDynamicValidator() {
        throw new StrictProxyException(format("Invalid call [containsDynamicValidator] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void putDynamicValidator() {
        throw new StrictProxyException(format("Invalid call [putDynamicValidator] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void putRequiredValidator() {
        throw new StrictProxyException(format("Invalid call [putRequiredValidator] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isDirty() {
        return false;
    }

    public MetaProperty<T> setDirty(final boolean dirty) {
        throw new StrictProxyException(format("Invalid call [setDirty] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isUpperCase() {
        throw new StrictProxyException(format("Invalid call [isUpperCase] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void restoreToOriginal() {
        throw new StrictProxyException(format("Invalid call [restoreToOriginal] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void resetValidationResult() {
        throw new StrictProxyException(format("Invalid call [resetValidationResult] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean isEnforceMutator() {
        throw new StrictProxyException(format("Invalid call [isEnforceMutator] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setEnforceMutator(final boolean enforceMutator) {
        throw new StrictProxyException(format("Invalid call [setEnforceMutator] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public boolean onDependencyPath(final MetaProperty<?> dependentMetaProperty) {
        if (parentMetaPropertyOnDependencyPath == null) {
            return false;
        }

        if (parentMetaPropertyOnDependencyPath == dependentMetaProperty) {
            return true;
        } else {
            return parentMetaPropertyOnDependencyPath.onDependencyPath(dependentMetaProperty);
        }
    }

    public void addToDependencyPath(final MetaProperty<?> metaProperty) {
        if (parentMetaPropertyOnDependencyPath != null) {
            final String msg = "Parent meta-property " + parentMetaPropertyOnDependencyPath.getName() + " for dependency path is already assigned.";
            throw new IllegalStateException(msg);
        }
        if (metaProperty == null) {
            final String msg = "Parent meta-property for dependency path cannot be null.";
            throw new IllegalStateException(msg);
        }

        boolean contains = false;
        for (final String name : metaProperty.dependentPropertyNames) {
            if (getName().equals(name)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            final String msg = "Meta-property " + metaProperty.getName() + " is not on dependency list.";
            throw new IllegalStateException(msg);
        }

        parentMetaPropertyOnDependencyPath = metaProperty;
    }

    public void removeFromDependencyPath(final MetaProperty<?> metaProperty) {
        if (metaProperty == null) {
            final String msg = "Meta-property to be removed should not be null.";
            throw new IllegalStateException(msg);
        }

        if (parentMetaPropertyOnDependencyPath != metaProperty) {
            final String msg = "Parent meta-property " + parentMetaPropertyOnDependencyPath.getName() + " is different to " + metaProperty.getName() + ".";
            throw new IllegalStateException(msg);
        }

        parentMetaPropertyOnDependencyPath = null;
    }

    /**
     * Returns the array of "dependent property names".
     *
     * @return
     */
    public final String[] getDependentPropertyNames() {
        return dependentPropertyNames;
    }

    /**
     * Returns true if there is at least one correct dependent property related to this one, false otherwise.
     *
     * @return
     */
    public final boolean hasDependentProperties() {
        return dependentPropertyNames != null && dependentPropertyNames.length > 0;
    }

    public boolean isAssigned() {
        throw new StrictProxyException(format("Invalid call [isAssigned] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setAssigned(final boolean hasAssignedValue) {
        throw new StrictProxyException(format("Invalid call [setAssigned] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Set<Annotation> getValidationAnnotations() {
        throw new StrictProxyException(format("Invalid call [getValidationAnnotations] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    /**
     * Returns property ACE handler.
     *
     * @return
     */
    public IAfterChangeEventHandler<T> getAceHandler() {
        throw new StrictProxyException(format("Invalid call [ getAceHandler] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Number getCollectionOrigSize() {
        throw new StrictProxyException(format("Invalid call [getCollectionOrigSize] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public Number getCollectionPrevSize() {
        throw new StrictProxyException(format("Invalid call [getCollectionPrevSize] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setCollectionOrigSize(final Number collectionOrigSize) {
        throw new StrictProxyException(format("Invalid call [setCollectionOrigSize] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public void setCollectionPrevSize(final Number collectionPrevSize) {
        throw new StrictProxyException(format("Invalid call [setCollectionPrevSize] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

    public final boolean isRetrievable() {
        return retrievable;
    }

    public final boolean isEntity() {
        return isEntity;
    }

    public final boolean isActivatable() {
        return activatable;
    }

    public boolean shouldAssignBeforeSave() {
        throw new StrictProxyException(format("Invalid call [shouldAssignBeforeSave] for meta-property of proxied property [%s] in entity [%s].", getName(), getEntity().getType().getName()));
    }

}
