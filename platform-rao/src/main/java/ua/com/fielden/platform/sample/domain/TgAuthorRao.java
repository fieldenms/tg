package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgAuthor}.
 * 
 * @author Developers
 *
 */
@EntityType(TgAuthor.class)
public class TgAuthorRao extends CommonEntityRao<TgAuthor> implements ITgAuthor {

    @Inject
    public TgAuthorRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}