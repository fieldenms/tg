package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@CompanionObject(ITgOrgUnit1.class)
@SkipKeyChangeValidation
public class TgOrgUnit1 extends AbstractPersistentEntity<String> {
    private static final long serialVersionUID = 1L;

    @Observable
    @Override
    public TgOrgUnit1 setKey(final String key) {
        super.setKey(key);
        return this;
    }
}
