package ua.com.fielden.platform.swing.components.bind;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.components.bind.development.HierarchicalPropertyChangeListener;
import ua.com.fielden.platform.swing.components.bind.test.EntityModuleWithDomainValidatorsForTesting;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.PropertyChangeOrIncorrectAttemptListener;

import com.google.inject.Injector;

/**
 * Test for {@link HierarchicalPropertyChangeListener} functionality
 *
 * @author Yura
 */
public class HierarchicalPropertyChangeListenerTest {

    /**
     * Testing fixtures
     */
    private EntityFactory entityFactory;

    private TopEntity topEntity;

    private Property1 property1, newProperty1;

    private Property2 property2, newProperty2;

    private Property3 property3, newProperty3;
    /**
     * Testing fixtures - END
     */

    /**
     * Flags for checking value assignment inside inner classes
     */
    private Object flag1, flag2;

    @Before
    public void setUp() {
	// setting test fixtures
	final Injector injector = new ApplicationInjectorFactory().add(new EntityModuleWithDomainValidatorsForTesting ()).getInjector();

	entityFactory = injector.getInstance(EntityFactory.class);
	topEntity = entityFactory.newEntity(TopEntity.class, "topEntity", "top desc");
	property1 = entityFactory.newEntity(Property1.class, "property1", "desc1");
	property2 = entityFactory.newEntity(Property2.class, "property2", "desc2");
	property3 = entityFactory.newEntity(Property3.class, "property3", "desc3");
	topEntity.setProperty1(property1.setProperty2(property2.setProperty3(property3)));

	newProperty1 = entityFactory.newEntity(Property1.class, "newProperty1", "newDesc1");
	newProperty2 = entityFactory.newEntity(Property2.class, "newProperty2", "newDesc2");
	newProperty3 = entityFactory.newEntity(Property3.class, "newProperty3", "newDesc3");
	newProperty1.setProperty2(newProperty2.setProperty3(newProperty3));

	// nullifying test flags
	flag1 = flag2 = null;
    }

