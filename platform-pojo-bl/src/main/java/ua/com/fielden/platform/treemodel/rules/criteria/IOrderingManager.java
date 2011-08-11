package ua.com.fielden.platform.treemodel.rules.criteria;

import java.util.List;

import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

/**
 * This interface defines how domain tree "tick" can be managed for base "add to result" functionality. <br><br>
 *
 * The major aspects of tree management (context-specific) are following: <br>
 * 1. property's ordering;<br>
 *
 * @author TG Team
 *
 */
public interface IOrderingManager {
    /**
     * Returns a list of <b>ordered</b> properties (columns) for concrete <code>root</code> type.
     *
     * @param root -- a root type that contains an <b>ordered</b> properties.
     * @return
     */
    List<Pair<String, Ordering>> orderedProperties(final Class<?> root);

    /**
     * Toggles an <i>ordering</i> of a result property by following convention: [... => ASC => DESC => unordered => ASC => ...]<br><br>
     *
     * This action should not conflict with "checked properties" (or with "used properties" -- more accurately) contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param ordering -- an ordering to set
     * @return -- a result tick representation
     */
    void toggleOrdering(final Class<?> root, final String property);
}
