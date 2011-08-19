package ua.com.fielden.platform.ui.config.controller;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

/**
 * The main menu controller, which constructs the menu from the Java configuration without accessing menu configuration stored in the could.
 * This implementation is intended purely for use as part of the development framework.
 *
 * @author TG Team
 *
 */
public class LocalMainMenuItemController implements IMainMenuItemController {

    private String username;

    public LocalMainMenuItemController() {

    }

    @Override
    public List<MainMenuItem> loadMenuSkeletonStructure() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setUsername(final String username) {
	this.username = username;
    }

    @Override
    public String getUsername() {
	return username;
    }

    @Override
    public Class<MainMenuItem> getEntityType() {
	return MainMenuItem.class;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
	return String.class;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
	return false;
    }

    @Override
    public MainMenuItem findById(final Long id, final fetch<MainMenuItem> fetchModel) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MainMenuItem findById(final Long id) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MainMenuItem findByKey(final Object... keyValues) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MainMenuItem findByKeyAndFetch(final fetch<MainMenuItem> fetchModel, final Object... keyValues) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> firstPage(final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> firstPage(final IQueryOrderedModel<MainMenuItem> query, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> firstPage(final IQueryOrderedModel<MainMenuItem> query, final fetch<MainMenuItem> fetchModel, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> firstPage(final IQueryOrderedModel<MainMenuItem> model, final IQueryOrderedModel<EntityAggregates> summaryModel, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> firstPage(final IQueryOrderedModel<MainMenuItem> model, final fetch<MainMenuItem> fetchModel, final IQueryOrderedModel<EntityAggregates> summaryModel, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> getPage(final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> getPage(final IQueryOrderedModel<MainMenuItem> query, final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> getPage(final IQueryOrderedModel<MainMenuItem> query, final fetch<MainMenuItem> fetchModel, final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> getPage(final IQueryOrderedModel<MainMenuItem> query, final int pageNo, final int pageCount, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<MainMenuItem> getPage(final IQueryOrderedModel<MainMenuItem> query, final fetch<MainMenuItem> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MainMenuItem save(final MainMenuItem entity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void delete(final MainMenuItem entity) {
	// TODO Auto-generated method stub

    }

    @Override
    public void delete(final IQueryOrderedModel<MainMenuItem> model) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean entityExists(final MainMenuItem entity) {
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
    public MainMenuItem getEntity(final IQueryOrderedModel<MainMenuItem> model) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MainMenuItem getEntity(final IQueryOrderedModel<MainMenuItem> model, final fetch<MainMenuItem> fetchModel) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int count(final IQueryOrderedModel<MainMenuItem> model) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<MainMenuItem> getEntities(final IQueryOrderedModel<MainMenuItem> query) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<MainMenuItem> getEntities(final IQueryOrderedModel<MainMenuItem> query, final fetch<MainMenuItem> fetchModel) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public byte[] export(final IQueryOrderedModel<MainMenuItem> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public byte[] export(final IQueryOrderedModel<MainMenuItem> query, final fetch<MainMenuItem> fetchModel, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

}
