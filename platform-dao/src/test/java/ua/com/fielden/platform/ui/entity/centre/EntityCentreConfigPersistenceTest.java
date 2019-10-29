package ua.com.fielden.platform.ui.entity.centre;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemDao;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigPersistenceTest extends AbstractDaoTestCase {
    private final IEntityCentreConfig dao = getInstance(EntityCentreConfigDao.class);
    private final IMainMenuItem menuDao = getInstance(MainMenuItemDao.class);
    private final IUser userDao = getInstance(UserDao.class);
    
    @Test
    public void test_insertion_and_retrieval_of_binary_data() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setDesc("desc");
        config.setConfigBody(new byte[] { 1, 2, 3 });
        dao.saveWithConflicts(config);
        
        final List<EntityCentreConfig> result = dao.getPage(0, 25).data();
        assertEquals("Incorrect number of retrieved configurations.", 1, result.size());
        assertTrue("Incorrectly saved binary property.", Arrays.equals(new byte[] { 1, 2, 3 }, result.get(0).getConfigBody()));
    }
    
    private EntityCentreConfig saveWithConflicts(final EntityCentreConfig config) {
        dao.saveWithConflicts(config);
        return dao.findByEntityAndFetch(null, config);
    }
    
    @Test
    public void test_update_of_binary_data() {
        EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 1, 2, 3 });
        config.setDesc("desc");
        dao.saveWithConflicts(config);
        
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        config.setConfigBody(new byte[] { 1, 2, 3, 4 });
        config = saveWithConflicts(config);
        assertEquals("Incorrect version.", Long.valueOf("1"), config.getVersion());
        
        final EntityCentreConfig fromDb = dao.findById(config.getId());
        
        assertNotNull("Configuration entity should exist.", fromDb);
        assertTrue("Incorrectly updated binary property.", Arrays.equals(new byte[] { 1, 2, 3, 4 }, fromDb.getConfigBody()));
    }
    
    // ========================================== CONFLICTING CHANGES RESOLUTION ==========================================
    // The following tests are based on the situation of the same 1) user 2) config name 3) menu item. Other conflicts will not be automatically resolved due to composite-key nature of 'owner', 'title' and 'menuItem' properties
    
    private EntityCentreConfig saveWithoutConflicts(final EntityCentreConfig config) {
        dao.saveWithoutConflicts(config);
        return dao.findByEntityAndFetch(null, config);
    }
    
    @Test
    public void conflicting_changes_in_EntityCentreConfig_configBody_override_without_exceptions() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithConflicts(config); // no conflict should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------| first
        //       |-------------------------| second
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setConfigBody(new byte[] { 1 });
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setConfigBody(new byte[] { 2 });
        
        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveWithoutConflicts(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), firstlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 1 }, firstlyRetrievedAndSaved.getConfigBody()));
        
        // after that save secondlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig secondlyRetrievedAndSaved = saveWithoutConflicts(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), secondlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 2 }, secondlyRetrievedAndSaved.getConfigBody()));
    }
    
    @Test
    public void conflicting_changes_in_EntityCentreConfig_desc_override_without_exceptions() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithConflicts(config); // no conflict should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------| first
        //       |-------------------------| second
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setDesc("desc1");
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setDesc("desc2");
        
        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveWithoutConflicts(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), firstlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc1", firstlyRetrievedAndSaved.getDesc());
        
        // after that save secondlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig secondlyRetrievedAndSaved = saveWithoutConflicts(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), secondlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc2", secondlyRetrievedAndSaved.getDesc());
    }
    
    @Test
    public void conflicting_changes_in_EntityCentreConfig_desc_override_without_exceptions_when_saving_order_switched() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithConflicts(config); // no conflict should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------------------| first
        //       |----------------| second
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setDesc("desc1");
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setDesc("desc2");
        
        // save secondlyRetrieved
        final EntityCentreConfig secondlyRetrievedAndSaved = saveWithoutConflicts(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), secondlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc2", secondlyRetrievedAndSaved.getDesc());
        
        // after that, save firstlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig firstlyRetrievedAndSaved = saveWithoutConflicts(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), firstlyRetrievedAndSaved.getVersion());
        assertEquals("Incorrect value.", "desc1", firstlyRetrievedAndSaved.getDesc());
    }
    
    @Test
    public void multiple_conflicting_changes_in_EntityCentreConfig_configBody_override_without_exceptions() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 0 });
        config.setDesc("desc0");
        dao.saveWithConflicts(config); // no conflict should appear -- initial saving
        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        
        // |----------------| first
        //       |-------------------------| second
        //                      |-----| third
        
        final EntityCentreConfig firstlyRetrieved = dao.findByEntityAndFetch(null, config);
        firstlyRetrieved.setConfigBody(new byte[] { 1 });
        
        final EntityCentreConfig secondlyRetrieved = dao.findByEntityAndFetch(null, config);
        secondlyRetrieved.setConfigBody(new byte[] { 2 });
        
        // save firstlyRetrieved
        final EntityCentreConfig firstlyRetrievedAndSaved = saveWithoutConflicts(firstlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("1"), firstlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 1 }, firstlyRetrievedAndSaved.getConfigBody()));
        
        final EntityCentreConfig thirdlyRetrieved = dao.findByEntityAndFetch(null, config);
        thirdlyRetrieved.setConfigBody(new byte[] { 3 });
        // save thirdlyRetrieved
        final EntityCentreConfig thirdlyRetrievedAndSaved = saveWithoutConflicts(thirdlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("2"), thirdlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 3 }, thirdlyRetrievedAndSaved.getConfigBody()));
        
        // after that, save secondlyRetrieved and it should not give any conflicting error but instead should complete saving successfully
        final EntityCentreConfig secondlyRetrievedAndSaved = saveWithoutConflicts(secondlyRetrieved);
        assertEquals("Incorrect version.", Long.valueOf("3"), secondlyRetrievedAndSaved.getVersion());
        assertTrue("Incorrect value.", Arrays.equals(new byte[] { 2 }, secondlyRetrievedAndSaved.getConfigBody()));
    }
    // ========================================== CONFLICTING CHANGES RESOLUTION [END] ==========================================
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(User.class, "USER", "DESC").setBase(true).setEmail("USER@unit-test.software").setActive(true));
        save(new_(MainMenuItem.class, "type", "desc").setOrder(1));
    }
    
}