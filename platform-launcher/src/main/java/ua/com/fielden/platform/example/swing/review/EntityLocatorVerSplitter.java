package ua.com.fielden.platform.example.swing.review;

import java.awt.Color;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.example.entities.IWheelsetDao;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReview;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.splitter.SplitEnum;
import ua.com.fielden.platform.swing.splitter.SplitManager;

public class EntityLocatorVerSplitter extends EntityReview<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> {

    private static final long serialVersionUID = 2945111517158537350L;

    public EntityLocatorVerSplitter(final EntityReviewModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> model) {
	this(model, false);
    }

    public EntityLocatorVerSplitter(final EntityReviewModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> model, final boolean showRecords) {
	super(model, showRecords);
    }

    @Override
    protected JPanel createCriteriaPanel(final EntityReviewModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> model) {
	final Map<String, IPropertyEditor> editors = model.getCriteriaInspectorModel().getEditors();
	if (editors.isEmpty()) {
	    return null;
	}
	final JPanel critPanel = new JPanel(new MigLayout("fillx", "[:100:][:200:]", "[c]"));
	for (final IPropertyEditor editor : editors.values()) {
	    final String labelAlignment = editor.getEditor() instanceof JScrollPane ? "align left top" : "";
	    final String editorAlignment = editor.getEditor() instanceof JScrollPane ? "grow, wrap" : "growx, wrap";

	    if (editor.getLabel() != null) {
		critPanel.add(editor.getLabel(), labelAlignment);
	    }
	    critPanel.add(editor.getEditor(), editorAlignment);
	}
	return critPanel;
    }

    @Override
    protected void layoutComponents() {
	setBorder(BorderFactory.createLineBorder(Color.gray));
	if (getCriteriaPanel() == null) {
	    super.layoutComponents();
	} else {
	    setLayout(new MigLayout("fill, insets 0 0 0 0"));
	    final SplitManager splitManager = new SplitManager(this);
	    splitManager.split(SplitEnum.EAST, "EAST");
	    splitManager.setComponent("EAST", getCriteriaPanel(), JSplitPane.LEFT);
	    final JPanel gridViewPanel = new JPanel(new MigLayout("fill, insets 0 0 0 0", "[]", "[][]"));
	    gridViewPanel.add(getButtonPanel(), "growx, wrap");
	    gridViewPanel.add(getProgressLayer(), "grow");
	    splitManager.setComponent("EAST", gridViewPanel, JSplitPane.RIGHT);
	    splitManager.setOneTouchExpandable(true);
	    splitManager.flush("grow");
	}
    }

    @Override
    public String getInfo() {
	return "No info";
    }
}
