package ua.com.fielden.platform.swing.attachment;

import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.LocatorMasterRetriever;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.IMasterConfigurationController;

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
    private final IDaoFactory daoFactory;
    private final LocatorMasterRetriever locatorRetriever;
    private final ILocatorConfigurationController locatorController;

    @Inject
    public AttachmentEntityAssociationMasterFactory(final ILocatorConfigurationController locatorController, final IEntityAttachmentAssociationController attachmentEntityAssociationController, final IEntityMasterManager entityMasterFactory, //
    final IValueMatcherFactory valueMatcherFactory,//
    final IDaoFactory daoFactory,//
    final IMasterConfigurationController masterController) {

	this.attachmentEntityAssociationController = attachmentEntityAssociationController;
	this.entityMasterFactory = entityMasterFactory;
	this.valueMatcherFactory = valueMatcherFactory;
	this.daoFactory = daoFactory;
	this.locatorController = locatorController;
	locatorRetriever = new LocatorMasterRetriever(masterController, EntityAttachmentAssociation.class);
    }

    public final AttachmentEntityAssociationFrame createAndMakeVisible(final AbstractEntity<?> entity) {
	final AttachmentEntityAssociationFrame frame = create(entity);
	frame.refresh();
	frame.setVisible(false);
	frame.setVisible(true);
	return frame;
    }

    public final AttachmentEntityAssociationFrame create(final AbstractEntity<?> entity) {
	return new AttachmentEntityAssociationFrame(entity, //
	attachmentEntityAssociationController, //
	valueMatcherFactory,//
	entityMasterFactory,//
	daoFactory, //
	locatorController, locatorRetriever);
    }
}
