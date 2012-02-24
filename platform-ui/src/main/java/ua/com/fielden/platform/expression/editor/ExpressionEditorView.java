package ua.com.fielden.platform.expression.editor;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.expression.editor.ExpressionEditorModel.TextInsertionType;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

public class ExpressionEditorView extends BaseNotifPanel<ExpressionEditorModel> {

    private static final long serialVersionUID = 5658695527885679022L;


    public ExpressionEditorView(final ExpressionEditorModel model) {
	super("Expression editor", model);
	model.setView(this);

	final Map<String, IPropertyEditor> editors = model.getEditors();
	final JPanel componentsPanel = new JPanel(new MigLayout("insets 5", "[:50:][fill,:200:]20[:50:][grow,fill,:200:]", "[c]"));

	// row 1
	addWithParamsForEditor(componentsPanel, editors, "contextualExpression", "span");
	addWithParamsForEditor(componentsPanel, editors, "originationProperty", "span");

	// row 2
	componentsPanel.add(new JLabel("Functions"), "gapbottom 5, gaptop 5, span, split 2, aligny center");
	componentsPanel.add(new JSeparator(), "gapleft rel, gapbottom 5, gaptop 5, growx");
	componentsPanel.add(createFunctionPanel(), "span, wrap");
	componentsPanel.add(new JSeparator(), "span, gapbottom 5, gaptop 5, growx, wrap");
	//row 3
	add(componentsPanel, editors, "title");
	addAndWrap(componentsPanel, editors, "desc");

	//TODO add attribute editor

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
	final JPanel functionPanel = new JPanel(new MigLayout("fill, insets 0", "[:50:]20[:70:, fill][:70:, fill][:70:, fill][:70:, fill][:70:, fill]", "[c]"));

	//row 1
	functionPanel.add(DummyBuilder.label("Date"));
	functionPanel.add(new JButton(model.getFunctionAction("Year", "Year", "YEAR()", TextInsertionType.APPLY, true, 5)));
	functionPanel.add(new JButton(model.getFunctionAction("Month", "Month", "MONTH()", TextInsertionType.APPLY, true ,6)));
	functionPanel.add(new JButton(model.getFunctionAction("Day", "Day", "DAY()", TextInsertionType.APPLY, true, 4)), "wrap");

	//row 2
	functionPanel.add(DummyBuilder.label("Mathematical"));
	functionPanel.add(new JButton(model.getFunctionAction("+", "Addition", "+", TextInsertionType.APPEND, false, 1)));
	functionPanel.add(new JButton(model.getFunctionAction("-", "Subtraction", "-", TextInsertionType.APPEND, false, 1)));
	functionPanel.add(new JButton(model.getFunctionAction("*", "Multiplication", "*", TextInsertionType.APPEND, false, 1)));
	functionPanel.add(new JButton(model.getFunctionAction("/", "Division", "/" ,TextInsertionType.APPEND, false, 1)), "wrap");

	//row 3
	functionPanel.add(DummyBuilder.label("Aggregation"));
	functionPanel.add(new JButton(model.getFunctionAction("Count", "Count", "COUNT()", TextInsertionType.APPLY, true, 6)));
	functionPanel.add(new JButton(model.getFunctionAction("Sum", "Summation", "SUM()", TextInsertionType.APPLY, true, 4)));
	functionPanel.add(new JButton(model.getFunctionAction("Avg", "Average", "AVG()", TextInsertionType.APPLY, true, 4)));
	functionPanel.add(new JButton(model.getFunctionAction("Min", "Minimum", "MIN()", TextInsertionType.APPLY, true, 4)));
	functionPanel.add(new JButton(model.getFunctionAction("Max", "Maximum", "MAX()", TextInsertionType.APPLY, true, 4)), "wrap");
	return functionPanel;
    }

    @Override
    public String getInfo() {
	return "A facility to create and edit calculated properties.";
    }

}
