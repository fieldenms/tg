package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
/**
 * Base class for all entities that represent associations between entities of type {@code A} and {@code Attachment}.
 *
 * @author TG Air Team
 *
 */
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAttachment<AA extends AbstractAttachment<AA, A>, A extends AbstractPersistentEntity<?>> extends AbstractPersistentEntity<DynamicEntityKey> {

    @IsProperty
    @Title("Attachment")
    @MapTo
    @CompositeKeyMember(2)
    @SkipEntityExistsValidation
    private Attachment attachment;

    @Observable
    public abstract AA setAttachedTo(final A value);

    public abstract A getAttachedTo();

    @Observable
    public AbstractAttachment<AA, A> setAttachment(final Attachment attachment) {
        this.attachment = attachment;
        return this;
    }

    public Attachment getAttachment() {
        return attachment;
    }

}
