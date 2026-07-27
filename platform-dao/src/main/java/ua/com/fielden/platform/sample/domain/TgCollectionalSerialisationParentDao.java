package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

import java.util.Optional;

import static java.util.Optional.of;

/** 
 * DAO implementation for companion object {@link ITgCollectionalSerialisationParent}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCollectionalSerialisationParent.class)
public class TgCollectionalSerialisationParentDao extends CommonEntityDao<TgCollectionalSerialisationParent> implements ITgCollectionalSerialisationParent {

    @Override
    public IFetchProvider<TgCollectionalSerialisationParent> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc")
                .with("collProp");
    }

    @SessionRequired
    @Override
    public TgCollectionalSerialisationParent save(final TgCollectionalSerialisationParent entity) {
        return save(entity, of(getFetchProvider().fetchModel())).asRight().value();
    }

}