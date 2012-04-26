package ua.com.fielden.platform.reflection.asm.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.asm.ClassAdapter;
import com.google.inject.asm.ClassVisitor;
import com.google.inject.asm.MethodAdapter;
import com.google.inject.asm.MethodVisitor;
import com.google.inject.asm.Opcodes;

/**
 * A class adapter designed for modifying class name.
 *
 * @author TG Team
 *
 */
public class AdvancedChangeNameAdapter extends ClassAdapter implements Opcodes {
    /**
     * Type name that is being put through the adapter.
     */
    private String owner;
    private String enhancedName;

    public AdvancedChangeNameAdapter(final ClassVisitor cv, final String newTypeName) {
	super(cv);
	this.enhancedName = newTypeName;
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, changes the name of the currently being traversed class.
     */
    @Override
    public synchronized void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
	owner = name;
	super.visit(version, access, enhancedName, signature, superName, interfaces);
    }

    public String getOwner() {
	return owner;
    }

    public String getEnhancedName() {
	return enhancedName;
    }

    /**
     * Replaces references to the owner in methods with the enhancedName.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
	// TODO see fix() methods and inner class MethodRenamer in AdvancedAddPropertyAdapter and AdvancedModifyPropertyAdapter
	// TODO They should be unified and used appropriately in all adapters (including this one).
	// TODO See also test_type_name_modification_after_properties_modification() test (@Ignored) in DynamicEntityTypeGenerationTest
	final MethodVisitor mv = cv.visitMethod(access, name, fix(desc), fix(signature), exceptions);
	// check if the method is not abstract
	return mv != null && (access & ACC_ABSTRACT) == 0 ? new MethodRenamer(mv) : mv;
    }

    /**
     * Changes all the occurences of <code>owner<code> with <code>enhancedName</code>.
     */
    private String fix(String s) {
	// TODO see fix() methods and inner class MethodRenamer in AdvancedAddPropertyAdapter and AdvancedModifyPropertyAdapter
	// TODO They should be unified and used appropriately in all adapters (including this one).
	// TODO See also test_type_name_modification_after_properties_modification() test (@Ignored) in DynamicEntityTypeGenerationTest
	if (s != null) {
	    if (s.indexOf(owner) != -1) {
		s = s.replaceAll(Pattern.quote(owner), Matcher.quoteReplacement(enhancedName));
	    }
	}

	return s;
    }

    /**
     * MethodAdapter is a CodeVisitor ie a visitor to visit the bytecode instructions of a Java method.
     *
     * // TODO see fix() methods and inner class MethodRenamer in AdvancedAddPropertyAdapter and AdvancedModifyPropertyAdapter
     * // TODO They should be unified and used appropriately in all adapters (including this one).
     * // TODO See also test_type_name_modification_after_properties_modification() test (@Ignored) in DynamicEntityTypeGenerationTest
     *
     */
    private class MethodRenamer extends MethodAdapter {

	public MethodRenamer(final MethodVisitor mv) {
	    super(mv);
	}

	/**
	 * Visits method type instruction. A type instruction is an instruction that takes a type descriptor as parameter.
	 *
	 * @param opcode
	 *            - the opcode of the type instruction to be visited. This opcode is either NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
	 *
	 * @param desc
	 *            - the operand of the instruction to be visited. This operand is must be a fully qualified class name in internal form, or the type descriptor of an array type
	 *            (see Type).
	 */
	@Override
	public void visitTypeInsn(final int opcode, String desc) {
	    if (owner.equals(desc)) {
		desc = enhancedName;
	    }
	    mv.visitTypeInsn(opcode, desc);
	}

	/**
	 * Visits method field instruction. A field instruction is an instruction that loads or stores the value of a field of an object.
	 *
	 * @param opcode
	 *            - the opcode of the type instruction to be visited. This opcode is either GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
	 * @param owner
	 *            - the internal name of the field's owner class (see getInternalName).
	 * @param name
	 *            - the field's name.
	 * @param desc
	 *            - the field's descriptor (see Type).
	 */
	@Override
	public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
	    if (AdvancedChangeNameAdapter.this.owner.equals(owner)) {
		mv.visitFieldInsn(opcode, enhancedName, name, fix(desc));
	    } else {
		mv.visitFieldInsn(opcode, owner, name, fix(desc));
	    }
	}

	/**
	 * Visits method instruction. A method instruction is an instruction that invokes a method.
	 *
	 * @param opcode
	 *            - the opcode of the type instruction to be visited. This opcode is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE.
	 * @param owner
	 *            - the internal name of the method's owner class (see getInternalName).
	 * @param name
	 *            - the method's name.
	 * @param desc
	 *            - the method's descriptor (see Type).
	 */
	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
	    if (AdvancedChangeNameAdapter.this.owner.equals(owner)) {
		mv.visitMethodInsn(opcode, enhancedName, name, fix(desc));
	    } else {
		mv.visitMethodInsn(opcode, owner, name, fix(desc));
	    }
	}

    }
}
