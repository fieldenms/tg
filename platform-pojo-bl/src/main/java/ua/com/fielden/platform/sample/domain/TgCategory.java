package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgCategory.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgCategory extends ActivatableAbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Selfy", desc = "Desc")
    private TgCategory parent; // is property is introduced to test activation/deactivation of self referenced

    @Observable
    public TgCategory setParent(final TgCategory parent) {
        this.parent = parent;
        return this;
    }

    public TgCategory getParent() {
        return parent;
    }

    @Observable
    @Override
    public TgCategory setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}