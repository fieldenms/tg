package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.entity.functional.centre.FetchProp;
import ua.com.fielden.platform.entity.functional.centre.IFetchProp;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IFetchProp}.
 * 
 * @author Developers
 *
 */
@EntityType(FetchProp.class)
public class FetchPropRao extends CommonEntityRao<FetchProp> implements IFetchProp {

    @Inject
    public FetchPropRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}