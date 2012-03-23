package ua.com.fielden.platform.swing.attachment;

import java.awt.event.ActionEvent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;

/**
 * A convenient action for invoking {@link AttachmentEntityAssociationFrame} for a specific entity.
 *
 * @author TG Team
 *
 */
public abstract class InvokeEntityAttachmentAssociationMasterAction extends BlockingLayerCommand<Void> {

    private static final long serialVersionUID = 5494880216646898554L;

    private final AttachmentEntityAssociationMasterFactory factory;

    public InvokeEntityAttachmentAssociationMasterAction(final String name, final AttachmentEntityAssociationMasterFactory factory, final IBlockingLayerProvider provider) {
	super(name, provider);
	this.factory = factory;
    }

    /** Should be implemented in order to provide context depended entity determination. */
    protected abstract AbstractEntity<?> getEntity();

    @Override
    protected boolean preAction() {
	final boolean flag = super.preAction();
	if (!flag) {
	    return flag;
	}
	if (getEntity() != null) {
	    setMessage("Opening attachments...");
	    return true;
	}
	return false;
    }

    @Override
    protected Void action(final ActionEvent event) throws Exception {

	return null;
    }

    @Override
    protected void postAction(final Void value) {
	factory.createAndMakeVisible(getEntity());
	super.postAction(value);
    }

}
