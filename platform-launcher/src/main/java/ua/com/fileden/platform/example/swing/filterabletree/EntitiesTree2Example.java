package ua.com.fileden.platform.example.swing.filterabletree;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
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
	final IDomainTreeManagerAndEnhancer manager = new CentreDomainTreeManagerAndEnhancer(null, new HashSet<Class<?>>(classes));

	manager.getRepresentation().excludeImmutably(Vehicle.class, "commonProperty");
	manager.getRepresentation().getFirstTick().checkImmutably(Vehicle.class, "desc");
	manager.getRepresentation().getFirstTick().disableImmutably(Vehicle.class, "eqClass");
	manager.getFirstTick().check(Vehicle.class, "replacing", true);
	manager.getFirstTick().move(Vehicle.class, "replacing", "desc");
	manager.getEnhancer().addCalculatedProperty(new CalculatedProperty(Vehicle.class, "replacing.replacing.calculated", CalculatedPropertyCategory.EXPRESSION, "replacing.replacing.numValue", BigDecimal.class, "2 * [replacing.replacing.numValue]", "Double Num Value", "Double Num Value description")); // excludeImmutably(Vehicle.class, "commonProperty");
	manager.getEnhancer().apply();

	final EntitiesTreeModel2 model = new EntitiesTreeModel2(manager);
	final EntitiesTree2 entitiesTree = new EntitiesTree2(model, "selection criteria", "result set");

	manager.getRepresentation().excludeImmutably(Vehicle.class, "replacing.commonProperty");
	manager.getRepresentation().getSecondTick().checkImmutably(Vehicle.class, "desc");
	manager.getRepresentation().getSecondTick().disableImmutably(Vehicle.class, "eqClass");
	manager.getSecondTick().check(Vehicle.class, "replacing", true);
	manager.getSecondTick().move(Vehicle.class, "replacing", "desc");
	manager.getEnhancer().addCalculatedProperty(new CalculatedProperty(Vehicle.class, "replacedBy.replacedBy.calculated", CalculatedPropertyCategory.EXPRESSION, "replacedBy.replacedBy.numValue", BigDecimal.class, "2 * [replacedBy.replacedBy.numValue]", "Double Num Value", "Double Num Value description")); // excludeImmutably(Vehicle.class, "commonProperty");
	manager.getEnhancer().apply();

	final EntitiesTreePanel treePanel = new EntitiesTreePanel(entitiesTree);
	treePanel.setPreferredSize(new Dimension(640, 480));
	SimpleLauncher.show("Expand all example", treePanel);
    }
}
