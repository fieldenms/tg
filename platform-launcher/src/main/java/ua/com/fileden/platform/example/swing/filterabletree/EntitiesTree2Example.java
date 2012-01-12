package ua.com.fileden.platform.example.swing.filterabletree;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
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
	final CentreDomainTreeManagerAndEnhancer manager = new CentreDomainTreeManagerAndEnhancer(null, new HashSet<Class<?>>(classes));

	manager.getRepresentation().getFirstTick().checkImmutably(Vehicle.class, "desc");
	manager.getFirstTick().check(Vehicle.class, "replacing.desc", true);

	// final EntitiesTreeModel2 treeModel = createTreeModel(/*MasterEntityForIncludedPropertiesLogic*/Vehicle.class);
	final EntitiesTreeModel2 model = new EntitiesTreeModel2(manager);

//	final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
//	final TreePath path = new TreePath(model.getPathToRoot(rootNode.getChildAt(0)));
//	model.addCheckingPath(path, 0);
//	model.getCheckingModel(1).addCheckingPath(path);
//	System.out.println("model.getCheckingModel(1).isPathChecked(path) == " + model.getCheckingModel(1).isPathChecked(path));
	// model.getCheckingModel(0).setPathEnabled(path, false);

	final EntitiesTree2 entitiesTree = new EntitiesTree2(model, "selection criteria", "result set");
//	System.out.println("entitiesTree.getEntitiesModel().getCheckingModel(1).isPathChecked(path) == " + entitiesTree.getEntitiesModel().getCheckingModel(1).isPathChecked(path));

	manager.getSecondTick().check(Vehicle.class, "replacing", true);
	manager.getSecondTick().check(Vehicle.class, "replacing.desc", true);

	final EntitiesTreePanel treePanel = new EntitiesTreePanel(entitiesTree);
	treePanel.setPreferredSize(new Dimension(640, 480));
	SimpleLauncher.show("Expand all example", treePanel);
    }
}
