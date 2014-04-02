/**
 *
 */
package ua.com.fielden.platform.application;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import ua.com.fielden.platform.branding.SplashController;

/**
 * Tests whether uncaught exceptions thrown on EDT are handled by {@link UncaughtExceptionHandler} class when using {@link AbstractUiApplication}
 * 
 * @author Yura
 */
public class UncaughtExceptionHandlingTest extends TestCase {

    /**
     * Exception purely for testing
     * 
     * @author Yura
     */
    private static class TestException extends RuntimeException {
        private static final long serialVersionUID = -8104827988559386839L;

        public TestException(final String msg) {
            super(msg);
        }
    }

    public void testHandlingOfUncaughtException() {
        final AbstractUiApplication application = new AbstractUiApplication() {

            private boolean uncaughtExceptionHandled = false;

            @Override
            protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        throw new TestException("Exception thrown on EDT");
                    }
                });
            }

            @Override
            protected void handleException(final Throwable exception) {
                System.out.println("Handled exception of type " + exception.getClass().getName() + " with message '" + exception.getMessage() + "'");
                uncaughtExceptionHandled = true;
            }

            @Override
            protected void afterUiExposure(final String[] args, final SplashController splashController) throws Exception {
                assertTrue("Exception thrown on EDT was not handled", uncaughtExceptionHandled);
            }
        };
        application.launch(new String[] {});
    }

}
