package ua.com.fielden.platform.eql.s1.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntProp implements ISingleOperand<ua.com.fielden.platform.eql.s2.elements.EntProp> {
    private String name;
    private boolean external;

    public EntProp(final String name, final boolean external) {
        this.name = name;
        this.external = external;
    }

    public EntProp(final String name) {
        this(name, false);
    }

    public ua.com.fielden.platform.eql.s2.elements.EntProp transform() {
	return null;
    }

    @Override
    public List<EntProp> getLocalProps() {
        //return isExpression() ? expression.getLocalProps() : Arrays.asList(new EntProp[]{this});
        return Arrays.asList(new EntProp[]{this});
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        //return isExpression() ? expression.getLocalSubQueries() : Collections.<EntQuery> emptyList();
        return Collections.<EntQuery> emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
        //return isExpression() ? expression.getAllValues() : Collections.<EntValue> emptyList();
        return Collections.<EntValue> emptyList();
    }

    public String getName() {
        return name;
    }

    public boolean isExternal() {
        return external;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(obj instanceof EntProp)) {
            return false;
        }
        final EntProp other = (EntProp) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}