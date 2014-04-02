package ua.com.fielden.platform.algorithm.bfs;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;

/**
 * A test case to ensure correct functionality of the Breadth-First Search algorithm when the state space is a graph (i.e. there are loops).
 * 
 * @author TG Team
 * 
 */
public class BfsAlgorithmForGraphTest {
    // let's create the test tree
    private final TreeNode level1_1;
    private final TreeNode level1_1_1;
    private final TreeNode level1_1_2;

    private final TreeNode level1_2;
    private final TreeNode level1_2_1;
    private final TreeNode level1_2_2;

    private final TreeNode level1_3;
    private final TreeNode level1_3_1;
    private final TreeNode level1_3_2;
    private final TreeNode level1_3_3;

    private final TreeNode tree = new TreeNode("root");
    {
        tree.//
        addChild(//
        level1_1 = new TreeNode("level1_1").//
        addChild(level1_1_1 = new TreeNode("level1_1_1")).//
        addChild(level1_1_2 = new TreeNode("level1_1_2"))//
        ).addChild(//
        level1_2 = new TreeNode("level1_2").//
        addChild(level1_2_1 = new TreeNode("level1_2_1")).//
        addChild(level1_2_2 = new TreeNode("level1_2_2"))//
        ).addChild(//
        level1_3 = new TreeNode("level1_3").//
        addChild(level1_3_1 = new TreeNode("level1_3_1").addChild(tree)).// make a loop
        addChild(level1_3_2 = new TreeNode("level1_3_2")).//
        addChild(level1_3_3 = new TreeNode("level1_3_3"))//
        );
    }
    // create a visiting predicate to visit all nodes
    private ITreeNodePredicate<String, TreeNode> visitAllPredicate;

    @Before
    public void setUp() {
        visitAllPredicate = new ITreeNodePredicate<String, TreeNode>() {
            private int numberOfVisitedNodes = 0;

            @Override
            public boolean eval(final TreeNode node) {
                node.incVisitCount();
                numberOfVisitedNodes++;
                return false;
            }

            public String toString() {
                return numberOfVisitedNodes + "";
            };
        };

        level1_1.reset();
        level1_1_1.reset();
        level1_1_2.reset();
        level1_2.reset();
        level1_2_1.reset();
        level1_2_2.reset();
        level1_3.reset();
        level1_3_1.reset();
        level1_3_2.reset();
        level1_3_3.reset();
    }

    @Test(timeout = 10000)
    public void test_that_all_nodes_are_visited_exactly_once() {
        final BreadthFirstSearch<String, TreeNode> bfs = new BreadthFirstSearch<String, TreeNode>();
        bfs.search(tree, visitAllPredicate);
        assertEquals("Incorrect number of visited nodes.", "11", visitAllPredicate.toString());

        assertEquals("Incorrect number of node visitations.", 1, tree.visits());

        assertEquals("Incorrect number of node visitations.", 1, level1_1.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_1_1.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_1_2.visits());

        assertEquals("Incorrect number of node visitations.", 1, level1_2.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_2_1.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_2_2.visits());

        assertEquals("Incorrect number of node visitations.", 1, level1_3.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_3_1.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_3_2.visits());
        assertEquals("Incorrect number of node visitations.", 1, level1_3_3.visits());
    }

}
