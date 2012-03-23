package ua.com.fielden.platform.example.dynamiccriteria.master;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

public class SimpleCompositeEntityView extends BaseNotifPanel<SimpleCompositeEntityModel> {

    private static final long serialVersionUID = -9119205741303344386L;

    public SimpleCompositeEntityView(final SimpleCompositeEntityModel model) {
	super(model.toString(), model);

	final JPanel componentsPanel = new JPanel(new MigLayout("insets 0", "[:50:][grow,fill,:50:250]20[:50:][grow,fill,:50:]", "[c]"));
	final Map<String, IPropertyEditor> editors = model.getEditors();

	add(componentsPanel, editors, "simpleEntity");
	addWithParamsForEditor(componentsPanel, editors, "stringKey", "wrap");
	add(componentsPanel, editors, "initDate");
	addWithParamsForEditor(componentsPanel, editors, "numValue", "wrap");
	add(componentsPanel, editors, "active");

	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,:100:][fill,:100:]20[fill,:100:][fill,:100:]30:push[fill,:100:]", "[c]"));
	actionPanel.add(new JButton(model.getNewAction()));
	actionPanel.add(new JButton(model.getEditAction()));
	actionPanel.add(new JButton(model.getSaveAction()));
	actionPanel.add(new JButton(model.getCancelAction()));
	actionPanel.add(new JButton(model.getRefreshAction()));

	final JPanel mainPanel = new JPanel(new MigLayout("", "[grow, fill]", "[fill]20[]"));
	mainPanel.add(componentsPanel, "wrap");
	mainPanel.add(actionPanel);
	add(mainPanel);
	model.setView(this);
    }

    @Override
    public String getInfo() {
	return "<html>" + "<h3>Master</h3>" + "A facility for entering and editing " + TitlesDescsGetter.getEntityTitleAndDesc(SimpleCompositeEntity.class).getKey() + ".</html>";
    }

}
