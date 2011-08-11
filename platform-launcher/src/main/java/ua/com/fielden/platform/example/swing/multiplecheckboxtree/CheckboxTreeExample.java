package ua.com.fielden.platform.example.swing.multiplecheckboxtree;

import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTree;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.jidesoft.plaf.LookAndFeelFactory;

public class CheckboxTreeExample extends AbstractUiApplication {

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final MultipleCheckboxTree tree = new MultipleCheckboxTree(2);
	SimpleLauncher.show("Multiple check box tree example", new JScrollPane(tree));
    }

    public static void main(final String[] args) {
	new CheckboxTreeExample().launch(args);
    }
}
