package ua.com.fielden.platform.swing.attachment;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Action;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.development.IValueMatcherFactory;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.actions.custom.AbstractDownloadAttachmentAction;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.model.FrameTitleUpdater;
import ua.com.fielden.platform.swing.model.UmDetailsWithCrudAndUpdaterMany;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A model for attachment/entity association UI.
 * 
 * @author TG Team
 * 
 */
public class AttachmentEntityAssociationModel extends UmDetailsWithCrudAndUpdaterMany<AbstractEntity<?>, EntityAttachmentAssociation, IEntityAttachmentAssociationController> {

    private final IEntityMasterManager entityMasterFactory;
    private final Command<File> downloadAttachment;

    public AttachmentEntityAssociationModel(//
	    final AbstractEntity<?> master, //
	    final IEntityAttachmentAssociationController controller,//
	    final IValueMatcherFactory valueMatcherFactory, //
	    final IEntityMasterManager entityMasterFactory,//
	    //final IDaoFactory daoFactory,//
	    final FrameTitleUpdater titleUpdater,//
	    final IMasterDomainTreeManager masterManager, final ICriteriaGenerator criteriaGenerator) {
	super(master, controller, MasterPropertyBinder.<EntityAttachmentAssociation>createPropertyBinderWithLocatorSupport(//
		valueMatcherFactory, //
		//entityMasterFactory,//
		//daoFactory, locatorController,//
		//locatorRetriever,
		masterManager,//
		criteriaGenerator,//
		"entityId"), null, produceEgiModel(), titleUpdater, false);

	this.entityMasterFactory = entityMasterFactory;
	this.downloadAttachment = createDownloadAttachmentCommand();

	setState(UmState.VIEW);
    }

    private Command<File> createDownloadAttachmentCommand() {
	final Command<File> command = new AbstractDownloadAttachmentAction(getController().getAttachmentController(), blockingLayerProvider) {
	    @Override
	    protected Attachment getAttachment() {
		return getManagedEntity().getAttachment();
	    }

	    @Override
	    protected Component getOwningComponent() {
		return getView();
	    }
	};

	command.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/download-16.png"));
	command.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/download-16.png"));
	command.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_4);
	command.putValue(Action.SHORT_DESCRIPTION, "Download attached file");
	command.setEnabled(getManagedEntity().isPersisted());

	return command;
    }

    private static PropertyTableModel<EntityAttachmentAssociation> produceEgiModel() {
	return new PropertyTableModelBuilder<EntityAttachmentAssociation>(EntityAttachmentAssociation.class).//
		addReadonly("attachment", 100).//
		addReadonly("attachment.desc", 300).//
		build(new ArrayList<EntityAttachmentAssociation>());
    }

    @Override
    protected EntityAttachmentAssociation newEntity(final EntityFactory factory) {
	final EntityAttachmentAssociation entity = factory.newEntity(EntityAttachmentAssociation.class);
	entity.setEntityId(getEntity().getId());
	return entity;
    }

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
	super.notifyActionStageChange(actionState);
	// add focusing different editors depending on the action stage
	if (actionState == ActionStage.NEW_POST_ACTION || actionState == ActionStage.EDIT_POST_ACTION) {
	    getEditors().get("attachment").getEditor().requestFocusInWindow();
	} else if (actionState == ActionStage.SAVE_POST_ACTION_SUCCESSFUL) {
	    getRefreshAction().actionPerformed(null);
	}

	// handle attachment download action enability
	if (actionState == ActionStage.EDIT_POST_ACTION || actionState == ActionStage.NEW_POST_ACTION) {
	    downloadAttachment.setEnabled(false);
	} else if (actionState == ActionStage.SAVE_POST_ACTION_SUCCESSFUL || actionState == ActionStage.CANCEL_POST_ACTION || actionState == ActionStage.REFRESH_POST_ACTION) {
	    downloadAttachment.setEnabled(getManagedEntity().isPersisted());
	}
    }

    @Override
    protected String defaultTitle() {
	return "Attachments (" + getEntity() + ")";
    }

    @Override
    public String toString() {
	return "Attachments";
    }

    public IEntityMasterManager getEntityMasterFactory() {
	return entityMasterFactory;
    }

    public Command<File> getDownloadAttachment() {
	return downloadAttachment;
    }

}
