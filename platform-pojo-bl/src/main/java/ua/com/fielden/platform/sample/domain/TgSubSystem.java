package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgSubSystem.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgSubSystem extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title("Active?")
    private boolean active;

    @IsProperty
    @MapTo
    @Title("Category")
    private TgCategory category;

    @Observable
    @EntityExists(TgCategory.class)
    public TgSubSystem setCategory(final TgCategory category) {
        this.category = category;
        return this;
    }

    public TgCategory getCategory() {
        return category;
    }




    @Observable
    public TgSubSystem setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }




}