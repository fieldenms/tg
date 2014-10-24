package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.entity.functional.centre.CritProp;
import ua.com.fielden.platform.entity.functional.centre.ICritProp;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for companion object {@link ICritProp}.
 *
 * @author Developers
 *
 */
@EntityType(CritProp.class)
public class CritPropRao extends CommonEntityRao<CritProp> implements ICritProp {

    @Inject
    public CritPropRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}