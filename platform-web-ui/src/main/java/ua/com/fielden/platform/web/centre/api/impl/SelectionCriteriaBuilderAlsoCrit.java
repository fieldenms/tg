package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.MatcherOptions;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityOrUnionType;
import static ua.com.fielden.platform.web.centre.api.EntityCentreConfig.MatcherOptions.HIDE_ACTIVE_ONLY_ACTION;

/// A package private helper class to decompose the task of implementing the Entity Centre DSL.
/// It has direct access to protected fields in [EntityCentreBuilder].
///
class SelectionCriteriaBuilderAlsoCrit<T extends AbstractEntity<?>> implements IAlsoCrit<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;

    public SelectionCriteriaBuilderAlsoCrit(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }

    @Override
    public ISelectionCriteriaBuilder<T> also() {
        this.builder.currSelectionCrit = Optional.empty();
        return selectionCritBuilder;
    }

    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (builder.selectionCriteria.isEmpty()) {
            throw new EntityCentreConfigurationException("Looks like out of sequence call as there are selection criteria to layout.");
        }

        if (device == null || orientation == null) {
            throw new EntityCentreConfigurationException("Selection criterial layout requries device and orientation (optional) to be specified.");
        }

        return new SelectionCriteriaLayoutBuilder<>(builder).setLayoutFor(device, orientation, flexString);
    }

    /// Builds a custom matcher configuration for criteria property with different parameters.
    ///
    protected void buildWithMatcher(
            final Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>> matcherType,
            final Optional<CentreContextConfig> contextOpt,
            final List<MatcherOptions> options)
    {
        if (builder.currSelectionCrit.isEmpty()) {
            throw new EntityCentreConfigurationException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (matcherType == null) {
            throw new EntityCentreConfigurationException("Matcher must be provided.");
        }

        final var currSelectionCrit = builder.currSelectionCrit.get();
        if (options.contains(HIDE_ACTIVE_ONLY_ACTION) && !isActivatableEntityOrUnionType(determinePropertyType(builder.getEntityType(), currSelectionCrit))) {
            throw new EntityCentreConfigurationException(format(
                    "'Active only' action can not be hidden for non-activatable property [%s.%s].",
                    builder.getEntityType().getSimpleName(), currSelectionCrit));
        }

        this.builder.valueMatchersForSelectionCriteria.put(currSelectionCrit, t3(matcherType, contextOpt, options));
    }

}
