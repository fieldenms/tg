package ua.com.fielden.platform.expression.editor;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;
import ua.com.fielden.platform.treemodel.rules.Function;

public class ExpressionEditorView extends BaseNotifPanel<ExpressionEditorModel> {

    private static final long serialVersionUID = 5658695527885679022L;


    public ExpressionEditorView(final ExpressionEditorModel model, final IPropertyProvider propertyProvider) {
	super("Expression editor", model);
	model.setView(this);
	propertyProvider.addPropertySelectionListener(model.getPropertySelectionListener());

	final Map<String, IPropertyEditor> editors = model.getEditors();
	final JPanel componentsPanel = new JPanel(new MigLayout("insets 5", "[:50:][grow,fill,:50:]20[:50:][grow,fill,:50:]20[:50:][grow,fill,:50:]", "[c]"));

	// row 1
	add(componentsPanel, editors, "name");//
	add(componentsPanel, editors, "title");
	addAndWrap(componentsPanel, editors, "desc");
	// row 2
	componentsPanel.add(new JLabel("Functions"), "gapbottom 5, gaptop 5, span, split 2, aligny center");
	componentsPanel.add(new JSeparator(), "gapleft rel, gapbottom 5, gaptop 5, growx");
	componentsPanel.add(createFunctionPanel(), "span, wrap");
	componentsPanel.add(new JSeparator(), "span, gapbottom 5, gaptop 5, growx, wrap");
	//row 3
	addWithParamsForEditor(componentsPanel, editors, "expression", "span");

	//////////////////action panel ///////////////////
	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 5", "push[fill,:100:][fill,:100:]", "[c]"));
	actionPanel.add(new JButton(model.getSaveAction()));
	actionPanel.add(new JButton(model.getCancelAction()));

	/////////////////// the final panel ///////////////////
	final JPanel viewPanel = new JPanel(new MigLayout("", "[grow, fill]", "[fill][]"));
	viewPanel.add(componentsPanel, "wrap");
	viewPanel.add(actionPanel);

	add(viewPanel);
    }


    private JPanel createFunctionPanel(){
	final ExpressionEditorModel model = getModel();
	final JPanel functionPanel = new JPanel(new MigLayout("fill, insets 0", "[:50:]20[left]", "[c]"));

	//row 1
	functionPanel.add(DummyBuilder.label("Collectional"));
	functionPanel.add(new JButton(model.getFunctionAction(Function.ALL)),"split 2");
	functionPanel.add(new JButton(model.getFunctionAction(Function.ANY)),"wrap");
	//row 2
	functionPanel.add(DummyBuilder.label("Date"));
	functionPanel.add(new JButton(model.getFunctionAction(Function.YEAR)),"split 3");
	functionPanel.add(new JButton(model.getFunctionAction(Function.MONTH)));
	functionPanel.add(new JButton(model.getFunctionAction(Function.DAY)),"wrap");
	//row 3
	functionPanel.add(DummyBuilder.label("Aggregation"));
	functionPanel.add(new JButton(model.getFunctionAction(Function.COUNT_DISTINCT)),"split 5");
	functionPanel.add(new JButton(model.getFunctionAction(Function.SUM)));
	functionPanel.add(new JButton(model.getFunctionAction(Function.AVG)));
	functionPanel.add(new JButton(model.getFunctionAction(Function.MIN)));
	functionPanel.add(new JButton(model.getFunctionAction(Function.MAX)));
	return functionPanel;
    }

    @Override
    public String getInfo() {
	return "A facility to create and edit calculated properties.";
    }

}
