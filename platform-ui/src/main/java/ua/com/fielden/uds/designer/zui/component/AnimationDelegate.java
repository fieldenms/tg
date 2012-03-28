package ua.com.fielden.uds.designer.zui.component;

import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This is a convenience class for implementing PActivityDelegate interface where events need to triggered during or after animation is completed.
 * 
 * @author 01es
 * 
 */
public abstract class AnimationDelegate implements PActivity.PActivityDelegate {

    private PInputEvent event;

    public AnimationDelegate(PInputEvent event) {
	setEvent(event);
    }

    public void activityFinished(PActivity arg0) {
    }

    public void activityStarted(PActivity arg0) {
    }

    public void activityStepped(PActivity arg0) {
    }

    protected PInputEvent getEvent() {
	return event;
    }

    private void setEvent(PInputEvent event) {
	this.event = event;
    }

}
