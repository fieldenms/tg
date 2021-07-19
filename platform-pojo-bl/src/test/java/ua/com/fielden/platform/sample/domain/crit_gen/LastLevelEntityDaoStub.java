package ua.com.fielden.platform.sample.domain.crit_gen;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.User;

public class LastLevelEntityDaoStub implements ILastLevelEntity {

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User getUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<LastLevelEntity> getEntityType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public LastLevelEntity findById(final boolean filtered, final Long id, final fetch<LastLevelEntity> fetchModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LastLevelEntity findById(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LastLevelEntity findByKey(final Object... keyValues) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LastLevelEntity findByKeyAndFetch(final boolean filtered, final fetch<LastLevelEntity> fetchModel, final Object... keyValues) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<LastLevelEntity> firstPage(final QueryExecutionModel<LastLevelEntity, ?> query, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<LastLevelEntity> getPage(final QueryExecutionModel<LastLevelEntity, ?> query, final int pageNo, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<LastLevelEntity> getPage(final QueryExecutionModel<LastLevelEntity, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LastLevelEntity save(final LastLevelEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean entityExists(final LastLevelEntity entity) {
        return "EntityKey".equals(entity.getKey());
    }

    @Override
    public boolean entityExists(final Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        for (final Object keyValue : keyValues) {
            if (!"EntityKey".equals(keyValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public LastLevelEntity getEntity(final QueryExecutionModel<LastLevelEntity, ?> model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int count(final EntityResultQueryModel<LastLevelEntity> model, final Map<String, Object> paramValues) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<LastLevelEntity> model) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<LastLevelEntity> getAllEntities(final QueryExecutionModel<LastLevelEntity, ?> query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<LastLevelEntity, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<LastLevelEntity> getFirstEntities(final QueryExecutionModel<LastLevelEntity, ?> query, final int numberOfEntities) {
        // TODO Auto-generated method stub
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
    public LastLevelEntity findByEntityAndFetch(final fetch<LastLevelEntity> fetchModel, final LastLevelEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IFetchProvider<LastLevelEntity> getFetchProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LastLevelEntity new_() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<LastLevelEntity> stream(final QueryExecutionModel<LastLevelEntity, ?> qem, final int fetchSize) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<LastLevelEntity> stream(final QueryExecutionModel<LastLevelEntity, ?> qem) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public boolean exists(final EntityResultQueryModel<LastLevelEntity> model, final Map<String, Object> paramValues) {
		// TODO Auto-generated method stub
		return false;
	}

}