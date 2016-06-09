package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.EntityUtils;

/** 
 * DAO implementation for companion object {@link ITgCollectionalSerialisationParent}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCollectionalSerialisationParent.class)
public class TgCollectionalSerialisationParentDao extends CommonEntityDao<TgCollectionalSerialisationParent> implements ITgCollectionalSerialisationParent {
    @Inject
    public TgCollectionalSerialisationParentDao(final IFilter filter) {
        super(filter);
    }

    
    @Override
    public IFetchProvider<TgCollectionalSerialisationParent> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc")
                .with("collProp", EntityUtils.fetchNotInstrumented(TgCollectionalSerialisationChild.class).with("key1", "key2"));
    }
}