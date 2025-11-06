package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.security.user.User;

import java.util.Date;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

@EntityTitle("Tiny Hyperlink")
@MapEntityTo
@KeyType(DynamicEntityKey.class)
@CompanionObject(TinyHyperlinkCo.class)
public class TinyHyperlink extends AbstractEntity<DynamicEntityKey> {

    public static final String ENTITY_TITLE = getEntityTitleAndDesc(TinyHyperlink.class).getKey();
    public static final String ENTITY_DESC = getEntityTitleAndDesc(TinyHyperlink.class).getValue();

    public static final String
            HASH = "hash",
            USER = "user",
            CREATED_DATE = "createdDate",
            ENTITY_TYPE_NAME = "entityTypeName",
            SAVING_INFO_HOLDER = "savingInfoHolder";

    @IsProperty(length = 64) // SHA256 is 32 bytes, hex encoding uses 2 characters per byte.
    @MapTo
    @Final
    @Title(value = "Hash", desc = "A unique hash value that identifies a hyperlink.")
    @CompositeKeyMember(1)
    private String hash;

    @IsProperty
    @MapTo
    private String entityTypeName;

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    private byte[] savingInfoHolder;

    @IsProperty
    @MapTo
    @Title(value = "User", desc = "The user who created this hyperlink.")
    private User user;

    @IsProperty
    @MapTo
    @Title(value = "Creation Date", desc = "The date/time when this entity instace was created.")
    private Date createdDate;

    public String getEntityTypeName() {
        return entityTypeName;
    }

    @Observable
    public TinyHyperlink setEntityTypeName(final String entityTypeName) {
        this.entityTypeName = entityTypeName;
        return this;
    }

    public byte[] getSavingInfoHolder() {
        return savingInfoHolder;
    }

    @Observable
    public TinyHyperlink setSavingInfoHolder(final byte[] savingInfoHolder) {
        this.savingInfoHolder = savingInfoHolder;
        return this;
    }

    @Observable
    public TinyHyperlink setUser(final User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Observable
    public TinyHyperlink setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Observable
    public TinyHyperlink setHash(final String hash) {
        this.hash = hash;
        return this;
    }

    public String getHash() {
        return hash;
    }

}
