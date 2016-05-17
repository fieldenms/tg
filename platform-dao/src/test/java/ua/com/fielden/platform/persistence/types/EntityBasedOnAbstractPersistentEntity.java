package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
import ua.com.fielden.platform.dao.EntityBasedOnAbstractPersistentEntityDao;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * This is a test entity, which is currently used for testing of classes {@link Money} and {@link HibernateValueMatcher}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@MapEntityTo
@CompanionObject(EntityBasedOnAbstractPersistentEntityDao.class)
public class EntityBasedOnAbstractPersistentEntity extends AbstractPersistentEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @Override
    @Observable
    public EntityBasedOnAbstractPersistentEntity setKey(String key) {
        super.setKey(key);
        return this;
    }
    
}