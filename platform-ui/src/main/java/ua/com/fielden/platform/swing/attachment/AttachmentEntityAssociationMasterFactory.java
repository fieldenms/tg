package ua.com.fielden.platform.swing.attachment;

import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

import com.google.inject.Inject;

/**
 * A convenient factory for producing new instances of {@link AttachmentEntityAssociationFrame}.
 * 
 * @author TG Team
 * 
 */
public class AttachmentEntityAssociationMasterFactory {

    private final IEntityAttachmentAssociationController attachmentEntityAssociationController;
    private final IEntityMasterManager entityMasterFactory;
    private final IValueMatcherFactory valueMatcherFactory;
    private final ICriteriaGenerator criteriaGenerator;
    private final IGlobalDomainTreeManager gdtm;

    @Inject
    public AttachmentEntityAssociationMasterFactory(//
    final IEntityAttachmentAssociationController attachmentEntityAssociationController,//
            final IEntityMasterManager entityMasterFactory, //
            final IValueMatcherFactory valueMatcherFactory,//
            final ICriteriaGenerator criteriaGenerator,//
            final IGlobalDomainTreeManager gdtm) {

        this.attachmentEntityAssociationController = attachmentEntityAssociationController;
        this.entityMasterFactory = entityMasterFactory;
        this.valueMatcherFactory = valueMatcherFactory;
        this.criteriaGenerator = criteriaGenerator;
        this.gdtm = gdtm;
    }

    public final AttachmentEntityAssociationFrame createAndMakeVisible(final AbstractEntity<?> entity) {
        final AttachmentEntityAssociationFrame frame = create(entity);
        frame.refresh();
        frame.setVisible(false);
        frame.setVisible(true);
        return frame;
    }

    public final AttachmentEntityAssociationFrame create(final AbstractEntity<?> entity) {
        IMasterDomainTreeManager masterManager = gdtm.getMasterDomainTreeManager(EntityAttachmentAssociation.class);
        if (masterManager == null) {
            gdtm.initMasterDomainTreeManager(EntityAttachmentAssociation.class);
            masterManager = gdtm.getMasterDomainTreeManager(EntityAttachmentAssociation.class);
        }

        return new AttachmentEntityAssociationFrame(entity, //
        attachmentEntityAssociationController, //
        valueMatcherFactory,//
        entityMasterFactory,//
        masterManager,//
        criteriaGenerator);
    }
}
