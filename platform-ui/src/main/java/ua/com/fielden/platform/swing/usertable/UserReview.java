package ua.com.fielden.platform.swing.usertable;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;

/**
 * Blocking panel that holds the {@link UserReviewTable} and buttons for loading and saving data.
 * 
 * @author oleh
 * 
 */
public class UserReview extends BlockingIndefiniteProgressLayer {

    private static final long serialVersionUID = 1L;

    /**
     * Initiates the {@link UserReview} with {@link UserReviewModel}
     * 
     * @param userReviewModel
     */
    public UserReview(final UserReviewModel userReviewModel) {
	super(null, "");

	// Creates panel that holds controls and is the view for blocking panel
	final JPanel panel = new JPanel(new MigLayout("fill", "[]push[]", "[fill,grow][]"));

	// Creates load and save buttons
	final JButton loadButton = new JButton(userReviewModel.getLoadAction(this));
	final JButton saveButton = new JButton(userReviewModel.getSaveAction(this));

	// Creating UserReview table with model
	final UserReviewTable userTable = new UserReviewTable(userReviewModel.getUserTableModel());

	// Adding controls to the view panel and setting the view of the view panel
	setView(panel);
	panel.add(new JScrollPane(userTable), "span 2, growx, wrap");
	panel.add(loadButton);
	panel.add(saveButton);

	// Component listener loads data at the start of the UserReview panel
	addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(final ComponentEvent e) {
		userReviewModel.getLoadAction(UserReview.this).actionPerformed(null);

		final ComponentListener refToThis = this;
		UserReview.this.removeComponentListener(refToThis);
	    }
	});
    }
}
