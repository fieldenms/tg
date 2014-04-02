package ua.com.fielden.platform.swing.menu.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.junit.Before;
import org.junit.Test;

public class FilterableTreeModelTestCase {

    private FilterableTreeModel filterTreeModel;
    private Vector<DefaultMutableTreeNode> treeNodes;
    private String pattern;

    @Before
    public void setUp() {
        treeNodes = new Vector<DefaultMutableTreeNode>();
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        treeNodes.add(root);// 0

        final DefaultMutableTreeNode color = new DefaultMutableTreeNode("color");
        final DefaultMutableTreeNode red = new DefaultMutableTreeNode("red");
        final DefaultMutableTreeNode green = new DefaultMutableTreeNode("green");
        final DefaultMutableTreeNode blue = new DefaultMutableTreeNode("blue");
        color.add(red);
        color.add(green);
        color.add(blue);
        treeNodes.add(color);// 1
        treeNodes.add(red);// 2
        treeNodes.add(green);// 3
        treeNodes.add(blue);// 4

        final DefaultMutableTreeNode sport = new DefaultMutableTreeNode("sport");
        final DefaultMutableTreeNode soccer = new DefaultMutableTreeNode("soccer");
        final DefaultMutableTreeNode basketball = new DefaultMutableTreeNode("basketball");
        final DefaultMutableTreeNode lie = new DefaultMutableTreeNode("lie");
        final DefaultMutableTreeNode die = new DefaultMutableTreeNode("die");
        final DefaultMutableTreeNode football = new DefaultMutableTreeNode("football");
        final DefaultMutableTreeNode hockey = new DefaultMutableTreeNode("hockey");
        final DefaultMutableTreeNode voleyball = new DefaultMutableTreeNode("voleyball");
        sport.add(soccer);
        sport.add(basketball);
        sport.add(football);
        sport.add(hockey);
        sport.add(voleyball);
        basketball.add(lie);
        basketball.add(die);
        treeNodes.add(sport);// 5
        treeNodes.add(soccer);// 6
        treeNodes.add(basketball);// 7
        treeNodes.add(lie);// 8
        treeNodes.add(die);// 9
        treeNodes.add(football);// 10
        treeNodes.add(hockey);// 11
        treeNodes.add(voleyball);// 12

        final DefaultMutableTreeNode food = new DefaultMutableTreeNode("food");
        final DefaultMutableTreeNode cake = new DefaultMutableTreeNode("cake");
        final DefaultMutableTreeNode bananna = new DefaultMutableTreeNode("banana");
        final DefaultMutableTreeNode pizza = new DefaultMutableTreeNode("pizza");
        final DefaultMutableTreeNode cheesburger = new DefaultMutableTreeNode("cheesburger");
        food.add(cake);
        food.add(bananna);
        food.add(pizza);
        food.add(cheesburger);
        treeNodes.add(food); // 13
        treeNodes.add(cake); // 14
        treeNodes.add(bananna); // 15
        treeNodes.add(pizza); // 16
        treeNodes.add(cheesburger); // 17

        root.add(color);
        root.add(sport);
        root.add(food);

        filterTreeModel = new FilterableTreeModel(new DefaultTreeModel(root));
        filterTreeModel.addFilter(new StringFilter());
        pattern = "*s*";
    }

