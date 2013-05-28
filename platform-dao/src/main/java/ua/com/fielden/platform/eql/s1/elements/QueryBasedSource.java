package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;

public class QueryBasedSource extends AbstractSource<ua.com.fielden.platform.eql.s2.elements.QueryBasedSource> {
    private final List<EntQuery> models;
    private final Map<String, List<Yield>> yieldsMatrix = new HashMap<String, List<Yield>>();

    private EntQuery firstModel() {
	return models.get(0);
    }

    public QueryBasedSource(final String alias, final DomainMetadataAnalyser domainMetadataAnalyser, final EntQuery... models) {
	super(alias, domainMetadataAnalyser, checkWhetherResultTypeIsPersisted(models));
	this.models = Arrays.asList(models);
	populateYieldMatrixFromQueryModels(models);
	validateYieldsMatrix();
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.QueryBasedSource transform(final TransformatorToS2 resolver) {
	final List<ua.com.fielden.platform.eql.s2.elements.EntQuery> transformed = new ArrayList<>();
	for (final EntQuery entQuery : models) {
	    transformed.add(entQuery.transform(resolver));
	}
	return new ua.com.fielden.platform.eql.s2.elements.QueryBasedSource(alias, getDomainMetadataAnalyser(), transformed.toArray(new ua.com.fielden.platform.eql.s2.elements.EntQuery[]{}));
    }

    private static boolean checkWhetherResultTypeIsPersisted(final EntQuery... models) {
	if (models == null || models.length == 0) {
	    throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
	}
	return models[0].isPersistedType();
    }

    private void populateYieldMatrixFromQueryModels(final EntQuery... models) {
	for (final EntQuery entQuery : models) {
	    for (final Yield yield : entQuery.getYields().getYields()) {
		final List<Yield> foundYields = yieldsMatrix.get(yield.getAlias());
		if (foundYields != null) {
		    foundYields.add(yield);
		} else {
		    final List<Yield> newList = new ArrayList<Yield>();
		    newList.add(yield);
		    yieldsMatrix.put(yield.getAlias(), newList);
		}
	    }
	}
    }

    private boolean getYieldNullability(final String yieldAlias) {
	final boolean result = false;
	for (final Yield yield : yieldsMatrix.get(yieldAlias)) {
//	    if (yield.getInfo().isNullable()) {
		return true;
//	    }
	}
	return result;
    }

    private void validateYieldsMatrix() {
	for (final Map.Entry<String, List<Yield>> entry : yieldsMatrix.entrySet()) {
	    if (entry.getValue().size() != models.size()) {
		throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
	    }
	}
    }

    @Override
    public Class sourceType() {
	return null;//firstModel().type();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((models == null) ? 0 : models.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (!(obj instanceof QueryBasedSource)) {
	    return false;
	}
	final QueryBasedSource other = (QueryBasedSource) obj;
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