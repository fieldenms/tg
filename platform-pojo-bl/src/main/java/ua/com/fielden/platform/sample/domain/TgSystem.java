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
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
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
@CompanionObject(ITgSystem.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgSystem extends ActivatableAbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title("Category")
    private TgCategory category;

    @IsProperty
    @MapTo
    @Title(value = "First Cat", desc = "Desc")
    private TgCategory firstCategory;

    @IsProperty
    @MapTo
    @Title(value = "Second Cat", desc = "Desc")
    @SkipEntityExistsValidation
    private TgCategory secondCategory;

    @IsProperty
    @MapTo
    @Title(value = "Third Cat", desc = "Desc")
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private TgCategory thirdCategory;

    @Observable
    public TgSystem setThirdCategory(final TgCategory thirdCategory) {
        this.thirdCategory = thirdCategory;
        return this;
    }

    public TgCategory getThirdCategory() {
        return thirdCategory;
    }

    @Observable
    public TgSystem setSecondCategory(final TgCategory secondCategory) {
        this.secondCategory = secondCategory;
        return this;
    }

    public TgCategory getSecondCategory() {
        return secondCategory;
    }

    @Observable
    public TgSystem setFirstCategory(final TgCategory firstCategory) {
        this.firstCategory = firstCategory;
        return this;
    }

    public TgCategory getFirstCategory() {
        return firstCategory;
    }

    @Observable
    public TgSystem setCategory(final TgCategory category) {
        this.category = category;
        return this;
    }

    public TgCategory getCategory() {
        return category;
    }

    @Override
    @Observable
    public TgSystem setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}