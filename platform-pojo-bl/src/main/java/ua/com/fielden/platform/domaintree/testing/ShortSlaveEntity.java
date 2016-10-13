package ua.com.fielden.platform.domaintree.testing;

import java.util.ArrayList;
import java.util.List;

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
public class ShortSlaveEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected ShortSlaveEntity() {
    }

    @IsProperty
    @CompositeKeyMember(1)
    private MasterEntity masterEntityProp;

    ////////// Range types //////////
    @IsProperty
    @CompositeKeyMember(2)
    private SlaveEntity key2;

    ///////// Collections /////////
    @IsProperty(value = EvenSlaverEntity.class, linkProperty = "slaveEntityLinkProp")
    private List<EvenSlaverEntity> collection = new ArrayList<EvenSlaverEntity>();

    public MasterEntity getMasterEntityProp() {
        return masterEntityProp;
    }

    @Observable
    public void setMasterEntityProp(final MasterEntity masterEntityProp) {
        this.masterEntityProp = masterEntityProp;
    }

    public SlaveEntity getKey2() {
        return key2;
    }

    @Observable
    public void setKey2(final SlaveEntity key2) {
        this.key2 = key2;
    }

    public List<EvenSlaverEntity> getCollection() {
        return collection;
    }

    @Observable
    public void setCollection(final List<EvenSlaverEntity> collection) {
        this.collection.clear();
        this.collection.addAll(collection);
    }
}
