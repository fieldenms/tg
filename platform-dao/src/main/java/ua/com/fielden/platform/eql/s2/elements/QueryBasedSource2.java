package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBasedSource2 extends AbstractSource2 {
    private final List<EntQuery2> models;
    private final Map<String, List<Yield2>> yieldsMatrix = new HashMap<String, List<Yield2>>();

    private EntQuery2 firstModel() {
        return models.get(0);
    }

    public QueryBasedSource2(final String alias, final EntQuery2... models) {
        if (models == null || models.length == 0) {
            throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
        }

        this.models = Arrays.asList(models);
        populateYieldMatrixFromQueryModels(models);
        validateYieldsMatrix();
    }

    private void populateYieldMatrixFromQueryModels(final EntQuery2... models) {
        for (final EntQuery2 entQuery : models) {
            for (final Yield2 yield : entQuery.getYields().getYields()) {
                final List<Yield2> foundYields = yieldsMatrix.get(yield.getAlias());
                if (foundYields != null) {
                    foundYields.add(yield);
                } else {
                    final List<Yield2> newList = new ArrayList<Yield2>();
                    newList.add(yield);
                    yieldsMatrix.put(yield.getAlias(), newList);
                }
            }
        }
    }

    //    private boolean getYieldNullability(final String yieldAlias) {
    //	final boolean result = false;
    //	for (final Yield2 yield : yieldsMatrix.get(yieldAlias)) {
    //	    if (false/*yield.getInfo().isNullable()*/) {
    //		return true;
    //	    }
    //	}
    //	return result;
    //    }

    private void validateYieldsMatrix() {
        for (final Map.Entry<String, List<Yield2>> entry : yieldsMatrix.entrySet()) {
            if (entry.getValue().size() != models.size()) {
                throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
            }
        }
    }

    @Override
    public Class sourceType() {
        return firstModel().type();
    }

    public Yields2 getYields() {
        return firstModel().getYields();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((models == null) ? 0 : models.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QueryBasedSource2)) {
            return false;
        }
        final QueryBasedSource2 other = (QueryBasedSource2) obj;
        if (models == null) {
            if (other.models != null) {
                return false;
            }
        } else if (!models.equals(other.models)) {
            return false;
        }
        return true;
    }
}