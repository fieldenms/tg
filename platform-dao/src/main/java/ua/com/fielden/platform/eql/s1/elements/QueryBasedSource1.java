package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.QueryBasedSource2;

public class QueryBasedSource1 extends AbstractSource1<QueryBasedSource2> {
    private final List<EntQuery1> models;
    private final Map<String, List<Yield1>> yieldsMatrix = new HashMap<String, List<Yield1>>();

    private EntQuery1 firstModel() {
	return models.get(0);
    }

    public QueryBasedSource1(final String alias, final DomainMetadataAnalyser domainMetadataAnalyser, final EntQuery1... models) {
	super(alias, domainMetadataAnalyser);
	if (models == null || models.length == 0) {
	    throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
	}

	this.models = Arrays.asList(models);
	populateYieldMatrixFromQueryModels(models);
	validateYieldsMatrix();
    }

    @Override
    public QueryBasedSource2 transform(final TransformatorToS2 resolver) {
	return (QueryBasedSource2) resolver.getTransformedSource(this);
//	final List<ua.com.fielden.platform.eql.s2.elements.EntQuery> transformed = new ArrayList<>();
//	for (final EntQuery entQuery : models) {
//	    transformed.add(entQuery.transform(resolver));
//	}
//	return new ua.com.fielden.platform.eql.s2.elements.QueryBasedSource(alias, getDomainMetadataAnalyser(), transformed.toArray(new ua.com.fielden.platform.eql.s2.elements.EntQuery[]{}));
    }

    private void populateYieldMatrixFromQueryModels(final EntQuery1... models) {
	for (final EntQuery1 entQuery : models) {
	    for (final Yield1 yield : entQuery.getYields().getYields()) {
		final List<Yield1> foundYields = yieldsMatrix.get(yield.getAlias());
		if (foundYields != null) {
		    foundYields.add(yield);
		} else {
		    final List<Yield1> newList = new ArrayList<Yield1>();
		    newList.add(yield);
		    yieldsMatrix.put(yield.getAlias(), newList);
		}
	    }
	}
    }

    private boolean getYieldNullability(final String yieldAlias) {
	final boolean result = false;
	for (final Yield1 yield : yieldsMatrix.get(yieldAlias)) {
//	    if (yield.getInfo().isNullable()) {
		return true;
//	    }
	}
	return result;
    }

    private void validateYieldsMatrix() {
	for (final Map.Entry<String, List<Yield1>> entry : yieldsMatrix.entrySet()) {
	    if (entry.getValue().size() != models.size()) {
		throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
	    }
	}
    }

    @Override
    public Class sourceType() {
	return firstModel().type();
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
	if (!(obj instanceof QueryBasedSource1)) {
	    return false;
	}
	final QueryBasedSource1 other = (QueryBasedSource1) obj;
	if (models == null) {
	    if (other.models != null) {
		return false;
	    }
	} else if (!models.equals(other.models)) {
	    return false;
	}
	return true;
    }

    public List<EntQuery1> getModels() {
        return models;
    }
}