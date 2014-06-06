package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This class implements an AbstractNode, with text editing capabilities. It is intended to be used anywhere where in-line text editing is required. Text editing is implemented by
 * registering an invent listener, which handles mouse click on the component and substitutes PStyledText component with an instance of JTextComponent, which provides editing
 * capabilities. Please note that a general approach is borrowed from edu.umd.cs.piccolox.event.PStyledTextEventHandler; the same is correct for some bits an peaces of the
 * implementation.
 * 
 * @author 01es
 */
public class TextNode extends AbstractNode implements Cloneable {
    private static final long serialVersionUID = 6906192414962408622L;
    /**
     * The editor component is used for editing text. It is a Swing component and therefore cannot be added to a PNode -- only to PCanvas. Because of that an instance of PCanvas
     * needs to be passed into TexNode's constructor.
     */
    private JTextComponent editor;

    private transient PCanvas canvas;
    /**
     * docListener is used for re-sizing of editor while text is being entered; this also automatically triggers reshaping of TextNode.
     */
    private transient DocumentListener docListener;
    /**
     * editedText is the component which is used when text is displayed (e.i. not edited)
     */
    private PStyledText editedText;
    private DocumentFilter documentFilter;

    private int fontSize = 14;
    private boolean editable = true;
    /**
     * If false then clone() method uses one of the constructors to create a clone. Otherwise it uses super.clone() method.
     */
    private boolean dummyCloning = true;

    private transient IOnClickEventListener onClickHook;

    private Color textPaint = Color.black;

    public TextNode(PCanvas canvas, DocumentFilter filter) {
        this(canvas, filter, 14);
    }

    public TextNode(PCanvas canvas, DocumentFilter filter, int fontSize) {
        super(new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2));
        setPedding(new Pedding(3, 3, 3, 3));
        setFontSize(fontSize);
        setDocumentFilter(filter);
        setCanvas(canvas);
        setEditor(createDefaultEditor());

        ((AbstractDocument) getEditor().getDocument()).setDocumentFilter(getDocumentFilter());

        getCanvas().setLayout(null);
        getCanvas().add(getEditor());
        getEditor().setVisible(false);

        // instantiate edited text
        PStyledText newText = new PStyledText();
        newText.setPickable(false);
        AbstractDocument doc = (AbstractDocument) getEditor().getUI().getEditorKit(getEditor()).createDefaultDocument();
        if (doc instanceof StyledDocument) {
            if (!doc.getDefaultRootElement().getAttributes().isDefined(StyleConstants.FontFamily)
                    || !doc.getDefaultRootElement().getAttributes().isDefined(StyleConstants.FontSize)) {

                Font eFont = getEditor().getFont();
                SimpleAttributeSet sas = new SimpleAttributeSet();
                sas.addAttribute(StyleConstants.FontFamily, eFont.getFamily());
                sas.addAttribute(StyleConstants.FontSize, new Integer(eFont.getSize()));

                ((StyledDocument) doc).setParagraphAttributes(0, doc.getLength(), sas, false);
            }
        }
        newText.setDocument(doc);
        setEditedText(newText);

        ((AbstractDocument) getEditedText().getDocument()).setDocumentFilter(getDocumentFilter());

