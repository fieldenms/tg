package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.ActivePropertyValidator;

/**
 * Second type from {@link TgUnion} types.
 * 
 * @author TG Team
 *
 */
@EntityTitle("TG Union Type 2")
@KeyType(String.class)
@KeyTitle("Key")
@DescTitle("Description") // desc property is present
@CompanionObject(TgUnionType2Co.class)
@MapEntityTo
public class TgUnionType2 extends ActivatableAbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title("Common Property (from TgUnionType2)")
    private TgUnionCommonType common;

    @IsProperty
    @MapTo
    @Title(value = "Uncommon Property", desc = "Property with the same name but different types and thus should not be common.")
    private Date uncommon;

    @IsProperty
    @MapTo
    @Title(value = "Active?", desc = "Designates whether an entity instance is active or not.")
    @BeforeChange(@Handler(ActivePropertyValidator.class))
    private boolean active;

    @Override
    @Observable
    public TgUnionType2 setActive(final boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Observable
    public TgUnionType2 setUncommon(final Date uncommon) {
        this.uncommon = uncommon;
        return this;
    }

    public Date getUncommon() {
        return uncommon;
    }

    @Observable
    public TgUnionType2 setCommon(final TgUnionCommonType common) {
        this.common = common;
        return this;
    }

    public TgUnionCommonType getCommon() {
        return common;
    }

}