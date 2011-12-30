package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class StubCriteriaPanel extends JPanel implements ICriteriaPanel {

    private static final long serialVersionUID = -3545898148806495676L;

    @Override
    public boolean canConfigure() {
	return true;
    }

    /**
     * Must updates the underlying model according to it's view.
     * 
     * @return
     */
    public boolean updateModel(){
	//TODO must implement that.
	return true;
    }

    @Override
    public Action getSwitchAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = -8861824249867165117L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final boolean selected = ((JToggleButton) e.getSource()).isSelected();
		if (selected) {
		    System.out.println("edit criteria panel");
		} else {
		    System.out.println("view criteria panel");;
		}
	    }
	};
    }

}
