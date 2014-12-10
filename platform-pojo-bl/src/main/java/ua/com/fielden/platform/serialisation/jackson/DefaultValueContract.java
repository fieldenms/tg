package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A set of utilities to determine if the value of some property or meta-info is default. It is used internally for Jackson entity serialiser to significantly reduce the amount of
 * the information to be serialised.
 *
 * @author TG Team
 *
 */
public class DefaultValueContract {
    ///////////////////////////////////////////////// EDITABLE /////////////////////////////////////////////////
    /**
     * Returns the default value of <code>editable</code> property.
     *
     * @return
     */
    public boolean getEditableDefault() {
        return true;
    }

    /**
     * Returns the value of <code>editable</code> property.
     *
     * @param metaProperty
     * @return
     */
    public boolean getEditable(final MetaProperty<Object> metaProperty) {
        return metaProperty == null ? getEditableDefault() : metaProperty.isEditable();
    }

    /**
     * Returns <code>true</code> if the value of <code>editable</code> property is default, <code>false</code> otherwise.
     *
     * @param metaProperty
     * @return
     */
    public boolean isEditableDefault(final MetaProperty<Object> metaProperty) {
        return EntityUtils.equalsEx(getEditable(metaProperty), getEditableDefault());
    }

    ///////////////////////////////////////////////// DIRTY /////////////////////////////////////////////////
    /**
     * Returns the default value of <code>dirty</code> property.
     *
     * @return
     */
    public boolean getDirtyDefault() {
        return false;
    }

    /**
     * Returns the value of <code>dirty</code> property.
     *
     * @param metaProperty
     * @return
     */
    public boolean getDirty(final MetaProperty<Object> metaProperty) {
        return metaProperty == null ? getDirtyDefault() : metaProperty.isDirty();
    }

    /**
     * Returns <code>true</code> if the value of <code>dirty</code> property is default, <code>false</code> otherwise.
     *
     * @param metaProperty
     * @return
     */
    public boolean isDirtyDefault(final MetaProperty<Object> metaProperty) {
        return EntityUtils.equalsEx(getDirty(metaProperty), getDirtyDefault());
    }

    ///////////////////////////////////////////////// REQUIRED /////////////////////////////////////////////////
    /**
     * Returns the default value of <code>required</code> property.
     *
     * @return
     */
    public boolean getRequiredDefault() {
        return false;
    }

    /**
     * Returns the value of <code>required</code> property.
     *
     * @param metaProperty
     * @return
     */
    public boolean getRequired(final MetaProperty<Object> metaProperty) {
        return metaProperty == null ? getRequiredDefault() : metaProperty.isRequired();
    }

    /**
     * Returns <code>true</code> if the value of <code>required</code> property is default, <code>false</code> otherwise.
     *
     * @param metaProperty
     * @return
     */
    public boolean isRequiredDefault(final MetaProperty<Object> metaProperty) {
        return EntityUtils.equalsEx(getRequired(metaProperty), getRequiredDefault());
    }

    ///////////////////////////////////////////////// VISIBLE /////////////////////////////////////////////////
    /**
     * Returns the default value of <code>visible</code> property.
     *
     * @return
     */
    public boolean getVisibleDefault() {
        return true;
    }

    /**
     * Returns the value of <code>visible</code> property.
     *
     * @param metaProperty
     * @return
     */
    public boolean getVisible(final MetaProperty<Object> metaProperty) {
        return metaProperty == null ? getVisibleDefault() : metaProperty.isVisible();
    }

    /**
     * Returns <code>true</code> if the value of <code>visible</code> property is default, <code>false</code> otherwise.
     *
     * @param metaProperty
     * @return
     */
    public boolean isVisibleDefault(final MetaProperty<Object> metaProperty) {
        return EntityUtils.equalsEx(getVisible(metaProperty), getVisibleDefault());
    }

    ///////////////////////////////////////////////// VALIDATION RESULT /////////////////////////////////////////////////
    /**
     * Returns the default value of <code>ValidationResult</code> property.
     *
     * @return
     */
    public Result getValidationResultDefault() {
        return null;
    }

    /**
     * Returns the value of <code>ValidationResult</code> property.
     *
     * @param metaProperty
     * @return
     */
    public Result getValidationResult(final MetaProperty<Object> metaProperty) {
        return metaProperty == null ? getValidationResultDefault() : validationResult(metaProperty);
    }

    private Result validationResult(final MetaProperty<Object> metaProperty) {
        return !metaProperty.isValid() ? metaProperty.getFirstFailure() : metaProperty.getFirstWarning();
    }

    /**
     * Returns <code>true</code> if the value of <code>ValidationResult</code> property is default, <code>false</code> otherwise.
     *
     * @param metaProperty
     * @return
     */
    public boolean isValidationResultDefault(final MetaProperty<Object> metaProperty) {
        return EntityUtils.equalsEx(getValidationResult(metaProperty), getValidationResultDefault());
    }

    ///////////////////////////////////////////////// COMPOSITE KEY MEMBER SEPARATOR /////////////////////////////////////////////////
    /**
     * Returns <code>true</code> if the value of <code>composite key member separator</code> is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isCompositeKeySeparatorDefault(final String compositeKeySeparator) {
        return EntityUtils.equalsEx(compositeKeySeparator, " ");
    }

    ///////////////////////////////////////////////// ENTITY TITLE AND DESC /////////////////////////////////////////////////
    /**
     * Returns <code>true</code> if the value of <code>entity title</code> is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isEntityTitleDefault(final Class<?> entityType, final String entityTitle) {
        return EntityUtils.equalsEx(entityTitle, TitlesDescsGetter.getDefaultEntityTitleAndDesc(entityType).getKey());
    }

    /**
     * Returns <code>true</code> if the value of <code>entity description</code> is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isEntityDescDefault(final Class<?> entityType, final String entityDesc) {
        return EntityUtils.equalsEx(entityDesc, TitlesDescsGetter.getDefaultEntityTitleAndDesc(entityType).getValue());
    }

    ///////////////////////////////////////////////// ENTITY TYPE PROP props /////////////////////////////////////////////////
    /**
     * Returns <code>true</code> if the value of <code>secrete</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isSecreteDefault(final Boolean secrete) {
        return EntityUtils.equalsEx(secrete, false);
    }

    /**
     * Returns <code>true</code> if the value of <code>upperCase</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isUpperCaseDefault(final Boolean upperCase) {
        return EntityUtils.equalsEx(upperCase, false);
    }

    /**
     * Returns <code>true</code> if the value of <code>critOnly</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isCritOnlyDefault(final Boolean critOnly) {
        return EntityUtils.equalsEx(critOnly, false);
    }

    /**
     * Returns <code>true</code> if the value of <code>resultOnly</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isResultOnlyDefault(final Boolean resultOnly) {
        return EntityUtils.equalsEx(resultOnly, false);
    }

    /**
     * Returns <code>true</code> if the value of <code>ignore</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isIgnoreDefault(final Boolean ignore) {
        return EntityUtils.equalsEx(ignore, false);
    }

    /**
     * Returns <code>true</code> if the value of <code>length</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isLengthDefault(final Long length) {
        return EntityUtils.equalsEx(length, 0L);
    }

    /**
     * Returns <code>true</code> if the value of <code>precision</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isPrecisionDefault(final Long precision) {
        return EntityUtils.equalsEx(precision, -1L);
    }

    /**
     * Returns <code>true</code> if the value of <code>scale</code> property is default, <code>false</code> otherwise.
     *
     * @return
     */
    public boolean isScaleDefault(final Long scale) {
        return EntityUtils.equalsEx(scale, -1L);
    }
}
