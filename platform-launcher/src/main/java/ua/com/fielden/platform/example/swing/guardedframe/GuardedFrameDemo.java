package ua.com.fielden.platform.example.swing.guardedframe;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.view.BaseFrame;

/**
 * This is a brief demo of the {@link BaseFrame} -- the frame created by clicking the button on the main frame is guarded, and cannot be closed because one of its components is
 * ICloseGuared with canClose returning false.
 * 
 * @author 01es
 * 
 */
public class GuardedFrameDemo extends AbstractUiApplication {

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
        final GuardedFrameContainer mainFrame = new GuardedFrameContainer();
        RefineryUtilities.centerFrameOnScreen(mainFrame.getFrame());
        mainFrame.setVisible(true);
    }

    public static void main(final String[] args) {
        new GuardedFrameDemo().launch(args);
    }

}
