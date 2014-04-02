package ua.com.fielden.platform.swing.menu.filter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.jxlayer.JXLayer;

import ua.com.fielden.platform.swing.menu.filter.IFilterBreakListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;

/**
 * This an autocompleter component, which is a JTextField wrapped into JXLayer. It utilises an instance of ValueMatcher to search for matching values.
 * 
 * @author 01es
 * 
 */
public class FilterControl extends JXLayer<JTextField> {
    private static final long serialVersionUID = 1L;

    private final IFilterableModel model;
    // we use this flag to workaround the bug that setText() will trigger the hint popup.
    private boolean keyTyped = false;

    private String prevFilter;

    /**
     * Constructs a filtering layer for the specified text field component using the provided filtering model.
     * 
     * @param textComponent
     *            -- used as a holder for selected values
     * @param model
     *            -- an instance of {@link IFilterableModel}.
     * @param caption
     *            -- a short informative message, which is displayed on top of the textComponent if it is empty and not focused.
     */
    public FilterControl(//
    final JTextField textComponent,//
            final IFilterableModel model,//
            final String caption) { //
        super(textComponent);
        this.model = model;
        model.addFilterBreakListener(new IFilterBreakListener() {

            @Override
            public void doAfterBreak(final IFilterableModel model) {
                getView().setText(prevFilter);
                refresh();
            }

        });

        getView().getDocument().addDocumentListener(documentListener);
        getView().addKeyListener(new KeyListener() {
            public void keyTyped(final KeyEvent e) {
            }

            public void keyPressed(final KeyEvent e) {
            }

            public void keyReleased(final KeyEvent e) {
                if (KeyEvent.VK_ESCAPE != e.getKeyCode()) {
                    setKeyTyped(true);
                }
            }
        });

        // instantiates UI and assigns it to this layer
        new FilterUi(this, caption);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                getView().requestFocusInWindow();
            }
        });
    }

    /**
     * A convenient method for filter clearing.
     */
    public void clear() {
        getView().setText(""); // clear text and refresh filterable model
        refresh();
    }

    /**
     * Refreshes filterable model.
     */
    public void refresh() {
        model.filter(getView().getText());
        prevFilter = getView().getText();
    }

    FilterUi getUi() {
        return (FilterUi) super.getUI();
    }

    public void setEditable(final boolean flag) {
        getView().setEditable(flag);
    }

    /**
     * This listener is responsible to triggering filter model refresh. The refresh happens by timer -- 200 millis are given for each key press before applying the filter.
     */
    private DocumentListener documentListener = new DocumentListener() {
        private Timer timer = new Timer(200, new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (isKeyTyped()) {
                    refresh();
                    setKeyTyped(false);
                }
            }
        });

        public void insertUpdate(final DocumentEvent e) {
            startTimer();
        }

        public void removeUpdate(final DocumentEvent e) {
            startTimer();
        }

        public void changedUpdate(final DocumentEvent e) {
        }

        void startTimer() {
            if (timer.isRunning()) {
                timer.restart();
            } else {
                timer.setRepeats(false);
                timer.start();
            }
        }
    };

    protected boolean isKeyTyped() {
        return keyTyped;
    }

    private void setKeyTyped(final boolean keyTyped) {
        this.keyTyped = keyTyped;
    }
}
