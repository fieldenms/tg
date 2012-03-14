package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Bogie;

import com.google.inject.Inject;

/**
 * Class for retrieval bogies
 *
 * @author TG Team
 */
@EntityType(Bogie.class)
public class BogieDao2 extends CommonEntityDao2<Bogie> implements IBogieDao2 {

    @Inject
    protected BogieDao2(final IFilter filter) {
	super(filter);
    }


}
