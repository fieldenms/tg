package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * An example functional entity to activate the ON status.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgONStatusActivationFunctionalEntity.class)
public class TgONStatusActivationFunctionalEntity extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = 1L;

}