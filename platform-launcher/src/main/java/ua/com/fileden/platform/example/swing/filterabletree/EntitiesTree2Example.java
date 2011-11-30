package ua.com.fileden.platform.example.swing.filterabletree;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.domaintree.EntitiesTreeModel2;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTree;
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
	final EntitiesTreeModel2 treeModel = createTreeModel(/*MasterEntityForIncludedPropertiesLogic*/Vehicle.class); // Vehicle.class);
	final EntitiesTree treeTable = new EntitiesTree(treeModel, "selection criteria", "result set");
	final EntitiesTreePanel treePanel = new EntitiesTreePanel(treeTable);
	treePanel.setPreferredSize(new Dimension(640, 480));
	SimpleLauncher.show("Expand all example", treePanel);
    }

    private EntitiesTreeModel2 createTreeModel(final Class<? extends AbstractEntity> clazz) {
	final List<Class<?>> classes = new ArrayList<Class<?>>();
	classes.add(clazz);
	final EntitiesTreeModel2 model = new EntitiesTreeModel2(new CentreDomainTreeManagerAndEnhancer(null, new HashSet<Class<?>>(classes)));
	return model;
    }
}
