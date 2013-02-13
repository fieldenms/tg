package ua.com.fielden.platform.criteria.generator.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
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
    public boolean isStale(Long entityId, Long version) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public SecondLevelEntity findById(Long id, fetch<SecondLevelEntity> fetchModel) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public SecondLevelEntity findById(Long id) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public SecondLevelEntity findByKey(Object... keyValues) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public SecondLevelEntity findByKeyAndFetch(fetch<SecondLevelEntity> fetchModel, Object... keyValues) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<SecondLevelEntity> firstPage(int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<SecondLevelEntity> getPage(int pageNo, int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<SecondLevelEntity> firstPage(QueryExecutionModel<SecondLevelEntity, ?> query, int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<SecondLevelEntity> firstPage(QueryExecutionModel<SecondLevelEntity, ?> model, QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> summaryModel, int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<SecondLevelEntity> getPage(QueryExecutionModel<SecondLevelEntity, ?> query, int pageNo, int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<SecondLevelEntity> getPage(QueryExecutionModel<SecondLevelEntity, ?> query, int pageNo, int pageCount, int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public SecondLevelEntity save(SecondLevelEntity entity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void delete(SecondLevelEntity entity) {
	// TODO Auto-generated method stub

    }

    @Override
    public void delete(EntityResultQueryModel<SecondLevelEntity> model, Map<String, Object> paramValues) {
	// TODO Auto-generated method stub

    }

    @Override
    public void delete(EntityResultQueryModel<SecondLevelEntity> model) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean entityExists(SecondLevelEntity entity) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean entityExists(Long id) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean entityWithKeyExists(Object... keyValues) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public SecondLevelEntity getEntity(QueryExecutionModel<SecondLevelEntity, ?> model) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int count(EntityResultQueryModel<SecondLevelEntity> model, Map<String, Object> paramValues) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int count(EntityResultQueryModel<SecondLevelEntity> model) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<SecondLevelEntity> getAllEntities(QueryExecutionModel<SecondLevelEntity, ?> query) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public byte[] export(QueryExecutionModel<SecondLevelEntity, ?> query, String[] propertyNames, String[] propertyTitles) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<SecondLevelEntity> getFirstEntities(QueryExecutionModel<SecondLevelEntity, ?> query, int numberOfEntities) {
	// TODO Auto-generated method stub
	return null;
    }

}
