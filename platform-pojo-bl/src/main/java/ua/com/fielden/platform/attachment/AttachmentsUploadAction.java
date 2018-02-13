package ua.com.fielden.platform.attachment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An action that represents file uploading for the purpose of creating attachments, which can be associated with the specified master entity.
 * <p>
 * If property <code>masterEntity</code> is not empty then all attachments in collectional property <code>attachments</code> get associated with it.
 * If any of those properties is empty then no association would occur, no error would get thrown.
 * <p>
 * It is required that the companion object for master entity implements contract {@link ICanAttach}, which is only checked at runtime.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(IAttachmentsUploadAction.class)
public class AttachmentsUploadAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Master", desc = "Master entity, if any, that is used to associte attachments with.")
    private AbstractPersistentEntity<?> masterEntity;
    
    @IsProperty(Attachment.class)
    @Title(value = "Attachments", desc = "Attachments to be associated with the master entity.")
    private Set<Attachment> attachments = new HashSet<>();

    @Observable
    protected AttachmentsUploadAction setAttachments(final Set<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
        return this;
    }

    public Set<Attachment> getAttachments() {
        return Collections.unmodifiableSet(attachments);
    }

    @Observable
    public AttachmentsUploadAction setMasterEntity(final AbstractPersistentEntity<?> masterEntity) {
        this.masterEntity = masterEntity;
        return this;
    }

    public AbstractPersistentEntity<?> getMasterEntity() {
        return masterEntity;
    }

}
