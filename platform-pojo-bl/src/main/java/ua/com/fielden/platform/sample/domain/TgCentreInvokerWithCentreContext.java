package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgCentreInvokerWithCentreContext.class)
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgCentreInvokerWithCentreContext  extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = -7981842667209233274L;

}