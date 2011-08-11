package ua.com.fileden.platform.example.swing.filterabletree;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.swing.filteredtree.FilterableTree;
import ua.com.fielden.platform.swing.filteredtree.FilterableTreePanel;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.review.DefaultDynamicCriteriaPropertyFilter;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;

import com.jidesoft.plaf.LookAndFeelFactory;

public class FilterableTreeExample extends AbstractUiApplication{

    /**
     * @param args
     */
    public static void main(final String[] args) {
	new FilterableTreeExample().launch(args);
    }

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable  {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final EntitiesTreeModel treeModel=createTreeModel(Vehicle.class);
	final FilterableTree treeTable=new FilterableTree(treeModel,new WordFilter(),false,false);
	final FilterableTreePanel filterableTreePanel=new FilterableTreePanel(treeTable, "filter items");
	filterableTreePanel.setPreferredSize(new Dimension(640,480));
	SimpleLauncher.show("Expand all example", filterableTreePanel);
    }

    private EntitiesTreeModel createTreeModel(final Class<? extends AbstractEntity> clazz){
	final List<Class<? extends AbstractEntity>> classes = new ArrayList<Class<? extends AbstractEntity>>();
	classes.add(clazz);
	final EntitiesTreeModel model= new EntitiesTreeModel(classes, new DefaultDynamicCriteriaPropertyFilter());
	return model;
    }

}
