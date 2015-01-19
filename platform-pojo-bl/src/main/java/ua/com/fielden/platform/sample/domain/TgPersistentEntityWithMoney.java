package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgPersistentEntityWithMoney.class)
@MapEntityTo
public class TgPersistentEntityWithMoney extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Money property;

    @Observable
    public TgPersistentEntityWithMoney setProperty(final Money property) {
        this.property = property;
        return this;
    }

    public Money getProperty() {
        return property;
    }

}