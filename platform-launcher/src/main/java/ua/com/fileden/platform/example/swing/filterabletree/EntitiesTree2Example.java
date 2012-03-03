package ua.com.fileden.platform.example.swing.filterabletree;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTree2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreePanel;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

public class EntitiesTree2Example extends AbstractUiApplication{
    /**
     * @param args
     */
    public static void main(final String[] args) {
	new EntitiesTree2Example().launch(args);
    }

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final List<Class<?>> classes = new ArrayList<Class<?>>();
	classes.add(Vehicle.class);
	final IDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(null, new HashSet<Class<?>>(classes));

	dtm.getRepresentation().excludeImmutably(Vehicle.class, "commonProperty");
	dtm.getRepresentation().getFirstTick().checkImmutably(Vehicle.class, "desc");
	dtm.getRepresentation().getFirstTick().disableImmutably(Vehicle.class, "eqClass");
	dtm.getFirstTick().check(Vehicle.class, "replacing", true);
	dtm.getFirstTick().move(Vehicle.class, "replacing", "desc");

	dtm.getEnhancer().addCalculatedProperty(Vehicle.class, "replacing.replacing", "2 * numValue", "Calculated", "Double Num Value description", CalculatedPropertyAttribute.NO_ATTR, "numValue"); // excludeImmutably(Vehicle.class, "commonProperty");
	dtm.getEnhancer().apply();

	final EntitiesTreeModel2 model = new EntitiesTreeModel2(dtm, //
		createNewAction(), createEditAction(), createCopyAction(), createRemoveAction(),//
		"selection criteria", "result set");
	final EntitiesTree2 entitiesTree = new EntitiesTree2(model);

	dtm.getRepresentation().excludeImmutably(Vehicle.class, "replacing.commonProperty");
	dtm.getRepresentation().getSecondTick().checkImmutably(Vehicle.class, "desc");
	dtm.getRepresentation().getSecondTick().disableImmutably(Vehicle.class, "eqClass");
	dtm.getSecondTick().check(Vehicle.class, "replacing", true);
	dtm.getSecondTick().move(Vehicle.class, "replacing", "desc");
	dtm.getSecondTick().check(Vehicle.class, "replacing", false);

	dtm.getEnhancer().addCalculatedProperty(Vehicle.class, "replacedBy.replacedBy", "2 * numValue", "Calculated", "Double Num Value description", CalculatedPropertyAttribute.NO_ATTR, "numValue"); // excludeImmutably(Vehicle.class, "commonProperty");
	dtm.getEnhancer().apply();

	final EntitiesTreePanel treePanel = new EntitiesTreePanel(entitiesTree);
	treePanel.setPreferredSize(new Dimension(640, 480));
	SimpleLauncher.show("Expand all example", treePanel);
    }

    private Action createRemoveAction() {
	return new AbstractAction("Remove") {

	    private static final long serialVersionUID = 5570741599486121249L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Remove property");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    private Action createCopyAction() {
	return new AbstractAction("Copy") {

	    private static final long serialVersionUID = 5570741599486121249L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Copy property");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    private Action createEditAction() {
	return new AbstractAction("Edit") {

	    private static final long serialVersionUID = 5570741599486121249L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Edit property");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    private Action createNewAction() {
	return new AbstractAction("New") {

	    private static final long serialVersionUID = 5570741599486121249L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "New property");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }
}
