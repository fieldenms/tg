package ua.com.fielden.platform.swing.egi;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.pagination.PaginatorModel;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.pagination.development.Paginator;
import ua.com.fielden.platform.swing.pagination.development.Paginator.IPageChangeFeedback;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

public abstract class SubEgiPanel<T extends AbstractEntity<?>, C extends AbstractEntity<?>> extends BlockingIndefiniteProgressLayer {

    private static final long serialVersionUID = -5587630205984698663L;

    public SubEgiPanel(final T value) {
	super(null, "Loading...");
	final PropertyTableModel<C> tableModel = createTableModel();
	final PageHolder pageHolder = new PageHolder();
	pageHolder.addPageChangedListener(createPageChangedListener(tableModel));
	final Action loadDataAction = createLoadDataAction(value, pageHolder);

	final JPanel view = new JPanel(new MigLayout("fill, insets 5", "[grow, fill]", "[][:143:,grow, fill]"));

	//Creating paginator view.
	final PaginatorModel paginatorModel = new PaginatorModel();
	paginatorModel.addPageHolder(pageHolder);
	paginatorModel.selectPageHolder(pageHolder);
	final JLabel feedBack = new JLabel("Page 0 of 0");
	final Paginator paginator = new Paginator(paginatorModel, createPaginatorFeedback(feedBack), this);
	final JPanel paginatorPanel = new JPanel(new MigLayout("fill, insets 0", "push[][][][]20[]push", "[]"));
	paginatorPanel.add(newButton(paginator.getFirst(), false));
	paginatorPanel.add(newButton(paginator.getPrev(), false));
	paginatorPanel.add(newButton(paginator.getNext(), false));
	paginatorPanel.add(newButton(paginator.getLast(), false));
	paginatorPanel.add(feedBack);

	//Creating Egi panel.
	final EntityGridInspector<?> egi = new EntityGridInspector<>(tableModel, false);
	egi.setRowHeight(EgiPanel.ROW_HEIGHT);
	egi.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
	egi.getColumnModel().getSelectionModel().setSelectionMode(SINGLE_INTERVAL_SELECTION);
	final JScrollPane scroll = new JScrollPane(egi);

	view.add(paginatorPanel, "wrap");
	view.add(scroll);
	view.addComponentListener(createFirstTimeOpenHandler(view, loadDataAction));
	setView(view);
    }

    private IPageChangedListener createPageChangedListener(final PropertyTableModel<C> model) {
	return new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		SwingUtilitiesEx.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			model.setInstances(((IPage<C>)e.getNewPage()).data());
		    }
		});
	    }
	};
    }

    protected abstract PropertyTableModel<C> createTableModel();

    protected abstract IPage<C> getData(T value);

    private ComponentListener createFirstTimeOpenHandler(final JPanel view, final Action loadDataAction) {
	return new ComponentAdapter() {
	    @Override
	    public void componentResized(final ComponentEvent e) {
		loadDataAction.actionPerformed(null);
		view.removeComponentListener(this);
	    }
	};
    }

    private JButton newButton(final Action action, final boolean focusable) {
	final JButton button = new JButton(action);
	button.setFocusable(focusable);
	return button;
    }

    private Action createLoadDataAction(final T value, final PageHolder pageHolder) {
	return new BlockingLayerCommand<IPage<C>>("Load", this) {

	    private static final long serialVersionUID = -6931303131255570628L;

	    @Override
	    protected IPage<C> action(final ActionEvent e) throws Exception {
		return getData(value);
	    }

	    @Override
	    protected void postAction(final IPage<C> value) {
		pageHolder.newPage(value);
		super.postAction(value);
	    }
	};
    }

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
