package ua.com.fielden.platform.reflection.asm.impl;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

/**
 * A class adapter designed for modifying class name.
 *
 * @author TG Team
 *
 */
public class AdvancedChangeNameAdapter extends ClassRemapper {

    private final String newTypeName;

    public AdvancedChangeNameAdapter(final ClassVisitor cv, final String oldTypeName, final String newTypeName) {
        super(cv, new SimpleRemapper(oldTypeName, newTypeName));
        this.newTypeName = newTypeName;
    }

    public String getNewTypeName() {
        return newTypeName;
    }

}
