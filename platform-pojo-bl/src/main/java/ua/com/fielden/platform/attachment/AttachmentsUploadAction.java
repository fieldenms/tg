package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
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
@EntityTitle(value = "Attach Files", desc ="An action for attaching files.")
public class AttachmentsUploadAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Master", desc = "Master entity, if any, that is used to associte attachments with.")
    private AbstractEntity<?> masterEntity;

    @IsProperty
    @MapTo
    @Title("Chosen Property Name")
    private String chosenPropName;

    @IsProperty(Attachment.class)
    @Title(value = "Attachments", desc = "Attachments to be associated with the master entity.")
    private final Set<Long> attachmentIds = new HashSet<>();

    @IsProperty
    @Title(value = "Attachment", desc = "The last attachment instance in a set of uploaded attachments.")
    @SkipEntityExistsValidation
    private Attachment singleAttachment;

    protected AttachmentsUploadAction() {
        setKey(NO_KEY);
    }

    @Observable
    protected AttachmentsUploadAction setAttachmentIds(final Set<Long> attachments) {
        attachmentIds.clear();
        attachmentIds.addAll(attachments);
        return this;
    }

    public Set<Long> getAttachmentIds() {
        return Collections.unmodifiableSet(attachmentIds);
    }

    @Observable
    public AttachmentsUploadAction setMasterEntity(final AbstractEntity<?> masterEntity) {
        this.masterEntity = masterEntity;
        return this;
    }

    public AbstractEntity<?> getMasterEntity() {
        return masterEntity;
    }

    @Observable
    public AttachmentsUploadAction setSingleAttachment(final Attachment singleAttachment) {
        this.singleAttachment = singleAttachment;
        return this;
    }

    public Attachment getSingleAttachment() {
        return singleAttachment;
    }

    @Observable
    public AttachmentsUploadAction setChosenPropName(final String chosenPropName) {
        this.chosenPropName = chosenPropName;
        return this;
    }

    public String getChosenPropName() {
        return chosenPropName;
    }
}
