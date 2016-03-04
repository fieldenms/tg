package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
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
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgCollectionalSerialisationChild.class)
@MapEntityTo
public class TgCollectionalSerialisationChild extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty
    @MapTo
    @Title(value = "Key 1", desc = "Key 1")
    @CompositeKeyMember(1)
    private TgCollectionalSerialisationParent key1;
    
    @IsProperty
    @MapTo
    @Title(value = "Key 2", desc = "Key 2")
    @CompositeKeyMember(2)
    private String key2;

    @Observable
    public TgCollectionalSerialisationChild setKey2(final String key2) {
        this.key2 = key2;
        return this;
    }

    public String getKey2() {
        return key2;
    }

    @Observable
    public TgCollectionalSerialisationChild setKey1(final TgCollectionalSerialisationParent key1) {
        this.key1 = key1;
        return this;
    }

    public TgCollectionalSerialisationParent getKey1() {
        return key1;
    }
}