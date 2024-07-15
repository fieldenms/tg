package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

@EntityTitle("Union Entity With Skip Exists Validation")
@CompanionObject(UnionEntityWithSkipExistsValidationCo.class)
public class UnionEntityWithSkipExistsValidation extends AbstractUnionEntity {

    @IsProperty(linkProperty = "bogie")
    @MapTo
    private TgWagonSlot wagonSlot;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true)
    private TgWorkshop workshop;

    @Observable
    public UnionEntityWithSkipExistsValidation setWorkshop(final TgWorkshop workshop) {
        this.workshop = workshop;
        return this;
    }

    public TgWorkshop getWorkshop() {
        return workshop;
    }

    @Observable
    public UnionEntityWithSkipExistsValidation setWagonSlot(final TgWagonSlot wagonSlot) {
        this.wagonSlot = wagonSlot;
        return this;
    }

    public TgWagonSlot getWagonSlot() {
        return wagonSlot;
    }

}