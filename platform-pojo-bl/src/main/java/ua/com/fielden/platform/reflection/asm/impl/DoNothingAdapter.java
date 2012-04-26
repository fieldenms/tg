package ua.com.fielden.platform.reflection.asm.impl;

import com.google.inject.asm.ClassAdapter;
import com.google.inject.asm.ClassVisitor;


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
