package ua.com.fielden.platform.entity.functional.paginator;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IPage}.
 * 
 * @author Developers
 *
 */
@EntityType(Page.class)
public class PageRao extends CommonEntityRao<Page> implements IPage {

    @Inject
    public PageRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}