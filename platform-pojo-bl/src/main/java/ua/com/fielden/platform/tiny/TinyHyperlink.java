package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.validation.annotation.Final;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

@EntityTitle("Tiny Hyperlink")
@MapEntityTo
@KeyType(DynamicEntityKey.class)
@CompanionObject(TinyHyperlinkCo.class)
@DenyIntrospection
public class TinyHyperlink extends AbstractPersistentEntity<DynamicEntityKey> {

    public static final String ENTITY_TITLE = getEntityTitleAndDesc(TinyHyperlink.class).getKey();
    public static final String ENTITY_DESC = getEntityTitleAndDesc(TinyHyperlink.class).getValue();

    public static final String
            HASH = "hash",
            ENTITY_TYPE_NAME = "entityTypeName",
            SAVING_INFO_HOLDER = "savingInfoHolder",
            ACTION_IDENTIFIER = "actionIdentifier";

    @IsProperty(length = 64) // SHA256 is 32 bytes, hex encoding uses 2 characters per byte.
    @MapTo
    @Final
    @Title(value = "Hash", desc = "A unique hash value that identifies a hyperlink.")
    @CompositeKeyMember(1)
    private String hash;

    @IsProperty
    @Required
    @MapTo
    private String entityTypeName;

    /// Serialised [SavingInfoHolder] that represents the shared entity state and its context.
    ///
    @IsProperty(length = Integer.MAX_VALUE)
    @Required
    @MapTo
    private byte[] savingInfoHolder;

    /// Corresponds to [ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig#actionIdentifier].
    ///
    @IsProperty
    @Required
    @MapTo
    @Title(value = "Action Identifier", desc = "Identifier for the action that opened the entity master where this tiny hyperlink was created.")
    private String actionIdentifier;

    public String getActionIdentifier() {
        return actionIdentifier;
    }

    @Observable
    public TinyHyperlink setActionIdentifier(final String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
        return this;
    }

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
    public TinyHyperlink setHash(final String hash) {
        this.hash = hash;
        return this;
    }

    public String getHash() {
        return hash;
    }

}
