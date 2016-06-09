package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * Unit tests to ensure correct comparison of composite entities with optional key members.
 *
 * @author TG Team
 *
 */
public class DynamicEntityKeyWithOptionalMemebersTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void entities_that_have_first_members_null_but_second_members_different_should_not_be_equal() {
        final EntityWithTwoOptionalKeyMembers instance1 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance1.setProperty1(null);
        instance1.setProperty2("value1");
        
        final EntityWithTwoOptionalKeyMembers instance2 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance2.setProperty1(null);
        instance2.setProperty2("value2");
        
        assertFalse(instance1.equals(instance2));
    }
    
    @Test
    public void entities_that_have_first_members_not_null_and_equal_but_second_members_different_should_not_be_equal() {
        final EntityWithTwoOptionalKeyMembers instance1 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance1.setProperty1(1L);
        instance1.setProperty2("value1");
        
        final EntityWithTwoOptionalKeyMembers instance2 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance2.setProperty1(1L);
        instance2.setProperty2("value2");
        
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void entities_that_have_all_key_members_null_should_be_equal() {
        final EntityWithTwoOptionalKeyMembers instance1 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance1.setProperty1(null);
        instance1.setProperty2(null);
        
        final EntityWithTwoOptionalKeyMembers instance2 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance2.setProperty1(null);
        instance2.setProperty2(null);
        
        assertTrue(instance1.equals(instance2));
    }

    @Test
    public void entities_that_have_all_not_null_and_equal_members_should_not_be_equal() {
        final EntityWithTwoOptionalKeyMembers instance1 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance1.setProperty1(1L);
        instance1.setProperty2("value");
        
        final EntityWithTwoOptionalKeyMembers instance2 = factory.newEntity(EntityWithTwoOptionalKeyMembers.class);
        instance2.setProperty1(1L);
        instance2.setProperty2("value");
        
        assertTrue(instance1.equals(instance2));
    }
    
    @Test
    public void entities_with_three_members_that_contain_nulls_but_different_only_in_third_not_null_member_should_not_be_equals() {
        final EntityWithTreeOptionalKeyMembers instance1 = factory.newEntity(EntityWithTreeOptionalKeyMembers.class);
        instance1.setProperty1(1L);
        instance1.setProperty2(null);
        instance1.setProperty3(1);
        
        final EntityWithTreeOptionalKeyMembers instance2 = factory.newEntity(EntityWithTreeOptionalKeyMembers.class);
        instance2.setProperty1(1L);
        instance2.setProperty2(null);
        instance2.setProperty3(2);
        
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void entities_with_three_members_that_contain_nulls_and_all_members_equal_should_be_equals() {
        final EntityWithTreeOptionalKeyMembers instance1 = factory.newEntity(EntityWithTreeOptionalKeyMembers.class);
        instance1.setProperty1(1L);
        instance1.setProperty2(null);
        instance1.setProperty3(null);
        
        final EntityWithTreeOptionalKeyMembers instance2 = factory.newEntity(EntityWithTreeOptionalKeyMembers.class);
        instance2.setProperty1(1L);
        instance2.setProperty2(null);
        instance2.setProperty3(null);
        
        assertTrue(instance1.equals(instance2));
    }

    
    @KeyType(DynamicEntityKey.class)
    static class EntityWithTwoOptionalKeyMembers extends AbstractEntity<DynamicEntityKey> {
        private static final long serialVersionUID = 1L;

        @IsProperty
        @CompositeKeyMember(1)
        protected Long property1;

        @IsProperty
        @CompositeKeyMember(2)
        private String property2;

        @Observable
        public EntityWithTwoOptionalKeyMembers setProperty1(final Long property1) {
            this.property1 = property1;
            return this;
        }

        public Long getProperty1() {
            return property1;
        }

        @Observable
        public EntityWithTwoOptionalKeyMembers setProperty2(final String property2) {
            this.property2 = property2;
            return this;
        }

        public String getProperty2() {
            return property2;
        }

    }
    
    @KeyType(DynamicEntityKey.class)
    static class EntityWithTreeOptionalKeyMembers extends AbstractEntity<DynamicEntityKey> {
        private static final long serialVersionUID = 1L;

        @IsProperty
        @CompositeKeyMember(1)
        protected Long property1;

        @IsProperty
        @CompositeKeyMember(2)
        private String property2;

        @IsProperty
        @CompositeKeyMember(3)
        private Integer property3;

        @Observable
        public EntityWithTreeOptionalKeyMembers setProperty3(final Integer property3) {
            this.property3 = property3;
            return this;
        }

        public Integer getProperty3() {
            return property3;
        }

        @Observable
        public EntityWithTreeOptionalKeyMembers setProperty1(final Long property1) {
            this.property1 = property1;
            return this;
        }

        public Long getProperty1() {
            return property1;
        }

        @Observable
        public EntityWithTreeOptionalKeyMembers setProperty2(final String property2) {
            this.property2 = property2;
            return this;
        }

        public String getProperty2() {
            return property2;
        }

    }
}
