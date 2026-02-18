package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

@KeyType(NoKey.class)
@CompanionObject(TgNoopActionCo.class)
@EntityTitle("Noop")
public class TgNoopAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    protected TgNoopAction() {
        setKey(NO_KEY);
    }

}
