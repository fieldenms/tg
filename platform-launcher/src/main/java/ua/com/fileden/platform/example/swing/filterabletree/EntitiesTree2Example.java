package ua.com.fileden.platform.example.swing.filterabletree;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.swing.treewitheditors.development.EntitiesTreeModel2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTree2;
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

	final EntitiesTreeModel2 model = new EntitiesTreeModel2(dtm, "selection criteria", "result set");
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
}
