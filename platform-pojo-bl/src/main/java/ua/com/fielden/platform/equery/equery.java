package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoin;
import ua.com.fielden.platform.equery.interfaces.IMain.IPlainJoin;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

public class equery {
    public static <T extends AbstractEntity> IPlainJoin select(final Class<T> entityType) {
	return new PlainJoin((new QueryTokens()).from(entityType, null));
    }

    public static <T extends AbstractEntity> IJoin select(final Class<T> entityType, final String alias) {
	return new Join((new QueryTokens()).from(entityType, alias));
    }

    public static <T extends AbstractEntity> IPlainJoin select(final IQueryModel... sourceQueryModels) {
	return new PlainJoin((new QueryTokens()).from(null, sourceQueryModels));
    }

    public static IJoin select(final IQueryModel sourceQueryModel, final String alias) {
	return new Join((new QueryTokens()).from(alias, sourceQueryModel));
    }
}
