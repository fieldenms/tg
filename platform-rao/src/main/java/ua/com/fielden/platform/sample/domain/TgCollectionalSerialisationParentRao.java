package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgCollectionalSerialisationParent}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCollectionalSerialisationParent.class)
public class TgCollectionalSerialisationParentRao extends CommonEntityRao<TgCollectionalSerialisationParent> implements ITgCollectionalSerialisationParent {

    @Inject
    public TgCollectionalSerialisationParentRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}