    @Test
    public void testHierarchicalAdditionAndRemoval() {
	HierarchicalPropertyChangeListener.addPropertyListenerToPropertyHierarchy(topEntity, "property1.property2.property3.desc", new PropertyChangeOrIncorrectAttemptListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
	    }
	});

	// checking whether SubjectValueChangeHandlers were added to whole hierarchy
	Assert.assertTrue(topEntity.getChangeSupport().getPropertyChangeListeners("property1").length == 1);
	Assert.assertTrue(property1.getChangeSupport().getPropertyChangeListeners("property2").length == 1);
	Assert.assertTrue(property2.getChangeSupport().getPropertyChangeListeners("property3").length == 1);
	Assert.assertTrue(property3.getChangeSupport().getPropertyChangeListeners("desc").length == 1);

	topEntity.setProperty1(newProperty1);

	// checking whether SubjectValueChangeHandlers were removed old hierarchy
	Assert.assertTrue(property1.getChangeSupport().getPropertyChangeListeners("property2").length == 0);
	Assert.assertTrue(property2.getChangeSupport().getPropertyChangeListeners("property3").length == 0);
	Assert.assertTrue(property3.getChangeSupport().getPropertyChangeListeners("desc").length == 0);
	// checking whether SubjectValueChangeHandlers were added to new hierarchy
	Assert.assertTrue(newProperty1.getChangeSupport().getPropertyChangeListeners("property2").length == 1);
	Assert.assertTrue(newProperty2.getChangeSupport().getPropertyChangeListeners("property3").length == 1);
	Assert.assertTrue(newProperty3.getChangeSupport().getPropertyChangeListeners("desc").length == 1);

	newProperty2.setProperty3(null);
	// checking whether SubjectValueChangeHandler were correctly removed from property3 hierarchy
	Assert.assertTrue(newProperty3.getChangeSupport().getPropertyChangeListeners("desc").length == 0);

	newProperty1.setProperty2(null);
	// checking whether SubjectValueChangeHandler were correctly removed from property2 hierarchy
	Assert.assertTrue(newProperty2.getChangeSupport().getPropertyChangeListeners("property3").length == 0);

	newProperty2.setProperty3(newProperty3);
	newProperty1.setProperty2(newProperty2);
	Assert.assertTrue(newProperty1.getChangeSupport().getPropertyChangeListeners("property2").length == 1);
	Assert.assertTrue(newProperty2.getChangeSupport().getPropertyChangeListeners("property3").length == 1);
	Assert.assertTrue(newProperty3.getChangeSupport().getPropertyChangeListeners("desc").length == 1);
    }

    @Test
    public void testValueSetting() throws InterruptedException, InvocationTargetException {
	// cheking whether correct values are passed to IComponentUpdater.updateComponent(...) method
	HierarchicalPropertyChangeListener.addPropertyListenerToPropertyHierarchy(topEntity, "property1.property2.property3.desc", new PropertyChangeOrIncorrectAttemptListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent event) {
		flag1 = event.getOldValue();
		flag2 = event.getNewValue();
	    }
	});
	// we should wait for property change listener firing, because part of it occurs on EDT, and test is not on EDT
	SwingUtilities.invokeAndWait(new Runnable() {
	    public void run() {
		topEntity.setProperty1(newProperty1);
	    }
	});
	Assert.assertEquals("desc3", flag1);
	Assert.assertEquals("newDesc3", flag2);

	flag1 = flag2 = null;
	// we should wait for property change listener firing, because part of it occurs on EDT, and test is not on EDT
	SwingUtilities.invokeAndWait(new Runnable() {
	    public void run() {
		newProperty2.setProperty3(null);
	    }
	});
	Assert.assertEquals("newDesc3", flag1);
	Assert.assertNull(flag2);

	flag1 = flag2 = null;
	SwingUtilities.invokeAndWait(new Runnable() {
	    public void run() {
		newProperty2.setProperty3(newProperty3);
		topEntity.setProperty1(null);
	    }
	});
	Assert.assertEquals("newDesc3", flag1);
	Assert.assertEquals(null, flag2);
    }

    @Test
    public void testHierarchicalAdditionAndRemovalForExtraListener() throws InterruptedException, InvocationTargetException {
	flag1 = flag2 = null;
	HierarchicalPropertyChangeListener.addValidationListenerToPropertyHierarchy(topEntity, "property1.property2.property3.desc", new PropertyChangeListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (flag1 == null) {
		    flag1 = new Integer(1);
		} else {
		    flag1 = ((Integer) flag1) + 1;
		}
	    }
	});

	// checking whether validation result listeners were added correctly
	Assert.assertTrue(topEntity.getProperty("property1").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(property1.getProperty("property2").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(property2.getProperty("property3").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(property3.getProperty("desc").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);

	SwingUtilities.invokeAndWait(new Runnable() {
	    public void run() {
		property1.setProperty2(newProperty2);
	    }
	});

	// checking whether validation result listeners were correctly removed from old property hierarchy
	Assert.assertTrue(topEntity.getProperty("property1").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(property1.getProperty("property2").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(property2.getProperty("property3").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 0);
	Assert.assertTrue(property3.getProperty("desc").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 0);

	// checking whether validation result listeners were correctly added to new property hierarchy
	Assert.assertTrue(newProperty2.getProperty("property3").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(newProperty3.getProperty("desc").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);

	SwingUtilities.invokeAndWait(new Runnable() {
	    public void run() {
		topEntity.setProperty1(null);
	    }
	});

	// checking whether validation result listeners were correctly removed from old property hierarchy
	Assert.assertTrue(topEntity.getProperty("property1").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 1);
	Assert.assertTrue(newProperty1.getProperty("property2").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 0);
	Assert.assertTrue(newProperty2.getProperty("property3").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 0);
	Assert.assertTrue(newProperty3.getProperty("desc").getChangeSupport().getPropertyChangeListeners(MetaProperty.VALIDATION_RESULTS_PROPERTY_NAME).length == 0);
    }

    @Test
    public void testCompleteRemovalOfHierarchicalListeners() {
	// adding and removing listeners from entity
	HierarchicalPropertyChangeListener topListener = HierarchicalPropertyChangeListener.addPropertyListenerToPropertyHierarchy(topEntity, "property1.property2.property3.desc", new PropertyChangeListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
	    }
	});
	HierarchicalPropertyChangeListener.removeListenersFromHierarchy(topListener);

	// checking whether all listeners were correctly removed from entity property hierarchy
	Assert.assertTrue(topEntity.getChangeSupport().getPropertyChangeListeners("property1").length == 0);
	Assert.assertTrue(property1.getChangeSupport().getPropertyChangeListeners("property2").length == 0);
	Assert.assertTrue(property2.getChangeSupport().getPropertyChangeListeners("property3").length == 0);
	Assert.assertTrue(property3.getChangeSupport().getPropertyChangeListeners("desc").length == 0);

	// lets test complete removal in case when hierarchy is not full
	// adding listeners again
	topListener = HierarchicalPropertyChangeListener.addPropertyListenerToPropertyHierarchy(topEntity, "property1.property2.property3.desc", new PropertyChangeListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
	    }
	});

	// removing part of hierarchy
	property1.setProperty2(null);

	// removing all listeners
	HierarchicalPropertyChangeListener.removeListenersFromHierarchy(topListener);

	// checking whether all listeners were correctly removed from entity property hierarchy
	Assert.assertTrue(topEntity.getChangeSupport().getPropertyChangeListeners("property1").length == 0);
	Assert.assertTrue(property1.getChangeSupport().getPropertyChangeListeners("property2").length == 0);
	Assert.assertTrue(property2.getChangeSupport().getPropertyChangeListeners("property3").length == 0);
	Assert.assertTrue(property3.getChangeSupport().getPropertyChangeListeners("desc").length == 0);
    }

    /**
     * Inner class for testing
     *
     * @author Yura
     */
    @KeyType(String.class)
    public static class TopEntity extends AbstractEntity<String> {

	private static final long serialVersionUID = 1L;

	@IsProperty
	private Property1 property1;

	public TopEntity() {
	}

	public TopEntity(final String key, final String desc) {
	    super(null, key, desc);
	}

	public Property1 getProperty1() {
	    return property1;
	}

	@Observable
	public TopEntity setProperty1(final Property1 property1) {
	    this.property1 = property1;
	    return this;
	}

    }

    /**
     * Inner class for testing
     *
     * @author Yura
     */
    @KeyType(String.class)
    public static class Property1 extends AbstractEntity<String> {

	private static final long serialVersionUID = 1L;

	@IsProperty
	private Property2 property2;

	public Property1() {
	}

	public Property1(final String key, final String desc) {
	    super(null, key, desc);
	}

	public Property2 getProperty2() {
	    return property2;
	}

	@Observable
	public Property1 setProperty2(final Property2 property2) {
	    this.property2 = property2;
	    return this;
	}

    }

    /**
     * Inner class for testing
     *
     * @author Yura
     */
    @KeyType(String.class)
    public static class Property2 extends AbstractEntity<String> {

	private static final long serialVersionUID = 1L;

	@IsProperty
	private Property3 property3;

	public Property2() {
	}

	public Property2(final String key, final String desc) {
	    super(null, key, desc);
	}

	public Property3 getProperty3() {
	    return property3;
	}

	@Observable
	public Property2 setProperty3(final Property3 property3) {
	    this.property3 = property3;
	    return this;
	}

    }

    /**
     * Inner class for testing
     *
     * @author Yura
     */
    @KeyType(String.class)
    public static class Property3 extends AbstractEntity<String> {

	private static final long serialVersionUID = 1L;

	public Property3() {
	}

	public Property3(final String key, final String desc) {
	    super(null, key, desc);
	}

    }

}
