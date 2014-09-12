package ua.com.fielden.platform.entity.validation.test_entities;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.TransactionDate;
import ua.com.fielden.platform.entity.annotation.TransactionUser;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.security.user.User;

/**
 * Entity with {@link DynamicEntityKey} that has transactional key members
 *
 * @author TG Team
 */
@SuppressWarnings("serial")
@KeyType(DynamicEntityKey.class)
public class CompositionalEntityWithTransactionalKeyMembers extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "Transaction Date", desc = "Desc")
    @TransactionDate
    @CompositeKeyMember(1)
    private Date date;

    @IsProperty
    @MapTo
    @Title(value = "User", desc = "Desc")
    @TransactionUser
    @CompositeKeyMember(2)
    private User user;

    @Observable
    @EntityExists(User.class)
    public CompositionalEntityWithTransactionalKeyMembers setUser(final User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Observable
    public CompositionalEntityWithTransactionalKeyMembers setDate(final Date date) {
        this.date = date;
        return this;
    }

    public Date getDate() {
        return date;
    }

}