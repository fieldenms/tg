package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgPersonName}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPersonName.class)
public class TgPersonNameRao extends CommonEntityRao<TgPersonName> implements ITgPersonName {

    @Inject
    public TgPersonNameRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}