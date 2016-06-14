package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
import ua.com.fielden.platform.dao.EntityWithAutoAssignablePropertiesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;

/**
 * This is a test entity, which is currently used for testing of classes {@link Money} and {@link HibernateValueMatcher}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@MapEntityTo
@CompanionObject(EntityWithAutoAssignablePropertiesDao.class)
public class EntityWithAutoAssignableProperties extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "User", desc = "User")
    private User user;

    @Observable
    public EntityWithAutoAssignableProperties setUser(final User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

}