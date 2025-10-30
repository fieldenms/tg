package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;

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

    @IsProperty
    @MapTo
    @Title(value = "Forth Cat", desc = "Desc")
    @SkipActivatableTracking
    private TgCategory forthCat;

    @IsProperty
    @MapTo
    private TgSystem system1;

    @IsProperty
    @CritOnly(SINGLE)
    @Title(value = "Crit-only Single Category", desc = "Desc")
    private TgCategory critOnlySingleCategory;

    @IsProperty
    @MapTo
    @Title(value = "Permit New", desc = "Permits new entity instances.")
    @SkipEntityExistsValidation(skipNew = true)
    private TgCategory permitNewCategory;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true, skipActiveOnly = true)
    private TgCategory permitNewAndSkipActiveOnlyCategory;

    @IsProperty(TgCategory.class)
    @MapTo
    private PropertyDescriptor<TgCategory> propDescriptor;

    @IsProperty
    @MapTo
    private TgSubSystem subSys1;

    public TgSubSystem getSubSys1() {
        return subSys1;
    }

    @Observable
    public TgSystem setSubSys1(final TgSubSystem subSys1) {
        this.subSys1 = subSys1;
        return this;
    }

    public TgSystem getSystem1() {
        return system1;
    }

    @Observable
    public TgSystem setSystem1(final TgSystem system1) {
        this.system1 = system1;
        return this;
    }

    @Observable
    public TgSystem setPermitNewCategory(final TgCategory permitNewCategory) {
        this.permitNewCategory = permitNewCategory;
        return this;
    }

    public TgCategory getPermitNewCategory() {
        return permitNewCategory;
    }

    @Observable
    public TgSystem setCritOnlySingleCategory(final TgCategory critOnlySingleCategory) {
        this.critOnlySingleCategory = critOnlySingleCategory;
        return this;
    }
    
    public TgCategory getCritOnlySingleCategory() {
        return critOnlySingleCategory;
    }
    
    @Observable
    public TgSystem setForthCat(final TgCategory forthCat) {
        this.forthCat = forthCat;
        return this;
    }

    public TgCategory getForthCat() {
        return forthCat;
    }
    
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

    public TgCategory getPermitNewAndSkipActiveOnlyCategory() {
        return permitNewAndSkipActiveOnlyCategory;
    }

    @Observable
    public TgSystem setPermitNewAndSkipActiveOnlyCategory(final TgCategory permitNewAndSkipActiveOnlyCategory) {
        this.permitNewAndSkipActiveOnlyCategory = permitNewAndSkipActiveOnlyCategory;
        return this;
    }

    @Observable
    public TgSystem setPropDescriptor(final PropertyDescriptor<TgCategory> propDescriptor) {
        this.propDescriptor = propDescriptor;
        return this;
    }

    public PropertyDescriptor<TgCategory> getPropDescriptor() {
        return propDescriptor;
    }

}
