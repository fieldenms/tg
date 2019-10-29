package ua.com.fielden.platform.web.centre.api.impl.helpers;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;

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
}