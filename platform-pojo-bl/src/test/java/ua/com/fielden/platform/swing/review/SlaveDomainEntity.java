package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
public class SlaveDomainEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected SlaveDomainEntity() {
    }

    @IsProperty
    @CompositeKeyMember(1)
    private MasterDomainEntity masterEntityProp;

    ////////// Range types //////////
    @IsProperty
    @CompositeKeyMember(2)
    private Integer integerProp = null;

    @IsProperty
    private DomainEntityWithStringKeyType anotherSimpleEntityProp;

    ////////// Entity type //////////
    @IsProperty(linkProperty = "slaveEntityLinkProp")
    private EvenSlaverDomainEntity entityProp;

    public MasterDomainEntity getMasterEntityProp() {
        return masterEntityProp;
    }
    @Observable
    public void setMasterEntityProp(final MasterDomainEntity masterEntityProp) {
        this.masterEntityProp = masterEntityProp;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    public EvenSlaverDomainEntity getEntityProp() {
        return entityProp;
    }
    @Observable
    public void setEntityProp(final EvenSlaverDomainEntity entityProp) {
        this.entityProp = entityProp;
    }

    public DomainEntityWithStringKeyType getAnotherSimpleEntityProp() {
        return anotherSimpleEntityProp;
    }
    @Observable
    public void setAnotherSimpleEntityProp(final DomainEntityWithStringKeyType anotherSimpleEntityProp) {
        this.anotherSimpleEntityProp = anotherSimpleEntityProp;
    }
}
