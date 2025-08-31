package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

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
    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgCategory.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Selfy", desc = "Desc")
    private TgCategory parent; // this property is introduced to test activation/deactivation of self referenced

    @IsProperty
    @Title(value = "Dummy non persistent", desc = "Desc")
    private Integer dummy;

    @IsProperty
    @MapTo
    @Final
    private Integer finalProp;
    
    @IsProperty
    @MapTo
    @Final(persistedOnly = false)
    @Title(value = "Immediately Final")
    private Integer immediatelyFinalProp;

    @IsProperty
    @MapTo(autoConflictResolution = false)
    @Title(value = "Pseudo Aggregate", desc = "A property that acts as if it represents some aggregation and does not support auto conflict resolution.")
    private Integer aggregate;

    @Observable
    public TgCategory setAggregate(final Integer aggregate) {
        this.aggregate = aggregate;
        return this;
    }

    public Integer getAggregate() {
        return aggregate;
    }

    @Observable
    public TgCategory setImmediatelyFinalProp(final Integer immediatelyFinalProp) {
        this.immediatelyFinalProp = immediatelyFinalProp;
        return this;
    }

    public Integer getImmediatelyFinalProp() {
        return immediatelyFinalProp;
    }

    @Observable
    public TgCategory setFinalProp(final Integer finalProp) {
        this.finalProp = finalProp;
        return this;
    }

    public Integer getFinalProp() {
        return finalProp;
    }

    @Override
    @Observable
    public TgCategory setKey(final String key) {
        super.setKey(key);
        return this;
    }

    @Override
    @Observable
    public TgCategory setDesc(final String desc) {
        super.setDesc(desc);
        return this;
    }

    @Observable
    public TgCategory setDummy(final Integer dummy) {
        this.dummy = dummy;
        return this;
    }

    public Integer getDummy() {
        return dummy;
    }

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

    @Observable
    @Override
    public TgCategory setRefCount(final Integer refCount) {
        super.setRefCount(refCount);
        return this;
    }

}
