package ua.com.fielden.platform.reflection.asm.impl;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A class adapter that does nothing.
 *
 * @author TG Team
 *
 */
public class DoNothingAdapter extends ClassVisitor {

    public DoNothingAdapter(final ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
    }

}
