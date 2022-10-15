package ua.com.fielden.platform.ui.entity.centre;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.config.MainMenuItemDao;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigPersistenceTest extends AbstractDaoTestCase {
    private final EntityCentreConfigCo dao = getInstance(EntityCentreConfigDao.class);
    private final MainMenuItemCo menuDao = getInstance(MainMenuItemDao.class);
    private final IUser userDao = getInstance(UserDao.class);
    
    @Test
    public void test_insertion_and_retrieval_of_binary_data() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setDesc("desc");
        config.setConfigBody(new byte[] { 1, 2, 3 });
        dao.saveWithRetry(config); // no conflicts should appear -- initial saving
        
        final EntityResultQueryModel<EntityCentreConfig> query = select(EntityCentreConfig.class).model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.ID).asc().model();
        final List<EntityCentreConfig> result = dao.getPage(from(query).with(orderBy).model(), 0, 25).data();
        assertEquals("Incorrect number of retrieved configurations.", 1, result.size());
        assertTrue("Incorrectly saved binary property.", Arrays.equals(new byte[] { 1, 2, 3 }, result.get(0).getConfigBody()));
    }
    
    @Test
    public void test_update_of_binary_data() {
        EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 1, 2, 3 });
        config.setDesc("desc");
        config = saveEntityCentre(config); // no conflicts should appear -- initial saving
        
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        config.setConfigBody(new byte[] { 1, 2, 3, 4 });
        config = saveEntityCentre(config); // no conflicts should appear
        assertEquals("Incorrect version.", Long.valueOf("1"), config.getVersion());
        
        final EntityCentreConfig fromDb = dao.findById(config.getId());
        
        assertNotNull("Configuration entity should exist.", fromDb);
        assertTrue("Incorrectly updated binary property.", Arrays.equals(new byte[] { 1, 2, 3, 4 }, fromDb.getConfigBody()));
    }
    
    // ========================================== CONFLICTING CHANGES RESOLUTION ==========================================
    // The following tests are based on the situation of the same 1) user 2) config name 3) menu item. Other conflicts will not be automatically resolved due to composite-key nature of 'owner', 'title' and 'menuItem' properties
    
    private EntityCentreConfig saveEntityCentre(final EntityCentreConfig config) {
        dao.saveWithRetry(config);
        return dao.findByEntityAndFetch(null, config);
    }
    
    @Test
    public void conflicting_changes_in_EntityCentreConfig_configBody_override_without_exceptions() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithRetry(config); // no conflicts should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------| first
        //       |-------------------------| second
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setConfigBody(new byte[] { 1 });
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setConfigBody(new byte[] { 2 });
        
        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveEntityCentre(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), firstlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 1 }, firstlyRetrievedAndSaved.getConfigBody()));
        
        // after that save secondlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig secondlyRetrievedAndSaved = saveEntityCentre(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), secondlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 2 }, secondlyRetrievedAndSaved.getConfigBody()));
    }
    
    @Test
    public void conflicting_changes_in_EntityCentreConfig_desc_override_without_exceptions() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithRetry(config); // no conflicts should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------| first
        //       |-------------------------| second
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setDesc("desc1");
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setDesc("desc2");
        
        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveEntityCentre(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), firstlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc1", firstlyRetrievedAndSaved.getDesc());
        
        // after that save secondlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig secondlyRetrievedAndSaved = saveEntityCentre(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), secondlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc2", secondlyRetrievedAndSaved.getDesc());
    }
    
    @Test
    public void conflicting_changes_in_EntityCentreConfig_desc_override_without_exceptions_when_saving_order_switched() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithRetry(config); // no conflicts should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------------------| first
        //       |----------------| second
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setDesc("desc1");
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setDesc("desc2");
        
        // save secondlyRetrieved
        final EntityCentreConfig secondlyRetrievedAndSaved = saveEntityCentre(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), secondlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc2", secondlyRetrievedAndSaved.getDesc());
        
        // after that, save firstlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig firstlyRetrievedAndSaved = saveEntityCentre(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), firstlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc1", firstlyRetrievedAndSaved.getDesc());
    }
    
    @Test
    public void multiple_conflicting_changes_in_EntityCentreConfig_configBody_override_without_exceptions() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithRetry(config); // no conflicts should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------| first
        //       |-------------------------| second
        //                      |-----| third
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setConfigBody(new byte[] { 1 });
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setConfigBody(new byte[] { 2 });
        
        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveEntityCentre(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), firstlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 1 }, firstlyRetrievedAndSaved.getConfigBody()));
        
        final EntityCentreConfig thirdlyRetrieved = dao.findByEntityAndFetch(null, config);
        thirdlyRetrieved.setConfigBody(new byte[] { 3 });
        // save thirdlyRetrieved
        final EntityCentreConfig thirdlyRetrievedAndSaved = saveEntityCentre(thirdlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), thirdlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 3 }, thirdlyRetrievedAndSaved.getConfigBody()));
        
        // after that, save secondlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig secondlyRetrievedAndSaved = saveEntityCentre(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("3"), secondlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 2 }, secondlyRetrievedAndSaved.getConfigBody()));
    }

    @Test
    @SessionRequired
    public void saving_conflicting_changes_in_EntityCentreConfig_within_outer_scope_does_not_re_attempt_saving() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithRetry(config); // no conflicts should appear -- initial saving

        // |----------------| first
        //       |-------------------------| second

        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        assertEquals(Long.valueOf(0), config.getVersion());
        assertTrue(Arrays.equals(new byte[] { 0 }, firstlyRetrieved.getConfigBody()));
        firstlyRetrieved.setConfigBody(new byte[] { 1 });

        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setConfigBody(new byte[] { 2 });

        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveEntityCentre(firstlyRetrieved);
        assertEquals(Long.valueOf(1), firstlyRetrievedAndSaved.getVersion());
        assertTrue(Arrays.equals(new byte[] { 1 }, firstlyRetrievedAndSaved.getConfigBody()));
                
        // after that, attempting to save secondlyRetrieved within an outer session scope, which results in a conflicting error, throws an exception instead of being saved through a retry
        try {
            saveEntityCentre(secondlyRetrieved);
            fail("Retry should not have occurred.");
        } catch (final EntityCompanionException ex) {
            assertEquals("Could not resolve conflicting changes. Entity Centre Config [USER CONFIG 1 type] could not be saved.", ex.getMessage());
        }
    }

    // ========================================== CONFLICTING CHANGES RESOLUTION [END] ==========================================

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(User.class, "USER", "DESC").setBase(true).setEmail("USER@unit-test.software").setActive(true));
        save(new_(MainMenuItem.class, "type", "desc").setOrder(1));
    }
    
}