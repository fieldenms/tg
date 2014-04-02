package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.utils.Pair;

public class DetailsQueryGenerator<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends GridAnalysisQueryGenerator<T, CDTME> {

    private final List<Pair<String, Object>> conditions;

    public DetailsQueryGenerator(final Class<T> root, final CDTME cdtme, final List<Pair<String, Object>> conditions) {
        super(root, cdtme);
        this.conditions = conditions == null ? new ArrayList<Pair<String, Object>>() : conditions;
    }

    @Override
    public ICompleted<T> createQuery() {
        ICompleted<T> query = super.createQuery();
        for (final Pair<String, Object> entry : conditions) {
            query = where(query).prop(property(StringUtils.isEmpty(entry.getKey()) ? "id" : entry.getKey())).eq().val(entry.getValue());
        }
        return query;
    }
}
