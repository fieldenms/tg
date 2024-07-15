package ua.com.fielden.platform.persistence.composite;

import ua.com.fielden.platform.dao.EntityWithSingleMemberDynamicCompositeKeyDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;

/**
 * Test entity class, which utilises {@link DynamicEntityKey} for composite key implementation. The composite key consists of a single member.
 * 
 * @author TG Team
 * 
 */
@KeyType(DynamicEntityKey.class)
@DescTitle("Description")
@MapEntityTo
@CompanionObject(EntityWithSingleMemberDynamicCompositeKeyDao.class)
public class EntityWithSingleMemberDynamicCompositeKey extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo
    private EntityWithMoney keyMemember;

    public EntityWithMoney getKeyMemember() {
        return keyMemember;
    }

    @Observable
    public EntityWithSingleMemberDynamicCompositeKey setKeyMemember(final EntityWithMoney keyMember) {
        this.keyMemember = keyMember;
        return this;
    }
}