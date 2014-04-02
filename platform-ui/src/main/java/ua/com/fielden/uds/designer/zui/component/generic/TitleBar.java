package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

public class TitleBar extends AbstractNode {
    private static final long serialVersionUID = -363748585223435786L;

    private PText title;
    private List<Button> buttons = new ArrayList<Button>();

    public TitleBar(String title, List<Button> buttons) {
        super(new Rectangle2D.Double(0., 0., 70., 10.));
        if (buttons != null) {
            for (Button button : buttons) {
                addButton(button);
            }
        }
        composeNode(title);
    }

    public void addButton(Button button) {
        if (!buttons.contains(button)) {
            buttons.add(button);
            if (button.getParentBar() != this) {
                button.addToTitleBar(this);
            }
        }
    }

    private int lengthOfOriginalTitle;

    private void composeNode(String title) {

        lengthOfOriginalTitle = title.length() - 2; // two chars are reserved for "..."

        setTitle(new PText(title));
        getTitle().setPickable(false);
        Font newFont = getTitle().getFont().deriveFont(Font.BOLD);
        getTitle().setTextPaint(Color.white);
        getTitle().setFont(newFont);
        getTitle().setOffset(5, 2);
        getLayoutIgnorantNodes().add(getTitle());
        addChild(getTitle());

        for (Button button : buttons) {
            addChild(button);
        }

        reshape(false);
    }

    public void updateTitle(String title) {
        // TODO requires some improvement in that that it should be possible to expand title bar to include as much of the new title text
        if (title.length() > lengthOfOriginalTitle) { // new title is potentially too long to fit
            getTitle().setText(title.substring(0, lengthOfOriginalTitle) + "...");
        } else {
            getTitle().setText(title);
        }
    }

    public void layoutComponents() {
        double xOffset = 5 + getTitle().getWidth() + 20; // 20 is the min dist from the title to the buttons or to the end of a bar
        setPedding(new AbstractNode.Pedding(0, 0, (int) xOffset, 5));

        Comparator<PNode> comparator = new Comparator<PNode>() {
            public int compare(PNode node1, PNode node2) {
                Integer index1 = buttons.indexOf(node1);
                Integer index2 = buttons.indexOf(node2);
                return index1.compareTo(index2);
            }
        };

        hrlLayoutComponents(-5, comparator);
    }

    public boolean setWidth(double width) {
        boolean result = super.setWidth(width);
        layoutComponents();
        return result;
    }

    public PText getTitle() {
        return title;
    }

    private void setTitle(PText title) {
        this.title = title;
    }

    public void adjustTitle() {
        setWidth(getParent().getWidth());
    }
}
