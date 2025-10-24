package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
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
            USER = "user",
            CREATED_DATE = "createdDate",
            ENTITY_TYPE_NAME = "entityTypeName",
            SAVING_INFO_HOLDER = "savingInfoHolder";

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "The user who created this hyperlink.")
    private User user;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @Title(value = "Creation Date", desc = "The date/time when this entity instace was created.")
    private Date createdDate;

    @IsProperty
    @MapTo
    private String entityTypeName;

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    private byte[] savingInfoHolder;

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

}
