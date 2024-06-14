package ua.com.fielden.platform.sample.domain.compound.ui_actions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;

/** 
 * DAO implementation for companion object {@link IOpenTgCompoundEntityMasterAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(OpenTgCompoundEntityMasterAction.class)
public class OpenTgCompoundEntityMasterActionDao extends AbstractOpenCompoundMasterDao<OpenTgCompoundEntityMasterAction> implements IOpenTgCompoundEntityMasterAction {

    @Inject
    public OpenTgCompoundEntityMasterActionDao(final IFilter filter, final IEntityAggregatesOperations coAggregates) {
        super(filter, coAggregates);
        addViewBinding(OpenTgCompoundEntityMasterAction.TGCOMPOUNDENTITYCHILDS, TgCompoundEntityChild.class, "tgCompoundEntity");
    }

    @Override
    protected IFetchProvider<OpenTgCompoundEntityMasterAction> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}