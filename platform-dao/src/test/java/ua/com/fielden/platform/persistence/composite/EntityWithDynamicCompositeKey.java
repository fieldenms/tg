package ua.com.fielden.platform.persistence.composite;

import ua.com.fielden.platform.dao.EntityWithDynamicCompositeKeyDao;
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
 * Test entity class, which utilises {@link DynamicEntityKey} for composite key implementation. The composite key consists of two parts -- one is simple string, another is also an
 * entity mapped as many-to-one association.
 * 
 * @author 01es
 * 
 */
@KeyType(DynamicEntityKey.class)
@DescTitle("Description")
@MapEntityTo("ENTITY_WITH_COMPOSITE_KEY")
@CompanionObject(EntityWithDynamicCompositeKeyDao.class)
public class EntityWithDynamicCompositeKey extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("KEY_PART_ONE")
    private String keyPartOne;
    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("MONEY_CLASS_ID")
    private EntityWithMoney keyPartTwo;

    protected EntityWithDynamicCompositeKey() {
        setKey(new DynamicEntityKey(this));
    }

    public EntityWithDynamicCompositeKey(final String keyPartOne, final EntityWithMoney keyPartTwo) {
        this();
        setKeyPartOne(keyPartOne);
        setKeyPartTwo(keyPartTwo);
    }

    public String getKeyPartOne() {
        return keyPartOne;
    }

    @Observable
    public EntityWithDynamicCompositeKey setKeyPartOne(final String keyPartOne) {
        this.keyPartOne = keyPartOne;
        return this;
    }

    public EntityWithMoney getKeyPartTwo() {
        return keyPartTwo;
    }

    @Observable
    public EntityWithDynamicCompositeKey setKeyPartTwo(final EntityWithMoney keyPartTwo) {
        this.keyPartTwo = keyPartTwo;
        return this;
    }
}