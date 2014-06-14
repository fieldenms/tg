package ua.com.fielden.platform.reflection.asm.impl;

import org.kohsuke.asm5.ClassVisitor;
import org.kohsuke.asm5.commons.RemappingClassAdapter;
import org.kohsuke.asm5.commons.SimpleRemapper;

/**
 * A class adapter designed for modifying class name.
 *
 * @author TG Team
 *
 */
public class AdvancedChangeNameAdapter extends RemappingClassAdapter {

    private final String newTypeName;

    public AdvancedChangeNameAdapter(final ClassVisitor cv, final String oldTypeName, final String newTypeName) {
        super(cv, new SimpleRemapper(oldTypeName, newTypeName));
        this.newTypeName = newTypeName;
    }

    public String getNewTypeName() {
        return newTypeName;
    }

}
