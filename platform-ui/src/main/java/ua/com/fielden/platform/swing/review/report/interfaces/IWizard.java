package ua.com.fielden.platform.swing.review.report.interfaces;



/**
 * A contract for <i>wizard</i> that allows one to build specific view.
 * 
 * @author TG Team
 *
 */
public interface IWizard {

    /**
     * Special wizard actions: build, cancel.
     * 
     * @author TG Team
     *
     */
    public enum WizardAction{
	PRE_BUILD, BUILD, POST_BUILD,
	PRE_CANCEL, CANCEL, POST_CANCEL;
    }

    /**
     * Adds {@link IWizardEventListener} instance to listen wizard events.
     * 
     * @param l
     */
    void addWizardEventListener(final IWizardEventListener l);

    /**
     * Removes {@link IWizardEventListener} instance.
     * 
     * @param l
     */
    void removeWizardEventListener(IWizardEventListener l);
}
