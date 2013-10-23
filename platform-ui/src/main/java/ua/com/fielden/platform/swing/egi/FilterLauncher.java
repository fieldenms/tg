package ua.com.fielden.platform.swing.egi;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.popup.JidePopup;

public class FilterLauncher {

    /**
     * @param args
     */
    public static void main(final String[] args) {
	final JFrame frame = new JFrame("Table filter example!");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow]"));
	frame.setPreferredSize(new Dimension(640, 480));

	final TableModel tableModel = createTableModel();
	final QuickTableFilterField _filterField = createFilterField(tableModel);
	final JidePopup _popup = createPopup(_filterField);
	final JScrollPane _tableScroll = createTable(_filterField.getDisplayTableModel());

	frame.add(_tableScroll);

	final KeyStroke showSearch = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK); // CTRL+F
	final String SHOW_SEARCH = "SHOW_SEARCH_PANEL";
	final Action showSearchAction = createShowSearchAction(_popup, _tableScroll);
	_tableScroll.getActionMap().put(SHOW_SEARCH, showSearchAction);
	_tableScroll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(showSearch, SHOW_SEARCH);

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		//JFrame.setDefaultLookAndFeelDecorated(true);
		//JDialog.setDefaultLookAndFeelDecorated(true);

		//SwingUtilitiesEx.installNimbusLnFifPossible();

		frame.pack();
		frame.setVisible(true);
	    }
	});
    }

    private static Action createShowSearchAction(final JidePopup _popup, final JScrollPane _tableScroll) {
	return new AbstractAction() {

	    private static final long serialVersionUID = 8327840776664813556L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		_popup.showPopup(SwingConstants.SOUTH_WEST, _tableScroll);
	    }
	};
    }

    private static JidePopup createPopup(final QuickTableFilterField _filterField) {

	final JidePopup _popup = com.jidesoft.popup.JidePopupFactory.getSharedInstance().createPopup();
	final Action hideSearchAction = createHideSearchAction(_popup);

	_popup.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	_popup.setResizable(false);
	_popup.setMovable(false);
	_popup.add(new JButton(hideSearchAction));
	_popup.add(_filterField);
	_popup.setTransient(false);
	_popup.setDefaultFocusComponent(_filterField.getTextField());
	_popup.addPopupMenuListener(new PopupMenuListener() {

	    @Override
	    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		_filterField.setSearchingText("");
	    }

	    @Override
	    public void popupMenuCanceled(final PopupMenuEvent e) {
		// TODO Auto-generated method stub

	    }
	});

	final KeyStroke hideSearch = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // ESC
	final String HIDE_SEARCH = "HIDE_SEARCH_PANEL";
	_popup.getActionMap().put(HIDE_SEARCH, hideSearchAction);
	_popup.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(hideSearch, HIDE_SEARCH);

	return _popup;
    }

    private static Action createHideSearchAction(final JidePopup _popup) {
	return new AbstractAction("Close") {

	    private static final long serialVersionUID = 8327840776664813556L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		_popup.hidePopup();
	    }
	};
    }

    private static QuickTableFilterField createFilterField(final TableModel tableModel) {
	final QuickTableFilterField _filterField = new QuickTableFilterField(tableModel, new int[] { 0, 1, 2 });
	_filterField.setHintText("Type here to filter...");
	return _filterField;
    }

    private static TableModel createTableModel() {
	final Object[] columnNames = new Object[] { "Base form", "Past simple", "Past participle" };
	final Object[][] data = new Object[5][];
	data[0] = new Object[] { "buy", "bought", "bought" };
	data[1] = new Object[] { "teach", "tought", "tought" };
	data[2] = new Object[] { "see", "saw", "seen" };
	data[3] = new Object[] { "do", "did", "done" };
	data[4] = new Object[] { "fly", "flew", "flown" };

	return new DefaultTableModel(data, columnNames);
    }

    private static JScrollPane createTable(final TableModel tableModel) {
	return new JScrollPane(new JTable(tableModel));
    }

}
