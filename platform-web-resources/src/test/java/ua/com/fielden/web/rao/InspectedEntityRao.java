package ua.com.fielden.web.rao;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;

@EntityType(InspectedEntity.class)
public class InspectedEntityRao extends CommonEntityRao<InspectedEntity> implements IInspectedEntityDao {
    public InspectedEntityRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
