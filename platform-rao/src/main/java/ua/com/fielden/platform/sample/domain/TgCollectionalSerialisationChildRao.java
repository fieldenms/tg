package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgCollectionalSerialisationChild}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCollectionalSerialisationChild.class)
public class TgCollectionalSerialisationChildRao extends CommonEntityRao<TgCollectionalSerialisationChild> implements ITgCollectionalSerialisationChild {

    @Inject
    public TgCollectionalSerialisationChildRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}