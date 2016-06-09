package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * The functional entity, that executes invocation of corresponding entity master for centre row entity.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IMasterInvocationFunctionalEntity.class)
public class MasterInvocationFunctionalEntity extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = -6477828472851159449L;
}