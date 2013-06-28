package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;

public class SimpleUserFilter implements IFilter {

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
	final String prefix = typeAlias != null ? (typeAlias + ".") : "";
	//return (entityType.equals(TgAuthor.class) ? cond().prop(prefix + "key").notLike().val("A%").model() : null);
	return (entityType.equals(TgAuthor.class) ? cond().prop(prefix + "key").isNotNull().model() : null);
    }
}
