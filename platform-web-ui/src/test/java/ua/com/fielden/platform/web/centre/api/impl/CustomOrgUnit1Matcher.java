package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.web.centre.CentreContext;

import com.google.inject.Inject;

/**
 * A stub value matcher for testing purposes
 *
 * @author TG Team
 *
 */
public class CustomOrgUnit1Matcher extends AbstractSearchEntityByKeyWithCentreContext<TgOrgUnit1> {

    @Inject
    public CustomOrgUnit1Matcher(final IEntityDao<TgOrgUnit1> dao) {
        super(dao);
    }

    @Override
    protected EntityResultQueryModel<TgOrgUnit1> completeEqlBasedOnContext(final CentreContext<TgOrgUnit1, ?> context, final String searchString, final ICompoundCondition0<TgOrgUnit1> incompleteEql) {
        // TODO Auto-generated method stub
        return null;
    }



}
