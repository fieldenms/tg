package ua.com.fielden.platform.snappy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnappyQuery {
    private final String mainQueryString;
    private final String secondaryQueryString;
    private final List<String> aggrAccessors = new ArrayList<String>();

    public SnappyQuery(final String mainQueryString, final String secondaryQueryString, final List<String> aggrAccessors) {
	this.mainQueryString = mainQueryString;
	this.secondaryQueryString = secondaryQueryString;
	this.aggrAccessors.addAll(aggrAccessors);
    }

    public String getMainQueryString() {
	return mainQueryString;
    }

    public String getSecondaryQueryString() {
	return secondaryQueryString;
    }

    public boolean isFilteringQuery() {
	return secondaryQueryString == null;
    }

    public List<String> getAggrAccessors() {
	return Collections.unmodifiableList(aggrAccessors);
    }

}
