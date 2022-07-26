package ua.com.fielden.platform.ui.entity.centre;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigCo;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigDao;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.config.MainMenuItemDao;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 * 
 * @author TG Team
 * 
 */
public class EntityCentreAnalysisConfigPersistenceTest extends AbstractDaoTestCase {
    private final EntityCentreConfigCo daoECC = getInstance(EntityCentreConfigDao.class);
    private final EntityCentreAnalysisConfigCo dao = getInstance(EntityCentreAnalysisConfigDao.class);
    private final MainMenuItemCo menuDao = getInstance(MainMenuItemDao.class);
    private final IUser userDao = getInstance(UserDao.class);

    @Test
    public void test_insertion_and_retrieval_of_data() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 1, 2, 3 });
        config.setDesc("desc");
        daoECC.saveWithRetry(config); // no conflicts should appear -- initial saving
        final EntityCentreConfig config2 = daoECC.findByEntityAndFetch(null, config);

        final EntityCentreAnalysisConfig analysis = new_composite(EntityCentreAnalysisConfig.class, config2, "ANALYSIS 1");
        dao.save(analysis);

        final EntityResultQueryModel<EntityCentreAnalysisConfig> query = select(EntityCentreAnalysisConfig.class).model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.ID).asc().model();
        final List<EntityCentreAnalysisConfig> result = dao.getPage(from(query).with(orderBy).model(), 0, 25).data();
        assertEquals("Incorrect number of retrieved configurations.", 1, result.size());
        assertTrue("Incorrectly saved binary property.", EntityUtils.equalsEx("ANALYSIS 1", result.get(0).getTitle()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(User.class, "USER", "DESC").setBase(true).setEmail("USER@unit-test.software").setActive(true));
        save(new_(MainMenuItem.class, "type", "desc").setOrder(1));
    }
}