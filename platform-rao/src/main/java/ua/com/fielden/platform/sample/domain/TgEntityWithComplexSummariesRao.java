package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgEntityWithComplexSummaries}.
 * 
 * @author Developers
 *
 */
@EntityType(TgEntityWithComplexSummaries.class)
public class TgEntityWithComplexSummariesRao extends CommonEntityRao<TgEntityWithComplexSummaries> implements ITgEntityWithComplexSummaries {

    @Inject
    public TgEntityWithComplexSummariesRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}