    @Test
    public void firstTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(false);
        filterTreeModel.setKeepAllChildren(false);
        filterTreeModel.setMatchesLeafNodeOnly(false);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("The root node sould always exist.", root);
        assertEquals("Incorrect number of root children.", 2, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(5), childNode);
        assertEquals("Incorrect numnumber of children in sport node.", 2, filterTreeModel.getChildCount(childNode));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect sport child", treeNodes.get(6), nextSibling);

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 1);
        assertEquals("Incorrect sport child", treeNodes.get(7), nextSibling);
        assertEquals("Incorrect numnumber of children in basketball node.", 0, filterTreeModel.getChildCount(nextSibling));

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child.", treeNodes.get(13), childNode);
        assertEquals("Incorrect numnumber of children in food node.", 1, filterTreeModel.getChildCount(childNode));

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect food child.", treeNodes.get(17), nextSibling);
    }

    @Test
    public void secondTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(false);
        filterTreeModel.setKeepAllChildren(false);
        filterTreeModel.setMatchesLeafNodeOnly(true);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("The root node sould always exist.", root);
        assertEquals("Incorrect number of root children.", 3, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(1), childNode);
        assertEquals("Incorrect numnumber of children in color node.", 0, filterTreeModel.getChildCount(childNode));

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child.", treeNodes.get(5), childNode);
        assertEquals("Incorrect numnumber of children in sport node.", 2, filterTreeModel.getChildCount(childNode));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect sport child.", treeNodes.get(6), nextSibling);

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 1);
        assertEquals("Incorrect sport child.", treeNodes.get(7), nextSibling);
        assertEquals("Incorrect numnumber of children in basketball node.", 0, filterTreeModel.getChildCount(nextSibling));

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 2);
        assertEquals("Incorrect root child.", treeNodes.get(13), childNode);
        assertEquals("Incorrect numnumber of children in food node.", 1, filterTreeModel.getChildCount(childNode));

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect food child.", treeNodes.get(17), nextSibling);
    }

    @Test
    public void testThirdLevelFiltering() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(false);
        filterTreeModel.setKeepAllChildren(false);
        filterTreeModel.setMatchesLeafNodeOnly(false);
        filterTreeModel.filter("die");

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("Root node is no filterable, and thus should exist", root);
        assertEquals("Incorrect number of root children.", 1, filterTreeModel.getChildCount(root));

        final DefaultMutableTreeNode rootChild = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(5), rootChild);
        assertEquals("Incorrect numnumber of children in sport node.", 1, filterTreeModel.getChildCount(rootChild));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(rootChild, 0);
        assertEquals("Incorrect sport sibling.", treeNodes.get(7), nextSibling);

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(nextSibling, 0);
        assertEquals("Incorrect basketball sibling.", treeNodes.get(9), nextSibling);
    }

    @Test
    public void thirdTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(false);
        filterTreeModel.setKeepAllChildren(true);
        filterTreeModel.setMatchesLeafNodeOnly(false);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("root should always be present", root);
        assertEquals("Incorrect number of root children.", 2, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode rootChild = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child", treeNodes.get(5), filterTreeModel.getChild(root, 0));
        assertEquals("Incorect number of sport children", 5, filterTreeModel.getChildCount(rootChild));

        int counter = 6;
        for (int childIndex = 0; childIndex < 5; childIndex++) {
            final DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) filterTreeModel.getChild(rootChild, childIndex);
            assertEquals("Incorrect sport child", treeNodes.get(counter), siblingNode);
            if (childIndex == 1) {
                assertEquals("Incorrect number of basketball children.", 2, filterTreeModel.getChildCount(siblingNode));
                for (int siblingIndex = 0; siblingIndex < 2; siblingIndex++) {
                    final DefaultMutableTreeNode basketBallSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(siblingNode, siblingIndex);
                    counter++;
                    assertEquals("Incorrect basketball child", treeNodes.get(counter), basketBallSibling);
                }
            }
            counter++;
        }

        rootChild = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child", treeNodes.get(13), rootChild);
        assertEquals("Incorect number of food children", 1, filterTreeModel.getChildCount(rootChild));
        assertEquals("Incorect food child", treeNodes.get(17), filterTreeModel.getChild(rootChild, 0));
    }

    @Test
    public void fourthTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(false);
        filterTreeModel.setKeepAllChildren(true);
        filterTreeModel.setMatchesLeafNodeOnly(true);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("The root node sould always exist.", root);
        assertEquals("Incorrect number of root children.", 3, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(1), childNode);
        assertEquals("Incorrect numnumber of children in color node.", 0, filterTreeModel.getChildCount(childNode));

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child.", treeNodes.get(5), childNode);
        assertEquals("Incorrect numnumber of children in sport node.", 2, filterTreeModel.getChildCount(childNode));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect sport child.", treeNodes.get(6), nextSibling);

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 1);
        assertEquals("Incorrect sport child.", treeNodes.get(7), nextSibling);
        assertEquals("Incorrect numnumber of children in basketball node.", 0, filterTreeModel.getChildCount(nextSibling));

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 2);
        assertEquals("Incorrect root child.", treeNodes.get(13), childNode);
        assertEquals("Incorrect numnumber of children in food node.", 1, filterTreeModel.getChildCount(childNode));

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect food child.", treeNodes.get(17), nextSibling);

    }

    @Test
    public void fifthTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(true);
        filterTreeModel.setKeepAllChildren(false);
        filterTreeModel.setMatchesLeafNodeOnly(false);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("The root node sould always exist.", root);
        assertEquals("Incorrect number of root children.", 2, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(5), childNode);
        assertEquals("Incorrect numnumber of children in sport node.", 2, filterTreeModel.getChildCount(childNode));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect sport child", treeNodes.get(6), nextSibling);

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 1);
        assertEquals("Incorrect sport child", treeNodes.get(7), nextSibling);
        assertEquals("Incorrect numnumber of children in basketball node.", 0, filterTreeModel.getChildCount(nextSibling));

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child.", treeNodes.get(13), childNode);
        assertEquals("Incorrect numnumber of children in food node.", 1, filterTreeModel.getChildCount(childNode));

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect food child.", treeNodes.get(17), nextSibling);
    }

    @Test
    public void sixthTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(true);
        filterTreeModel.setKeepAllChildren(false);
        filterTreeModel.setMatchesLeafNodeOnly(true);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("The root node sould always exist.", root);
        assertEquals("Incorrect number of root children.", 2, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(5), childNode);
        assertEquals("Incorrect numnumber of children in sport node.", 1, filterTreeModel.getChildCount(childNode));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect sport child", treeNodes.get(6), nextSibling);

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child.", treeNodes.get(13), childNode);
        assertEquals("Incorrect numnumber of children in food node.", 1, filterTreeModel.getChildCount(childNode));

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect food child.", treeNodes.get(17), nextSibling);
    }

    @Test
    public void seventhTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(true);
        filterTreeModel.setKeepAllChildren(true);
        filterTreeModel.setMatchesLeafNodeOnly(false);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("root should always be present", root);
        assertEquals("Incorrect number of root children.", 2, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode rootChild = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child", treeNodes.get(5), filterTreeModel.getChild(root, 0));
        assertEquals("Incorect number of sport children", 5, filterTreeModel.getChildCount(rootChild));

        int counter = 6;
        for (int childIndex = 0; childIndex < 5; childIndex++) {
            final DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) filterTreeModel.getChild(rootChild, childIndex);
            assertEquals("Incorrect sport child", treeNodes.get(counter), siblingNode);
            if (childIndex == 1) {
                assertEquals("Incorrect number of basketball children.", 2, filterTreeModel.getChildCount(siblingNode));
                for (int siblingIndex = 0; siblingIndex < 2; siblingIndex++) {
                    final DefaultMutableTreeNode basketBallSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(siblingNode, siblingIndex);
                    counter++;
                    assertEquals("Incorrect basketball child", treeNodes.get(counter), basketBallSibling);
                }
            }
            counter++;
        }

        rootChild = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child", treeNodes.get(13), rootChild);
        assertEquals("Incorect number of food children", 1, filterTreeModel.getChildCount(rootChild));
        assertEquals("Incorect food child", treeNodes.get(17), filterTreeModel.getChild(rootChild, 0));
    }

    @Test
    public void eighthTestCase() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(true);
        filterTreeModel.setKeepAllChildren(true);
        filterTreeModel.setMatchesLeafNodeOnly(true);
        filterTreeModel.filter(pattern);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("The root node sould always exist.", root);
        assertEquals("Incorrect number of root children.", 2, filterTreeModel.getChildCount(root));

        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child.", treeNodes.get(5), childNode);
        assertEquals("Incorrect numnumber of children in sport node.", 1, filterTreeModel.getChildCount(childNode));

        DefaultMutableTreeNode nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect sport child", treeNodes.get(6), nextSibling);

        childNode = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 1);
        assertEquals("Incorrect root child.", treeNodes.get(13), childNode);
        assertEquals("Incorrect numnumber of children in food node.", 1, filterTreeModel.getChildCount(childNode));

        nextSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(childNode, 0);
        assertEquals("Incorrect food child.", treeNodes.get(17), nextSibling);
    }

    @Test
    public void addTest() {
        filterTreeModel.setAndMode(true);
        filterTreeModel.setHideEmptyParentNode(true);
        filterTreeModel.setKeepAllChildren(true);
        filterTreeModel.setMatchesLeafNodeOnly(false);
        filterTreeModel.filter("*sp*");

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) filterTreeModel.getRoot();
        assertNotNull("root should always be present", root);
        assertEquals("Incorrect number of root children.", 1, filterTreeModel.getChildCount(root));

        final DefaultMutableTreeNode rootChild = (DefaultMutableTreeNode) filterTreeModel.getChild(root, 0);
        assertEquals("Incorrect root child", treeNodes.get(5), filterTreeModel.getChild(root, 0));
        assertEquals("Incorect number of sport children", 5, filterTreeModel.getChildCount(rootChild));

        int counter = 6;
        for (int childIndex = 0; childIndex < 5; childIndex++) {
            final DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) filterTreeModel.getChild(rootChild, childIndex);
            assertEquals("Incorrect sport child", treeNodes.get(counter), siblingNode);
            if (childIndex == 1) {
                assertEquals("Incorrect number of basketball children.", 2, filterTreeModel.getChildCount(siblingNode));
                for (int siblingIndex = 0; siblingIndex < 2; siblingIndex++) {
                    final DefaultMutableTreeNode basketBallSibling = (DefaultMutableTreeNode) filterTreeModel.getChild(siblingNode, siblingIndex);
                    counter++;
                    assertEquals("Incorrect basketball child", treeNodes.get(counter), basketBallSibling);
                }
            }
            counter++;
        }

    }
}
