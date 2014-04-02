package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/**
 * RAO implementation for companion object {@link ITgAuthorship}.
 * 
 * @author Developers
 * 
 */
@EntityType(TgAuthorship.class)
public class TgAuthorshipRao extends CommonEntityRao<TgAuthorship> implements ITgAuthorship {

    @Inject
    public TgAuthorshipRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}