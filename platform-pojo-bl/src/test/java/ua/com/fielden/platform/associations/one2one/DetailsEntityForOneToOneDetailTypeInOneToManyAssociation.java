package ua.com.fielden.platform.associations.one2one;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailsEntityForOneToOneDetailTypeInOneToManyAssociation extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "Key 1", desc = "Desc")
    @CompositeKeyMember(1)
    private DetailEntityForOneToOneAssociationWithOneToManyAssociation key1;

    @IsProperty
    @MapTo
    @Title(value = "Key 2", desc = "Desc")
    @CompositeKeyMember(2)
    private Integer key2;

    public DetailEntityForOneToOneAssociationWithOneToManyAssociation getKey1() {
        return key1;
    }

    @Observable
    public void setKey1(final DetailEntityForOneToOneAssociationWithOneToManyAssociation key1) {
        this.key1 = key1;
    }

    public Integer getKey2() {
        return key2;
    }

    @Observable
    public void setKey2(final Integer key2) {
        this.key2 = key2;
    }


}
