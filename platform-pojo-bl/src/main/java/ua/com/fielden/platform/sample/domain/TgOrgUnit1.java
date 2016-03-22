package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@CompanionObject(ITgOrgUnit1.class)
public class TgOrgUnit1 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @Observable
    @Override
    public TgOrgUnit1 setKey(final String key) {
        super.setKey(key);
        return this;
    }
}
