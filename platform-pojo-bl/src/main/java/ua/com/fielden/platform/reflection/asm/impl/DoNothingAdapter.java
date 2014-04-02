package ua.com.fielden.platform.reflection.asm.impl;

import org.kohsuke.asm3.ClassAdapter;
import org.kohsuke.asm3.ClassVisitor;

/**
 * A class adapter that does nothing.
 * 
 * @author TG Team
 * 
 */
public class DoNothingAdapter extends ClassAdapter {

    public DoNothingAdapter(final ClassVisitor cv) {
        super(cv);
    }

}
