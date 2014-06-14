package ua.com.fielden.platform.reflection.asm.impl;

import org.kohsuke.asm5.ClassVisitor;
import org.kohsuke.asm5.Opcodes;

/**
 * A class adapter that does nothing.
 *
 * @author TG Team
 *
 */
public class DoNothingAdapter extends ClassVisitor {

    public DoNothingAdapter(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

}
