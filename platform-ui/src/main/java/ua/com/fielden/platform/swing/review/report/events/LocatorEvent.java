package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityLocator;

/**
 * This represents the locator event that is generated when select or close action were performed.
 * 
 * @author TG Team
 *
 */
public class LocatorEvent extends EventObject {

    private static final long serialVersionUID = 7455205272815579848L;

    private final LocatorAction locatorAction;

    /**
     * Represent two types of locator actions: CLOSE or SELECT.
     * 
     * @author TG Team
     *
     */
    public enum LocatorAction {
	SELECT, CLOSE;
    }

    public LocatorEvent(final SingleAnalysisEntityLocator<? extends AbstractEntity> locator, final LocatorAction locatorAction) {
	super(locator);
	this.locatorAction = locatorAction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SingleAnalysisEntityLocator<? extends AbstractEntity<?>> getSource() {
	return (SingleAnalysisEntityLocator<? extends AbstractEntity<?>>)super.getSource();
    }

    public LocatorAction getLocatorAction() {
	return locatorAction;
    }
}
