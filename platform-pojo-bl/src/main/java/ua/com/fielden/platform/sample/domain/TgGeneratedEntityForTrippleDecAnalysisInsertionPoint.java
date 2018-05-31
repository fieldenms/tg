package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents the insertion point for triple dec example.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@EntityTitle("Triple Dec Insertion Point")
@CompanionObject(ITgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
public class TgGeneratedEntityForTrippleDecAnalysisInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey>{

    public TgGeneratedEntityForTrippleDecAnalysisInsertionPoint() {
        setKey(NO_KEY);
    }
}
