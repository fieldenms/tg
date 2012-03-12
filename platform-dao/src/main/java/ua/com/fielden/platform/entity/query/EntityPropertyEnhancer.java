package ua.com.fielden.platform.entity.query;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;

public class EntityPropertyEnhancer<E extends AbstractEntity<?>> {
    private final static String ID_PROPERTY_NAME = "id";
    private Session session;
    private EntityFactory entityFactory;
    private Logger logger = Logger.getLogger(this.getClass());
    private MappingsGenerator mappingsGenerator;
    private DbVersion dbVersion;
    private final IFilter filter;
    private final String username;

    protected EntityPropertyEnhancer(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	this.session = session;
	this.entityFactory = entityFactory;
	this.mappingsGenerator = mappingsGenerator;
	this.dbVersion = dbVersion;
	this.filter = filter;
	this.username = username;
    }

}
