package ua.com.fielden.platform.web.centre.api.impl;

import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.IRangeValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueCritSelector;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilder<T extends AbstractEntity<?>> implements ISelectionCritKindSelector<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;


    public SelectionCriteriaBuilder(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }

    @Override
    public IMultiValueCritSelector<T> asMulti() {
        // check if the specified property can be used for muti-valued selection criteria
        final Field field = Finder.findFieldByName(builder.getEntityType(), builder.currSelectionCrit.get());
        if (field.isAnnotationPresent(CritOnly.class)) {
            final CritOnly critOnly = field.getAnnotation(CritOnly.class);
            if (critOnly.value() == CritOnly.Type.SINGLE) {
                throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used as a multi-valued criterion due to its definition as @CritOnly(SINGLE).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName()));
            }
        }

        return new SelectionCriteriaBuilderAsMulti<T>(builder, selectionCritBuilder);
    }

    @Override
    public ISingleValueCritSelector<T> asSingle() {
        // check if the specified property can be used for single-valued selection criteria
        final Field field = Finder.findFieldByName(builder.getEntityType(), builder.currSelectionCrit.get());
        if (field.isAnnotationPresent(CritOnly.class)) {
            final CritOnly critOnly = field.getAnnotation(CritOnly.class);
            if (critOnly.value() != CritOnly.Type.SINGLE) {
                throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used as a single-valued criterion due to its definition as @CritOnly(RANGE).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName()));
            }

        } else {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used as a single-valued criterion due to missing @CritOnly(SINGLE) in its definition.", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingle<T>(builder, selectionCritBuilder);
    }

    @Override
    public IRangeValueCritSelector<T> asRange() {
        // check if the specified property can be used for muti-valued selection criteria
        final Field field = Finder.findFieldByName(builder.getEntityType(), builder.currSelectionCrit.get());
        if (field.isAnnotationPresent(CritOnly.class)) {
            final CritOnly critOnly = field.getAnnotation(CritOnly.class);
            if (critOnly.value() == CritOnly.Type.SINGLE) {
                throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used as a range-valued criterion due to its definition as @CritOnly(SINGLE).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName()));
            }
        }

        return new SelectionCriteriaBuilderAsRange<T>(builder, selectionCritBuilder);
    }

}
