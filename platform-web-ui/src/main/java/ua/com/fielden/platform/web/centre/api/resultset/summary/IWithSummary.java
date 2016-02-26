package ua.com.fielden.platform.web.centre.api.resultset.summary;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder5WithPropAction;

/**
 * A contract for declaring summary expression as part of Entity Centres.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWithSummary<T extends AbstractEntity<?>> extends  IResultSetBuilder5WithPropAction<T> {
    /**
     * Adds a summary expression to be computed and displayed underneath a corresponding property column in the grid EGI representation.
     * The provided <code>alias</code> can be used to refer to the summary property to specify card layouts.
     *
     * @param alias -- a proper Java field name that is used during the generation of summary property.
     * @param expression -- a valid Entity Expression Language summary expression.
     * @param titleAndDesc -- title and description that are used during summary expression visualisation, where title is separated from description by a colon.
     * @return
     */
    IWithSummary<T> withSummary(final String alias, final String expression, final String titleAndDesc);
}
