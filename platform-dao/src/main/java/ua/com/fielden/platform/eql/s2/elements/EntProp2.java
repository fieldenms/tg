package ua.com.fielden.platform.eql.s2.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntProp2 implements ISingleOperand2 {
    private final String name;
    private final boolean aliased;
    private final ISource2 source;
    private final Object resolution;
    private final Expression2 expression;


    public EntProp2(final String name, final ISource2 source, final boolean aliased, final Object resolution, final Expression2 expression) {
        this.name = name;
        this.source = source;
        this.aliased = aliased;
        this.resolution = resolution;
        this.expression = expression;
        source.addProp(this);
        System.out.println(toString());
    }

    @Override
    public String toString() {
        return " name = " + name + "; source = " + source + "; aliased = " + aliased + "; resolution = " + resolution;
    }

    @Override
    public List<EntProp2> getLocalProps() {
        //return isExpression() ? expression.getLocalProps() : Arrays.asList(new EntProp[]{this});
        return Arrays.asList(new EntProp2[]{this});
    }

    @Override
    public List<EntQuery2> getLocalSubQueries() {
        //return isExpression() ? expression.getLocalSubQueries() : Collections.<EntQuery> emptyList();
        return Collections.<EntQuery2> emptyList();
    }

    @Override
    public List<EntValue2> getAllValues() {
        //return isExpression() ? expression.getAllValues() : Collections.<EntValue> emptyList();
        return Collections.<EntValue2> emptyList();
    }

    public String getName() {
        return name;
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
        if (!(obj instanceof EntProp2)) {
            return false;
        }
        final EntProp2 other = (EntProp2) obj;
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