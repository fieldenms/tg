package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgEntityWithPropertyDependency}.
 * 
 * @author Developers
 *
 */
@EntityType(TgEntityWithPropertyDependency.class)
public class TgEntityWithPropertyDependencyRao extends CommonEntityRao<TgEntityWithPropertyDependency> implements ITgEntityWithPropertyDependency {

    @Inject
    public TgEntityWithPropertyDependencyRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}