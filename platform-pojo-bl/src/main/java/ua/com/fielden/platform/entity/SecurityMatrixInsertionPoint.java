package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(NoKey.class)
@KeyTitle("Security Matrix Insertion Point")
@CompanionObject(ISecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    protected SecurityMatrixInsertionPoint() {
        setKey(NO_KEY);
    }
}
