package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

public class NullTest extends AbstractCondition {
    private final ISingleOperand operand;
    private final boolean negated;
    private final DomainMetadataAnalyser domainMetadataAnalyser;

    @Override
    public String sql() {
        if (operand instanceof EntProp) {
            final EntProp prop = (EntProp) operand;
            if (EntityUtils.isUnionEntityType(prop.getPropType())) {
                System.out.println(prop.getSource().sourceType() + " --- " + prop.getName());
                final String propName = prop.getSource().getAlias() != null && prop.getName().startsWith(prop.getSource().getAlias() + ".") ? EntityUtils.splitPropByFirstDot(prop.getName()).getValue()
                        : prop.getName();
                final PropertyMetadata ppi = domainMetadataAnalyser.getInfoForDotNotatedProp(prop.getSource().sourceType(), propName);

                if (ppi.isUnionEntity()) {
                    final Set<String> columns = new HashSet<>();
                    for (final PropertyMetadata pmd : ppi.getComponentTypeSubprops()) {
                        columns.add(pmd.getColumn().getName());
                    }

                    return getUnionPropSql(columns, negated);
                }
            }
        }

        return operand.sql() + (negated ? " IS NOT NULL" : " IS NULL");
    }

    private String getUnionPropSql(final Set<String> props, final boolean negation) {
        final StringBuffer sb = new StringBuffer();
        final String nullString = negation ? " IS NOT NULL" : " IS NULL";
        final String logicalString = negation ? " OR " : " AND ";
        sb.append("(");
        for (final Iterator<String> iterator = props.iterator(); iterator.hasNext();) {
            sb.append(iterator.next());
            sb.append(nullString);
            if (iterator.hasNext()) {
                sb.append(logicalString);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public NullTest(final ISingleOperand operand, final boolean negated, final DomainMetadataAnalyser domainMetadataAnalyser) {
        this.operand = operand;
        this.negated = negated;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
    }

    @Override
    public boolean ignore() {
        return operand.ignore();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
        if (!(obj instanceof NullTest)) {
            return false;
        }
        final NullTest other = (NullTest) obj;
        if (negated != other.negated) {
            return false;
        }
        if (operand == null) {
            if (other.operand != null) {
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            return false;
        }
        return true;
    }

    @Override
    protected List<IPropertyCollector> getCollection() {
        return new ArrayList<IPropertyCollector>() {
            {
                add(operand);
            }
        };
    }
}