package ua.com.fielden.platform.swing.usertable;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.PaginatorModel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.pagination.development.Paginator;
import ua.com.fielden.platform.swing.pagination.development.Paginator.IPageChangeFeedback;

/**
 * Blocking panel that holds the {@link UserReviewTable} and buttons for loading and saving data.
 * 
 * @author oleh
 * 
 */
public class UserReview extends BlockingIndefiniteProgressLayer {

    private static final long serialVersionUID = 1L;

    private final JScrollPane viewScroller;

    private final UserReviewTable userTable;

    /**
     * Initiates the {@link UserReview} with {@link UserReviewModel}
     * 
     * @param userReviewModel
     */
    public UserReview(final UserReviewModel userReviewModel) {
        super(null, "");

        // Creates panel that holds controls and is the view for blocking panel
        final JPanel panel = new JPanel(new MigLayout("fill", "[]push[]", "[][fill,grow][]"));

        // Create panel with pagination controls.
        final JPanel paginationPanel = createPaginationPanel(userReviewModel);

        // Creating UserReview table with model
        userTable = new UserReviewTable(userReviewModel.getUserTableModel());
        viewScroller = new JScrollPane(userTable);

        // Creates load and save buttons
        final JButton loadButton = new JButton(userReviewModel.getLoadAction(this));
        final JButton saveButton = new JButton(userReviewModel.getSaveAction(this));

        // Adding controls to the view panel and setting the view of the view panel
        setView(panel);
        panel.add(paginationPanel, "span 2, growx, wrap");
        panel.add(viewScroller, "span 2, growx, wrap");
        panel.add(loadButton);
        panel.add(saveButton);

        // Component listener loads data at the start of the UserReview panel
        viewScroller.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                userReviewModel.getLoadAction(UserReview.this).actionPerformed(null);
                viewScroller.removeComponentListener(this);
            }
        });
    }

    /**
     * Returns the number of users on one page.
     * 
     * @return
     */
    public int getPageSize() {
        final double pageSize = viewScroller.getSize().getHeight() / userTable.getRowHeight();
        final int pageCapacity = (int) Math.floor(pageSize);
        return pageCapacity > 1 ? pageCapacity : 1;
    }

    /**
     * Creates and returns the panel that contains pagination controls.
     * 
     * @return
     */
    private JPanel createPaginationPanel(final UserReviewModel model) {

        final PaginatorModel paginatorModel = new PaginatorModel();
        paginatorModel.addPageHolder(model.getPageHolder());
        paginatorModel.selectPageHolder(model.getPageHolder());

        final JLabel feedBack = new JLabel("Page 0 of 0");
        final Paginator paginator = new Paginator(paginatorModel, createPaginatorFeedback(feedBack), this);

        final JPanel controlPanel = new JPanel(new MigLayout("fill, insets 0", "push[][][][]20[]push", "[c,fill]"));

        controlPanel.add(newUnfocusableButton(paginator.getFirst()));
        controlPanel.add(newUnfocusableButton(paginator.getPrev()));
        controlPanel.add(newUnfocusableButton(paginator.getNext()));
        controlPanel.add(newUnfocusableButton(paginator.getLast()));
        controlPanel.add(feedBack);

        return controlPanel;

    }

    /**
     * Creates new unfocusable button with specified action.
     * 
     * @param action
     * @return
     */
    private JButton newUnfocusableButton(final Action action) {
        final JButton button = new JButton(action);
        button.setFocusable(false);
        return button;
    }

    /**
     * Creates and returns the pagination {@link IPageChangeFeedback} implementation
     * 
     * @param feedBack
     * 
     * @return
     */
    private IPageChangeFeedback createPaginatorFeedback(final JLabel feedBack) {
        return new IPageChangeFeedback() {
            @Override
            public void feedback(final IPage<?> page) {
                feedBack.setText(page != null ? page.toString() : "Page 0 of 0");
            }

            @Override
            public void enableFeedback(final boolean enable) {
                feedBack.setEnabled(enable);
            }
        };
    }
}
