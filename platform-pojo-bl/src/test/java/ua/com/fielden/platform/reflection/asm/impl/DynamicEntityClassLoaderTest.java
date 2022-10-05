package ua.com.fielden.platform.reflection.asm.impl;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getInstance;

import org.junit.Test;

/**
 * A test case for verifying assumptions about {@link DynamicEntityClassLoader}.
 * 
 * @author TG Team
 *
 */
public class DynamicEntityClassLoaderTest {

    @Test
    public void DynamicEntityClassLoader_instances_are_cached_depending_on_the_parent_class_loader() {
        final DynamicEntityClassLoader cl1 = getInstance(getSystemClassLoader());
        final DynamicEntityClassLoader cl2 = getInstance(getSystemClassLoader());
        assertSame("Instances obtained with the same parent class loader should refer to the same object.", cl1, cl2);
        
        // now provide a different parent class loader
        final DynamicEntityClassLoader cl3 = getInstance(cl1);
        assertNotSame("Instances obtained with different parent class loaders should refer to different objects.", cl3, cl2);
    }

}
