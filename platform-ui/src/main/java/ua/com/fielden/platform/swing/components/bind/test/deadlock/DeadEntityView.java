package ua.com.fielden.platform.swing.components.bind.test.deadlock;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.VerticalLayout;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.ReadOnlyLabel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;

import com.jidesoft.plaf.LookAndFeelFactory;

import static ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase.MIXED_CASE;

/**
 * This is the Entity view representation with 3 types of the components (used for testing and demo purposes) : CommitOnKeyTyped, CommitOnFocusLost, CommitOnTriggerCommit. Missing
 * components - have no sense - for e.g. OnKeyTypedCommit autocompleter
 * 
 * @author jhou
 * 
 */
public class DeadEntityView {
    private final DeadEntity entity;
    private final EntityFactory factory;

    public DeadEntityView(final EntityFactory factory) throws Result {
        this.factory = factory;

        entity = factory.newByKey(DeadEntity.class, "key");
    }

    private JPanel panel;

    /**
     * Creates Entity. Sets its properties. Creates trigger. Creates components from ComponentFactory. Creates 2 autocompleters with OnFocusLost commiting strategy and 2 with
     * OnSaveButtonClick strategy.
     * 
     * <strong>NOTE: </strong>components can be created from simple Entity and PropertyName, but it will commit any changes on keyTyped/focusLost. If you want to delay commits
     * (e.g. Save button) - use triggers and ComponentFactory.createTriggered*() methods
     * 
     * @param factory
     * @throws Exception
     */
    private void initComponents() {
        try {

            com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
            LookAndFeelFactory.installJideExtension();

            panel = new JPanel(new GridLayout(6, 1));
            panel.setPreferredSize(new Dimension(700, 300));
            panel.add(createPropertyPanel(new JLabel("On Key Typed:"), new JLabel("On Focus Lost:"), new JLabel("On Trigger Commit:"), new JLabel("Bounded Label:")));
            panel.add(createPropertyPanel(null, new JLabel("String fields/areas :"), null, null));

            // string fields
            // this property commits on focus lost
            final BoundedValidationLayer<JTextField> stringFieldOFL = ComponentFactory.createStringTextField(entity, DeadEntity.PROPERTY_VEHICLE, true, "string text field OFL", MIXED_CASE);
            final BoundedValidationLayer<ReadOnlyLabel> label1 = ComponentFactory.createLabel(entity, DeadEntity.PROPERTY_VEHICLE, "PROPERTY_STRING");
            panel.add(createPropertyPanel(null, stringFieldOFL, null, label1));

            // integer formatted field
            final BoundedValidationLayer<JFormattedTextField> numberFieldOFL = ComponentFactory.createIntegerTextField(entity, DeadEntity.PROPERTY_ODOMETER, true, "integer text field OFL");
            final BoundedValidationLayer<ReadOnlyLabel> label2 = ComponentFactory.createLabel(entity, DeadEntity.PROPERTY_ODOMETER, "PROPERTY_NUMBER");
            panel.add(createPropertyPanel(null, numberFieldOFL, null, label2));

            final BoundedValidationLayer<DatePickerLayer> datePickerLayerOKT = ComponentFactory.createDatePickerLayer(entity, DeadEntity.PROPERTY_ACT_ST, "enter date (toolTip)", "enter date (caption)", true, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()); // , new SimpleOnCommitSysoutMessageAction("onKeyTyped OnCommitAction for date date picker layer")
            final BoundedValidationLayer<ReadOnlyLabel> label42 = ComponentFactory.createLabel(entity, DeadEntity.PROPERTY_ACT_ST, "PROPERTY_NUMBER");
            panel.add(createPropertyPanel(null, datePickerLayerOKT, null, label42));

            final BoundedValidationLayer<DatePickerLayer> datePickerLayerOKT2 = ComponentFactory.createDatePickerLayer(entity, DeadEntity.PROPERTY_ACT_FIN, "enter date (toolTip)", "enter date (caption)", true, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()); // , new SimpleOnCommitSysoutMessageAction("onKeyTyped OnCommitAction for date date picker layer")
            final BoundedValidationLayer<ReadOnlyLabel> label43 = ComponentFactory.createLabel(entity, DeadEntity.PROPERTY_ACT_ST, "PROPERTY_NUMBER");
            panel.add(createPropertyPanel(null, datePickerLayerOKT2, null, label43));

        } catch (final Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * creates simple horizontal panel for three types of the bounded components
     * 
     * @param commitOnKeyTypedComponent
     * @param commitOnFocusLostComponent
     * @param commitOnTriggerCommitComponent
     * @return
     */
    private static final JPanel createPropertyPanel(final JComponent commitOnKeyTypedComponent, final JComponent commitOnFocusLostComponent, final JComponent commitOnTriggerCommitComponent, final JComponent boundedLabel) {

        final JPanel panel = new JPanel(new GridLayout(1, 4));
        if (commitOnKeyTypedComponent == null) {
            panel.add(new JLabel(""));
        } else {
            panel.add(commitOnKeyTypedComponent);
        }
        if (commitOnFocusLostComponent == null) {
            panel.add(new JLabel(""));
        } else {
            panel.add(commitOnFocusLostComponent);
        }
        if (commitOnTriggerCommitComponent == null) {
            panel.add(new JLabel(""));
        } else {
            panel.add(commitOnTriggerCommitComponent);
        }
        if (boundedLabel == null) {
            panel.add(new JLabel(""));
        } else {
            panel.add(boundedLabel);
        }
        return panel;
    }

    /**
     * builds the panel from existing components
     * 
     * @param factory
     * @return
     * @throws Exception
     */
    public JComponent buildPanel() {
        initComponents();
        final JPanel mainPanel = new JPanel(new VerticalLayout());
        final BlockingIndefiniteProgressLayer blockingLayer = new BlockingIndefiniteProgressLayer(mainPanel, "");
        mainPanel.setPreferredSize(new Dimension(750, 300));
        mainPanel.add(panel);
        mainPanel.validate();
        mainPanel.invalidate();
        mainPanel.repaint();
        return blockingLayer;
    }

    /**
     * sysout-styled OnCommitAction for testing purposes
     * 
     * @author jhou
     * 
     */
    private class SimpleOnCommitSysoutMessageAction implements IOnCommitAction {

        private final String message;

        public SimpleOnCommitSysoutMessageAction(final String message) {
            this.message = message;
        }

        @Override
        public void postCommitAction() {
            System.out.println("\t\t\tpost CommitAction : " + message);
        }

        @Override
        public void postNotSuccessfulCommitAction() {
            System.out.println("\t\t\tpost Not Successful CommitAction");
        }

        @Override
        public void postSuccessfulCommitAction() {
            System.out.println("\t\t\tpost Successful CommitAction");
        }
    }

}
