package ua.com.fielden.platform.criteria.generator.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.fetch.IEntityFetchStrategy;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

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
    public Class<? extends Comparable> getKeyType() {
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        return false;
    }

    @Override
    public TopLevelEntity findById(final Long id, final fetch<TopLevelEntity> fetchModel) {
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
    public TopLevelEntity findByKeyAndFetch(final fetch<TopLevelEntity> fetchModel, final Object... keyValues) {
        return null;
    }

    @Override
    public IPage<TopLevelEntity> firstPage(final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TopLevelEntity> getPage(final int pageNo, final int pageCapacity) {
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
    public void delete(final TopLevelEntity entity) {
    }

    @Override
    public void delete(final EntityResultQueryModel<TopLevelEntity> model, final Map<String, Object> paramValues) {
    }

    @Override
    public void delete(final EntityResultQueryModel<TopLevelEntity> model) {
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
    public Integer progress() {
        return null;
    }

    @Override
    public TopLevelEntity findByEntityAndFetch(final fetch<TopLevelEntity> fetchModel, final TopLevelEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEntityFetchStrategy<TopLevelEntity> getFetchStrategy() {
        // TODO Auto-generated method stub
        return null;
    }
}
