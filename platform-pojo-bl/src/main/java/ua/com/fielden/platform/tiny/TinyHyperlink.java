package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.types.Hyperlink;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// A tiny hyperlink is a short URL that provides access to an application resource.
///
/// In principle, tiny hyperlinks can represent any resource, but currently they are used for:
/// * Executing a shared action entity, including actions that open masters.
/// * Opening a master for a persisted entity.
/// * Opening a menu item within a compound master.
///
/// Only property [#hash] may be of interest to application developers.
///
/// Properties [#savingInfoHolder], [#actionIdentifier] and [#entityTypeName] are mutually exclusive with [#target].
///
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
            ACTION_IDENTIFIER = "actionIdentifier",
            TARGET = "target";

    @IsProperty(length = 64) // SHA256 is 32 bytes, hex encoding uses 2 characters per byte.
    @MapTo
    @Final
    @Title(value = "Hash", desc = "A unique hash value that identifies a hyperlink.")
    @CompositeKeyMember(1)
    private String hash;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Properties that represent a shared entity.
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @IsProperty
    @MapTo
    @Final(nullIsValueForPersisted = true)
    // @Required If `target` is null.
    private String entityTypeName;

    /// Serialised [SavingInfoHolder] that represents the shared entity state and its context.
    ///
    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @Final(nullIsValueForPersisted = true)
    // @Required If `target` is null.
    private byte[] savingInfoHolder;

    /// Corresponds to [ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig#actionIdentifier].
    ///
    @IsProperty
    @MapTo
    @Final(nullIsValueForPersisted = true)
    // @Required If `target` is null.
    @Title(value = "Action Identifier", desc = "Identifier for the action that opened the entity master where this tiny hyperlink was created.")
    private String actionIdentifier;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @IsProperty
    @MapTo
    @Final(nullIsValueForPersisted = true)
    // @Required If `entityTypeName`, `savingInfoHolder` and `actionIdentifier` are null.
    @Title(value = "Target Hyperlink", desc = "A hyperlink that this tiny hyperlink points to.")
    private Hyperlink target;

    public Hyperlink getTarget() {
        return target;
    }

    @Observable
    public TinyHyperlink setTarget(final Hyperlink target) {
        this.target = target;
        return this;
    }

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
