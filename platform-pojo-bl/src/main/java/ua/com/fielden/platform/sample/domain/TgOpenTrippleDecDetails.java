package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Action that opens triple dec details.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@EntityTitle("Triple Dec Details")
@CompanionObject(ITgOpenTrippleDecDetails.class)
public class TgOpenTrippleDecDetails extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public TgOpenTrippleDecDetails() {
        setKey(NO_KEY);
    }
}
