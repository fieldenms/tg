package ua.com.fileden.platform.example.swing.treetable;

import java.awt.Dimension;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTablePanel;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.jidesoft.plaf.LookAndFeelFactory;

public class ExpandAllSpikeExample extends AbstractUiApplication {

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
        com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
        LookAndFeelFactory.installJideExtension();
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
        final FilterableTreeTableModel treeTableModel = new FilterableTreeTableModel(new ExampleTreeTableModel());
        final ExampleTreeTable treeTable = new ExampleTreeTable(treeTableModel);
        final FilterableTreeTablePanel<ExampleTreeTable> filterableTreeTablePanel = new FilterableTreeTablePanel<ExampleTreeTable>(treeTable, new WordFilter(), "filter items");
        filterableTreeTablePanel.setPreferredSize(new Dimension(640, 480));
        SimpleLauncher.show("Expand all example", filterableTreeTablePanel);
    }

    public static void main(final String[] args) {
        new ExpandAllSpikeExample().launch(args);
    }

}
