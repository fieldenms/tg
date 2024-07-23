package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.HibernateScalar;
import ua.com.fielden.platform.eql.retrieval.records.QueryResultLeaf;
import ua.com.fielden.platform.eql.retrieval.records.ValueTree;

/**
 * Utility class for getting ordered list of {@link HibernateScalar Hibernate scalars} from {@link EntityTree}.
 *
 * @author TG Team
 *
 */
public class HibernateScalarsExtractor {

    private HibernateScalarsExtractor() {}

    public static <E extends AbstractEntity<?>> List<HibernateScalar> getSortedScalars(final EntityTree<E> entityTree) {
        final SortedMap<Integer, HibernateScalar> result = new TreeMap<>();
        for (final QueryResultLeaf leaf : getLeavesFromEntityTree(entityTree)) {
            result.put(leaf.position(), leaf.hibScalar());
        }
        return unmodifiableList(new ArrayList<HibernateScalar>(result.values()));
    }

    private static <E extends AbstractEntity<?>> List<QueryResultLeaf> getLeavesFromEntityTree(final EntityTree<E> entityTree) {
        final List<QueryResultLeaf> result = new ArrayList<>();

        result.addAll(entityTree.leaves());

        for (final ValueTree composite : entityTree.valueTrees().values()) {
            result.addAll(composite.leaves());
        }

        for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> composite : entityTree.entityTrees().entrySet()) {
            result.addAll(getLeavesFromEntityTree(composite.getValue()));
        }

        return result;
    }
}