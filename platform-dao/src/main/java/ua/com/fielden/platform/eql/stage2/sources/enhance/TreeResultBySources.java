package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.HelperNodeForImplicitJoins;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;

/**
 * Structure that represents all necessary parts for transformation into stage 3.
 * This data structure is populated by {@link PathsToTreeTransformer}, which is effectively 2.5 stage transformation.
 *
 * @param helperNodesMap -- a map between source IDs and a corresponding list of helping nodes for implicit joins.
 *
 * @param calcPropsResolutions -- a map between source IDs and associations between calculated {@code Prop2.propPath} and their expressions, transformed to stage 2.
 *
 * @param plainPropsResolutions -- a map between source IDs and associations between persistent {@code Prop2.propPath} and the data needed for the respective stage 3 data structure ({@link Prop3}.
 *
 *
 * @author TG Team
 */
public record TreeResultBySources (
        Map<Integer, List<HelperNodeForImplicitJoins>> helperNodesMap,
        Map<Integer, Map<String, Expression2>> calcPropsResolutions,
        Map<Integer, Map<String, DataForProp3>> plainPropsResolutions) {
}