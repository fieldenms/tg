package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAsloCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup0;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class TopLevelActionsBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements
        ICentreTopLevelActions<T>, ICentreTopLevelActionsInGroup<T>, ICentreTopLevelActionsInGroup0<T>, IAsloCentreTopLevelActions<T> {

    private final EntityCentreBuilder<T> builder;


    public TopLevelActionsBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
        this.builder = builder;
    }

    @Override
    public IAsloCentreTopLevelActions<T> addTopAction(final EntityActionConfig actionConfig) {
        builder.topLevelActions.add(new Pair<>(actionConfig, builder.currGroup));
        return this;
    }

    @Override
    public ICentreTopLevelActionsInGroup<T> beginTopActionsGroup(final String desc) {
        builder.currGroup = Optional.of(desc);
        return this;
    }

    @Override
    public ICentreTopLevelActionsInGroup0<T> addGroupAction(final EntityActionConfig actionConfig) {
        builder.topLevelActions.add(new Pair<>(actionConfig, builder.currGroup));
        return this;
    }

    @Override
    public IAsloCentreTopLevelActions<T> endTopActionsGroup() {
        builder.currGroup = Optional.empty();
        return this;
    }

    @Override
    public ICentreTopLevelActions<T> also() {
        return this;
    }

    @Override
    public ISelectionCritKindSelector<T> addCrit(final String propName) {
        if (!EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new IllegalArgumentException(String.format("Provided value '%s' is not a valid property expression for entity '%s'", propName, builder.getEntityType().getSimpleName()));
        }

        builder.currSelectionCrit = Optional.of(propName);
        builder.selectionCriteria.add(propName);
        return new SelectionCriteriaBuilder<T>(builder, this);
    }

}
