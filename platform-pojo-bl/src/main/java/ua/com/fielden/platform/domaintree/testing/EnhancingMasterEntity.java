package ua.com.fielden.platform.domaintree.testing;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "domain tree enhancing" testing.
 *
 * @author TG Team
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(String.class)
public class EnhancingMasterEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

    protected EnhancingMasterEntity() {
    }

    @IsProperty
    private Integer integerProp = null;
    @IsProperty
    private EnhancingMasterEntity masterEntityProp;
    @IsProperty
    private EnhancingSlaveEntity slaveEntityProp;
    @IsProperty
    private EnhancingEvenSlaverEntity evenSlaverEntityProp;
    @IsProperty(EnhancingMasterEntity.class)
    private List<EnhancingMasterEntity> masterEntityCollProp;
    @IsProperty(EnhancingSlaveEntity.class)
    private Set<EnhancingSlaveEntity> slaveEntityCollProp;
    @IsProperty(EnhancingEvenSlaverEntity.class)
    private Collection<EnhancingEvenSlaverEntity> evenSlaverEntityCollProp;

    public EnhancingMasterEntity getMasterEntityProp() {
        return masterEntityProp;
    }

    @Observable
    public void setMasterEntityProp(final EnhancingMasterEntity masterEntityProp) {
        this.masterEntityProp = masterEntityProp;
    }

    public EnhancingSlaveEntity getSlaveEntityProp() {
        return slaveEntityProp;
    }

    @Observable
    public void setSlaveEntityProp(final EnhancingSlaveEntity slaveEntityProp) {
        this.slaveEntityProp = slaveEntityProp;
    }

    public EnhancingEvenSlaverEntity getEvenSlaverEntityProp() {
        return evenSlaverEntityProp;
    }

    @Observable
    public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity evenSlaverEntityProp) {
        this.evenSlaverEntityProp = evenSlaverEntityProp;
    }

    public List<EnhancingMasterEntity> getMasterEntityCollProp() {
        return masterEntityCollProp;
    }

    @Observable
    public EnhancingMasterEntity setMasterEntityCollProp(final List<EnhancingMasterEntity> masterEntityCollProp) {
        this.masterEntityCollProp.clear();
        this.masterEntityCollProp.addAll(masterEntityCollProp);
        return this;
    }

    public Set<EnhancingSlaveEntity> getSlaveEntityCollProp() {
        return slaveEntityCollProp;
    }

    @Observable
    public EnhancingMasterEntity setSlaveEntityCollProp(final Set<EnhancingSlaveEntity> slaveEntityCollProp) {
        this.slaveEntityCollProp.clear();
        this.slaveEntityCollProp.addAll(slaveEntityCollProp);
        return this;
    }

    public Collection<EnhancingEvenSlaverEntity> getEvenSlaverEntityCollProp() {
        return evenSlaverEntityCollProp;
    }

    @Observable
    public void setEvenSlaverEntityCollProp(final Collection<EnhancingEvenSlaverEntity> evenSlaverEntityCollProp) {
        this.evenSlaverEntityCollProp = evenSlaverEntityCollProp;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

}
