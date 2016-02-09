package ua.com.fielden.platform.criteria.generator.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.User;

public class SecondLevelEntityDaoStub implements ISecondLevelEntity {

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
    public Class<SecondLevelEntity> getEntityType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SecondLevelEntity findById(final Long id, final fetch<SecondLevelEntity> fetchModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SecondLevelEntity findById(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SecondLevelEntity findByKey(final Object... keyValues) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SecondLevelEntity findByKeyAndFetch(final fetch<SecondLevelEntity> fetchModel, final Object... keyValues) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<SecondLevelEntity> firstPage(final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<SecondLevelEntity> getPage(final int pageNo, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<SecondLevelEntity> firstPage(final QueryExecutionModel<SecondLevelEntity, ?> query, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<SecondLevelEntity> getPage(final QueryExecutionModel<SecondLevelEntity, ?> query, final int pageNo, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<SecondLevelEntity> getPage(final QueryExecutionModel<SecondLevelEntity, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SecondLevelEntity save(final SecondLevelEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean entityExists(final SecondLevelEntity entity) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean entityExists(final Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SecondLevelEntity getEntity(final QueryExecutionModel<SecondLevelEntity, ?> model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int count(final EntityResultQueryModel<SecondLevelEntity> model, final Map<String, Object> paramValues) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<SecondLevelEntity> model) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<SecondLevelEntity> getAllEntities(final QueryExecutionModel<SecondLevelEntity, ?> query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<SecondLevelEntity, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SecondLevelEntity> getFirstEntities(final QueryExecutionModel<SecondLevelEntity, ?> query, final int numberOfEntities) {
        // TODO Auto-generated method stub
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
    public SecondLevelEntity findByEntityAndFetch(final fetch<SecondLevelEntity> fetchModel, final SecondLevelEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IFetchProvider<SecondLevelEntity> getFetchProvider() {
        // TODO Auto-generated method stub
        return null;
    }
}