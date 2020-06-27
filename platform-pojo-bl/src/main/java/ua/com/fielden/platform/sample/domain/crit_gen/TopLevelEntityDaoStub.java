package ua.com.fielden.platform.sample.domain.crit_gen;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.User;

@EntityType(TopLevelEntity.class)
public class TopLevelEntityDaoStub implements ITopLevelEntity {

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public Class<TopLevelEntity> getEntityType() {
        return TopLevelEntity.class;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        return false;
    }

    @Override
    public TopLevelEntity findById(final boolean filtered, final Long id, final fetch<TopLevelEntity> fetchModel) {
        return null;
    }

    @Override
    public TopLevelEntity findById(final Long id) {
        return null;
    }

    @Override
    public TopLevelEntity findByKey(final Object... keyValues) {
        return null;
    }

    @Override
    public TopLevelEntity findByKeyAndFetch(final boolean filtered, final fetch<TopLevelEntity> fetchModel, final Object... keyValues) {
        return null;
    }

    @Override
    public IPage<TopLevelEntity> firstPage(final QueryExecutionModel<TopLevelEntity, ?> query, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TopLevelEntity> getPage(final QueryExecutionModel<TopLevelEntity, ?> query, final int pageNo, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TopLevelEntity> getPage(final QueryExecutionModel<TopLevelEntity, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        return null;
    }

    @Override
    public TopLevelEntity save(final TopLevelEntity entity) {
        return null;
    }

    @Override
    public boolean entityExists(final TopLevelEntity entity) {
        return false;
    }

    @Override
    public boolean entityExists(final Long id) {
        return false;
    }

    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        return false;
    }

    @Override
    public TopLevelEntity getEntity(final QueryExecutionModel<TopLevelEntity, ?> model) {
        return null;
    }

    @Override
    public int count(final EntityResultQueryModel<TopLevelEntity> model, final Map<String, Object> paramValues) {
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<TopLevelEntity> model) {
        return 0;
    }

    @Override
    public List<TopLevelEntity> getAllEntities(final QueryExecutionModel<TopLevelEntity, ?> query) {
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<TopLevelEntity, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        return null;
    }

    @Override
    public List<TopLevelEntity> getFirstEntities(final QueryExecutionModel<TopLevelEntity, ?> query, final int numberOfEntities) {
        return null;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Optional<Integer> progress() {
        return Optional.empty();
    }

    @Override
    public TopLevelEntity findByEntityAndFetch(final fetch<TopLevelEntity> fetchModel, final TopLevelEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IFetchProvider<TopLevelEntity> getFetchProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopLevelEntity new_() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TopLevelEntity> stream(final QueryExecutionModel<TopLevelEntity, ?> qem, final int fetchSize) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TopLevelEntity> stream(final QueryExecutionModel<TopLevelEntity, ?> qem) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public boolean exists(final EntityResultQueryModel<TopLevelEntity> model, final Map<String, Object> paramValues) {
		// TODO Auto-generated method stub
		return false;
	}

}