        setDocListener(createDocumentListener());
        listener = new InputEventHandler(this);
        addInputEventListener(listener);
    }

    private transient InputEventHandler listener;

    public void initiateEditing(PInputEvent event) {
        setPickable(true);
        listener.mouseClicked(event);
    }

    public TextNode(PCanvas canvas, String text, DocumentFilter filter) {
        this(canvas, filter, 14);
        setText(text);
        setDocumentFilter(filter);
    }

    public TextNode(PCanvas canvas, String text, int fontSize, DocumentFilter filter) {
        this(canvas, filter, fontSize);
        setText(text);
        setDocumentFilter(filter);
    }

    public void setText(String text) {
        getEditor().setDocument(getEditedText().getDocument());
        getEditedText().setEditing(true);
        getEditedText().getDocument().addDocumentListener(getDocListener());
        // assign the value
        getEditor().setText(text);
        getEditedText().syncWithDocument();
        getEditedText().setEditing(false);
    }

    public String getText() {
        getEditedText().syncWithDocument(); // just in case
        try {
            return getEditedText().getDocument().getText(0, getEditedText().getDocument().getLength());
        } catch (BadLocationException e) {
            // should not really happen
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method is copied from edu.umd.cs.piccolox.event.PStyledTextEventHandler
     * 
     * @return
     */
    protected JTextComponent createDefaultEditor() {
        JTextComponent textComponent = new JTextPane() {
            private static final long serialVersionUID = 6954176332471863456L;

            /**
             * Set some rendering hints - if we don't then the rendering can be inconsistent. Also, Swing doesn't work correctly with fractional metrics.
             */
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

                super.paint(g);
            }

            /**
             * If the standard scroll rect to visible is on, then you can get weird behaviors if the canvas is put in a scroll pane.
             */
            @SuppressWarnings("unused")
            public void scrollRectToVisible() {
            }
        };
        textComponent.setBorder(new CompoundBorder(new LineBorder(Color.black), new EmptyBorder(3, 3, 3, 3)));

        Font newFont = textComponent.getFont().deriveFont(Font.PLAIN, fontSize);
        textComponent.setFont(newFont);
        return textComponent;
    }

    /**
     * Creates document listener, which handles editor reshaping upon text entry.
     * 
     * @return
     */
    protected DocumentListener createDocumentListener() {
        return new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {
                reshapeEditorLater();
            }

            public void insertUpdate(DocumentEvent e) {
                reshapeEditorLater();
            }

            public void changedUpdate(DocumentEvent e) {
                reshapeEditorLater();
            }
        };
    }

    /**
     * Reshapes editor and the containing node (this).
     */
    public void reshapeEditor() {
        if (getEditedText() != null) {
            // Update the size to fit the new document - note that it is a 2 stage process
            Dimension prefSize = getEditor().getPreferredSize();

            Insets pInsets = getEditedText().getInsets();
            Insets jInsets = getEditor().getInsets();

            int width = (getEditedText().getConstrainWidthToTextWidth()) ? (int) prefSize.getWidth() : (int) (getEditedText().getWidth() - pInsets.left - pInsets.right
                    + jInsets.left + jInsets.right);
            prefSize.setSize(width, prefSize.getHeight());
            getEditor().setSize(prefSize);

            prefSize = getEditor().getPreferredSize();
            int height = (getEditedText().getConstrainHeightToTextHeight()) ? (int) prefSize.getHeight() : (int) (getEditedText().getHeight() - pInsets.top - pInsets.bottom
                    + jInsets.top + jInsets.bottom);

            prefSize.setSize(width, height);
            getEditor().setSize(prefSize);

            // need some twiddling to make the width dance together with an editor...
            if (enforceWidth) {
                if (getWidth() < width + getPedding().getRight() - getPedding().getLeft()) {
                    width = width + getPedding().getRight() - getPedding().getLeft();
                } else {
                    width = width + getPedding().getLeft();
                }
                setMinConstraint(new PDimension(width, height));
            } else {
                setMinConstraint(new PDimension(0, 0));
            }

            reshape(false); // reshaping this component
            getCanvas().repaint();
        }
    }

    private boolean enforceWidth = false;

    /**
     * Following a good Swing programming practice all UI events should be dispatched through the event thread.
     */
    protected void reshapeEditorLater() {
        reshapeEditor();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reshapeEditor();
            }
        });
    }

    private static class InputEventHandler extends PBasicInputEventHandler {
        private String originalValue;

        public KeyAdapter keyAdapeter = new KeyAdapter() {
            /**
             * Handles ESC and ENTER.
             */
            public void keyPressed(KeyEvent event) {
                if (originalValue == null) {
                    originalValue = node.getText();
                }
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // Esc needs to stop editing -- the same as loosing focus
                    node.setText(originalValue);
                    stopEditing();
                } else if (event.getKeyCode() == KeyEvent.VK_ENTER) { // Enter
                    stopEditing();
                }
            }
        };

        public FocusAdapter focusAdapter = new FocusAdapter() {
            public void focusLost(FocusEvent event) {
                stopEditing();
            }
        };

        private TextNode node;

        public InputEventHandler(TextNode node) {
            this.node = node;
            // getEventFilter().setMarksAcceptedEventsAsHandled(true);
            getEventFilter().setAndMask(InputEvent.BUTTON3_MASK);
            // register listeners for proper handling of key events
            node.getEditor().addFocusListener(focusAdapter);
            node.getEditor().addKeyListener(keyAdapeter);
        }

        public void mouseClicked(PInputEvent event) {
            if (node.isEditable()) {
                stopEditing();
                startEditing(event, node.getEditedText());
            }
        }

        public void startEditing(PInputEvent event, PStyledText text) {
            originalValue = null;
            // get the node's top right hand corner
            Insets pInsets = text.getInsets();
            Point2D nodePt = new Point2D.Double(pInsets.left, // text.getX() + pInsets.left
            pInsets.top); // text.getY() + pInsets.top
            text.localToGlobal(nodePt);
            event.getTopCamera().viewToLocal(nodePt);

            node.getEditor().setDocument(text.getDocument());
            Insets bInsets = node.getEditor().getBorder().getBorderInsets(node.getEditor());
            node.getEditor().setLocation((int) nodePt.getX() - bInsets.left, (int) nodePt.getY() - bInsets.top);
            node.getEditor().setVisible(true);

            dispatchEventToEditor(event);
            node.getCanvas().repaint();

            text.setEditing(true);
            text.getDocument().addDocumentListener(node.getDocListener());

            node.setEditedText(text);

            node.enforceWidth = true;
            node.reshapeEditorLater();
        }

        public void stopEditing() {
            originalValue = null;
            node.enforceWidth = false;
            if (node.getEditedText() != null) {
                node.getEditedText().getDocument().removeDocumentListener(node.getDocListener());
                node.getEditedText().setEditing(false);
                node.getEditedText().syncWithDocument();
                node.updateBoundObject();
                node.getEditor().setVisible(false);
            }
            node.reshapeEditorLater();
            if (node.onClickHook != null) {
                node.onClickHook.click(null);
            }
        }

        public void dispatchEventToEditor(final PInputEvent e) {
            // we have to nest the mouse press in two invoke later so that it is
            // fired so that the component has been completely validated at the new size
            // and the mouse event has the correct offset
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            MouseEvent me = new MouseEvent(node.getEditor(), MouseEvent.MOUSE_PRESSED, e.getWhen(), e.getModifiers() | InputEvent.BUTTON1_MASK, (int) (e.getCanvasPosition().getX() - node.getEditor().getX()), (int) (e.getCanvasPosition().getY() - node.getEditor().getY()), 1, false);
                            node.getEditor().dispatchEvent(me);
                        }
                    });
                }
            });
        }
    }

    private PCanvas getCanvas() {
        return canvas;
    }

    private void setCanvas(PCanvas canvas) {
        this.canvas = canvas;
    }

    private DocumentListener getDocListener() {
        return docListener;
    }

    private void setDocListener(DocumentListener docListener) {
        this.docListener = docListener;
    }

    public PStyledText getEditedText() {
        return editedText;
    }

    private void setEditedText(PStyledText editedText) {
        if (getEditedText() != null) {
            getEditedText().removeFromParent();
        }
        this.editedText = editedText;
        addChild(getEditedText());
    }

    public JTextComponent getEditor() {
        return editor;
    }

    private void setEditor(JTextComponent editor) {
        this.editor = editor;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    protected DocumentFilter getDocumentFilter() {
        return documentFilter;
    }

    protected void setDocumentFilter(DocumentFilter documentFilter) {
        this.documentFilter = documentFilter;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        getEditedText().syncWithDocument();
    }

    private IValue<? extends IValue<String>> value;

    public void bind(IValue<? extends IValue<String>> value) {
        this.value = value;
        setText(value.getValue().getValue());
    }

    public void updateBoundObject() {
        if (value != null) {
            value.getValue().setValue(getText());
        }
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        if (getEditedText() == null) {
            return;
        }
        AbstractDocument doc = (AbstractDocument) getEditedText().getDocument();
        if (doc instanceof StyledDocument) {
            if (!doc.getDefaultRootElement().getAttributes().isDefined(StyleConstants.FontFamily)
                    || !doc.getDefaultRootElement().getAttributes().isDefined(StyleConstants.FontSize)) {

                SimpleAttributeSet sas = new SimpleAttributeSet();
                sas.addAttribute(StyleConstants.FontSize, new Integer(fontSize));

                ((StyledDocument) doc).setParagraphAttributes(0, doc.getLength(), sas, false);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        if (dummyCloning) {
            return super.clone();
        } else {
            TextNode clone = new TextNode(getCanvas(), getText(), getDocumentFilter());
            clone.setOffset(getOffset());

            clone.setTextPaint(textPaint);
            clone.setBackgroundColor(getBackgroundColor());
            if (value != null) {
                clone.bind((IValue) value.clone());
            }
            return clone;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextNode)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return getText().equals(((TextNode) obj).getText());
    }

    public boolean isDummyCloning() {
        return dummyCloning;
    }

    public void setDummyCloning(boolean dummyCloning) {
        this.dummyCloning = dummyCloning;
    }

    public void setTextPaint(Color colour) {
        AbstractDocument doc = (AbstractDocument) getEditedText().getDocument();
        if (doc instanceof StyledDocument) {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(StyleConstants.Foreground, colour);
            ((StyledDocument) doc).setParagraphAttributes(0, doc.getLength(), sas, false);
        }

        textPaint = colour;
    }

    public IOnClickEventListener getOnClickHook() {
        return onClickHook;
    }

    public void setOnClickHook(IOnClickEventListener onClickHook) {
        this.onClickHook = onClickHook;
    }
}
