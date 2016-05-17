package ua.com.fielden.platform.entity;

import java.util.Date;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;

/**
 * This is a base class for all persistent entity types.
 * Its main objective is to capture basic auxiliary information about the entity's persistent life cycle such as creation time/user, last modified time/user and a transaction scope. 
 * 
 * @author TG Team
 *
 * @param <K>
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPersistentEntity<K extends Comparable> extends AbstractEntity<K>{
    private static final long serialVersionUID = 1L;
    
    /** Convenient constants to reference properties by name */
    public static final String CREATED_BY = "createdBy";
    public static final String CREATED_DATE = "createdDate";
    public static final String CREATED_TRANSACTION_GUID = "createdTransactionGuid";
    public static final String LAST_UPDATED_BY = "lastUpdatedBy";
    public static final String LAST_UPDATED_DATE = "lastUpdatedDate";
    public static final String LAST_UPDATED_TRANSACTION_GUID = "lastUpdatedTransactionGuid";
    
    @IsProperty
    @MapTo
    @Title(value = "Created by User", desc = "The user who originally created this entity instance.")
    @SkipEntityExistsValidation
    private User createdBy;

    @IsProperty
    @MapTo
    @Title(value = "Creation Date", desc = "The date/time when this entity instace was created.")
    private Date createdDate;

    @IsProperty
    @MapTo
    @Title(value = "Creation Transation ID", desc = "A unique identifier of the creation transation for this entity instance.")
    private String createdTransactionGuid;

    @IsProperty
    @MapTo
    @Title(value = "Last Updated By", desc = "The user who was the last to update this entity instance.")
    @SkipEntityExistsValidation
    private User lastUpdatedBy;

    @IsProperty
    @MapTo
    @Title(value = "Last Updated Date", desc = "The date/time when this entity instance was last updated.")
    private Date lastUpdatedDate;

    @IsProperty
    @MapTo
    @Title(value = "Last Update Transaction ID", desc = "A unique identifier of the last update transaction for this entity instance.")
    private String lastUpdatedTransactionGuid;

    @Observable
    public AbstractPersistentEntity<K> setLastUpdatedTransactionGuid(final String lastUpdatedTransactionGuid) {
        this.lastUpdatedTransactionGuid = lastUpdatedTransactionGuid;
        return this;
    }

    public String getLastUpdatedTransactionGuid() {
        return lastUpdatedTransactionGuid;
    }
    
    @Observable
    public AbstractPersistentEntity<K> setCreatedTransactionGuid(final String createdTransactionGuid) {
        this.createdTransactionGuid = createdTransactionGuid;
        return this;
    }

    public String getCreatedTransactionGuid() {
        return createdTransactionGuid;
    }
    
    @Observable
    protected AbstractPersistentEntity<K> setLastUpdatedDate(final Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }
    
    @Observable
    protected AbstractPersistentEntity<K> setLastUpdatedBy(final User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }
    
    @Observable
    protected AbstractPersistentEntity<K> setCreatedDate(final Date creationTime) {
        this.createdDate = creationTime;
        return this;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
    
    @Observable
    protected AbstractPersistentEntity<K> setCreatedBy(final User createdByUser) {
        this.createdBy = createdByUser;
        return this;
    }

    public User getCreatedBy() {
        return createdBy;
    }

}
