package ua.com.fielden.platform.swing.attachment;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.review.OpenMasterClickAction;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * Provides a view for attachment/entity association management.
 *
 * @author TG Team
 *
 */
public class AttachmentEntityAssociationView extends BaseNotifPanel<AttachmentEntityAssociationModel> {
    private static final long serialVersionUID = 1L;

    @Override
    public String getInfo() {
	return "<html>" +
		"<h3>Attachment/Entity Association</h3>" +
		"A facility for adding and removing attachments associated with a specific entity." +
		"</html>";
    }


    public AttachmentEntityAssociationView(final AttachmentEntityAssociationModel model) {
	super(model.toString(), model);

	final JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill,:600:]", "[][grow,fill]"));
	mainPanel.add(createMasterPanel(getModel()), "wrap");

	final EntityGridInspector<EntityAttachmentAssociation> egi = createEgi(getModel());
	final BlockingIndefiniteProgressLayer egiBlockingLayer = createEgiPanel(egi);
	mainPanel.add(egiBlockingLayer);

	final BlockingIndefiniteProgressLayer mainBlockingLayer  = new BlockingIndefiniteProgressLayer(mainPanel, "");
	add(mainBlockingLayer);

	OpenMasterClickAction.enhanceWithBlockingLayer(egi.getActualModel().getPropertyColumnMappings(),//
		EntityAttachmentAssociation.class, //
		getModel().getEntityMasterFactory(), //
		egiBlockingLayer.provider());

	model.setBlockingLayer(mainBlockingLayer);
	model.setView(this);
    }


    private JPanel createMasterPanel(final AttachmentEntityAssociationModel model) {
	/**********************************************************
	 * Create a panel for edit controls: 6 rows and 6 columns *
	 **********************************************************/
	final JPanel componentsPanel = new JPanel(new MigLayout("insets 0", "[:50:][grow,fill,:150:]20[fill,120:120:120]", "[c]"));
	final Map<String, IPropertyEditor> editors = model.getEditors();
	// row 1
	add(componentsPanel, editors, "attachment");
	componentsPanel.add(new JButton(model.getDownloadAttachment()), "wrap");

	/*******************************************************
	 * Create a panel for action buttons: 1 row, 3 columns *
	 *******************************************************/
	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,:100:][fill,:100:]20[fill,:100:][fill,:100:]20:push[fill,120:120:120]", "[c]"));
	actionPanel.add(new JButton(model.getNewAction()));
	actionPanel.add(new JButton(model.getDeleteAction()));
	actionPanel.add(new JButton(model.getSaveAction()));
	actionPanel.add(new JButton(model.getCancelAction()));
	actionPanel.add(new JButton(model.getRefreshAction()));

	/*************************************
	 * Add all panels to the master one. *
	 *************************************/
	final JPanel masterPanel = new JPanel(new MigLayout("", "[grow, fill]", "[]20[]"));
	masterPanel.add(componentsPanel, "wrap");
	masterPanel.add(actionPanel);

	return masterPanel;
    }

    private BlockingIndefiniteProgressLayer createEgiPanel(final EntityGridInspector<EntityAttachmentAssociation> egi) {
	final JPanel egiPanel = new JPanel(new MigLayout("", "[grow,fill]", "[grow,fill][]"));
	egi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	egi.setColumnResizable(true);
	egi.setColumnAutoResizable(true);
	egi.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

	egiPanel.add(new JScrollPane(egi), "wrap");
	return new BlockingIndefiniteProgressLayer(egiPanel, "");
    }

    private EntityGridInspector<EntityAttachmentAssociation> createEgi(final AttachmentEntityAssociationModel model) {
	final EntityGridInspector<EntityAttachmentAssociation> egi = new EntityGridInspector<EntityAttachmentAssociation>(model.getTableModel(), false);
	egi.setRowHeight(26);
	egi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	egi.setHierarchicalColumn(-1);
	egi.setSingleExpansion(true);
	egi.getSelectionModel().addListSelectionListener(model.getOnRowSelect());
	if (model.getTableModel().instances().size() > 0) {
	    model.getTableModel().selectRow(0);
	}
	return egi;
    }

}
