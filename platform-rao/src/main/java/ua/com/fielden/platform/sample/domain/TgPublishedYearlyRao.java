package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgPublishedYearly}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPublishedYearly.class)
public class TgPublishedYearlyRao extends CommonEntityRao<TgPublishedYearly> implements ITgPublishedYearly {

    @Inject
    public TgPublishedYearlyRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}