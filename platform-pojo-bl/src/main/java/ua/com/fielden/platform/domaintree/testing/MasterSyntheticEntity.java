package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.equery.IParameterGetter;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(String.class)
public class MasterSyntheticEntity extends AbstractEntity<String>  implements IQueryModelProvider<MasterSyntheticEntity> {
    private static final long serialVersionUID = 1L;

    protected MasterSyntheticEntity() {
    }

    @Override
    public IQueryModel<MasterSyntheticEntity> model(final IParameterGetter parameterGetter) {
	return null;
    }
}