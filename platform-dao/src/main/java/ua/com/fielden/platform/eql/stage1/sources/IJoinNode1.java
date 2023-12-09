package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage1.TransformationResultFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

/**
 * Starting with stage 1 the structure for the {@code FROM} statement of the EQL query becomes tree-like.
 * The diagram below illustrates a typical pattern of such left-deep tree.
 * <pre>
 *       Join ((A+B)+C)+D
 *           /           \
 *     Join (A+B)+C       D
 *         /       \
 *   Join A+B       C
 *       /   \
 *      A     B
 * </pre>
 * This specific diagram corresponds to the following SQL statement: 
 * <pre>
 * SELECT * 
 * FROM A
 * JOIN B ON A.ID = B.ID
 * JOIN C ON A.ID = C.ID
 * JOIN D ON A.ID = D.ID
 * </pre>
 * 
 * The same tree-like structure (also different classes) is used for stages 2 and 3.
 * Stage 3 may also contain bushy trees to represent implicit joins (refer {@link IJoinNode3}).
 * 
 * A node in such tree could either be an inner node {@link JoinInnerNode1} or a leaf node {@link JoinLeafNode1}.
 * Inner nodes contain two query sources. Leaf nodes contain a single source. Each source could either represent a table or another query/s.
 * 
 * The main difference between the join trees at different stages lies in how far query-based sources (i.e., not table-based) are processed.
 * 
 * @param <T> -- a type of corresponding join node at stage 2.
 */
public interface IJoinNode1<T extends IJoinNode2<? extends IJoinNode3>> extends ITransformableFromStage1To2<TransformationResultFromStage1To2<T>> {

    /**
     * Gets the leftmost query source. Needed for UDF (user data filtering).
     * 
     * @return
     */
    ISource1<? extends ISource2<?>> mainSource();
}
