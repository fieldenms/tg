package ua.com.fielden.platform.reflection.asm.impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.AnnotationDescriptor;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;

import com.google.inject.asm.AnnotationVisitor;
import com.google.inject.asm.ClassAdapter;
import com.google.inject.asm.ClassVisitor;
import com.google.inject.asm.FieldVisitor;
import com.google.inject.asm.MethodAdapter;
import com.google.inject.asm.MethodVisitor;
import com.google.inject.asm.Opcodes;
import com.google.inject.asm.Type;

/**
 * A class adapter designed for adding new properties to a class.
 *
 * @author TG Team
 *
 */
public class AdvancedAddPropertyAdapter extends ClassAdapter implements Opcodes {

    /**
     * Properties to be added.
     */
    private final Map<String, NewProperty> propertiesToAdd;
    /**
     * Type name that is being put through the adapter.
     */
    private String owner;
    private String enhancedName;

    private final DynamicTypeNamingService namingService;

    public AdvancedAddPropertyAdapter(final ClassVisitor cv, final DynamicTypeNamingService namingService, final Map<String, NewProperty> propertiesToAdd) {
	super(cv);
	this.propertiesToAdd = propertiesToAdd;
	this.namingService = namingService;
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, modifies and records the name of the currently being traversed class.
     */
    @Override
    public synchronized void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
	owner = name;
	enhancedName = namingService.nextTypeName(name);
	super.visit(version, access, enhancedName, signature, superName, interfaces);
    }

    /**
     * Visits fields declaration to check of the to be added fields do not conflict with existing. The conflicting ones will not be added.
     */
    @Override
    public synchronized FieldVisitor visitField(final int access, final String fieldName, final String desc, final String signature, final Object value) {
	if (propertiesToAdd.containsKey(fieldName)) {
	    propertiesToAdd.remove(fieldName);
	}
	return super.visitField(access, fieldName, desc, signature, value);
    }

    /**
     * Replaces references to the owner in methods with the enhancedName.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
	final MethodVisitor mv = cv.visitMethod(access, name, fix(desc), fix(signature), exceptions);
	// check if the method is not abstract
	return mv != null && (access & ACC_ABSTRACT) == 0 ? new MethodRenamer(mv) : mv;
    }

    /**
     * This is where new fields and their mutators are added.
     */
    @Override
    public void visitEnd() {
	for (final NewProperty np : propertiesToAdd.values()) {
	    addPropertyField(np, cv);
	    addPropertyGetter(np, cv);
	    addPropertySetter(np, cv);
	}
	super.visitEnd();
    }

    /**
     * Constructs a signature based on the new property annotation descriptor information.
     * Its primary use if to correctly construct collectional properties based on the type parameter as specified in the IsProperty annotation descriptor.
     *
     * @param pd
     * @param propertyType
     * @return
     */
    private String constructSignature(final NewProperty pd, final String propertyType) {
	final AnnotationDescriptor adIsProperty = pd.getAnnotationDescriptorByType(IsProperty.class);
	final String signature = adIsProperty != null && adIsProperty.params.get("value") != null ?  Type.getDescriptor((Class) adIsProperty.params.get("value")) : null;
	final String signatureMode = signature == null ? null :
	    propertyType.substring(0, propertyType.length()-1) + "<" + signature + ">;";
	return signatureMode;
    }

    private void addPropertyField(final NewProperty pd, final ClassVisitor cv) {
	final String propertyType  = Type.getDescriptor(pd.type);
	final String signatureMode = constructSignature(pd, propertyType);
	final FieldVisitor fvProperty = cv.visitField(ACC_PRIVATE, pd.name, propertyType, signatureMode, null);

	// mark the field as generated
	fvProperty.visitAnnotation(Type.getDescriptor(Generated.class), true).visitEnd();

	// the generated field should correspond to a property
	// thus it should have annotation IsProperty, but the annotation descriptor list may already contain it
	// therefore add IsProperty only if it is not already present in the list
	if (!pd.containsAnnotationDescriptorFor(IsProperty.class)) {
	    final AnnotationVisitor avIsProperty = fvProperty.visitAnnotation(Type.getDescriptor(IsProperty.class), true);
	    avIsProperty.visitEnd();
	}

	// property should have title and description
	final AnnotationVisitor avTitle = fvProperty.visitAnnotation(Type.getDescriptor(Title.class), true);
	avTitle.visit("value", pd.title);
	avTitle.visit("desc", pd.desc);
	avTitle.visitEnd();

	// add other annotations to the field being generated
	for (final AnnotationDescriptor ad : pd.annotations) {
	    final AnnotationVisitor av = fvProperty.visitAnnotation(Type.getDescriptor(ad.type), true);
	    for (final Map.Entry<String, Object> param : ad.params.entrySet()) {
		// determine the type of annotation method to correctly handle Enum types.
		// TODO Perhaps nested annotations / arrays should be handled too.
		final Class<?> methodType = PropertyTypeDeterminator.determinePropertyType(ad.type, param.getKey() + "()");
		if (Enum.class.isAssignableFrom(methodType)) {
		    av.visitEnum(param.getKey(), Type.getDescriptor(methodType), param.getValue().toString());
		} else if (param.getValue() instanceof Class){ // if the parameter value is a class then need to use its description
		    final Type value = Type.getType((Class) param.getValue());
		    av.visit(param.getKey(), value);
		} else {
		    av.visit(param.getKey(), param.getValue());
		}
	    }
	    av.visitEnd();
	}

	// finalise field generation
	fvProperty.visitEnd();
    }


    private void addPropertyGetter(final NewProperty pd, final ClassVisitor cv) {
	final String propertyType  = Type.getDescriptor(pd.type);
	final String signatureMode = constructSignature(pd, propertyType);
	final String signature = signatureMode != null ? "()" + signatureMode : null;

	final String getterName = "get" + pd.name.substring(0, 1).toUpperCase() + pd.name.substring(1);
	final MethodVisitor mvGetProperty = cv.visitMethod(ACC_PUBLIC, getterName, "()" + propertyType, signature, null);
	mvGetProperty.visitCode();
	mvGetProperty.visitVarInsn(ALOAD, 0);
	mvGetProperty.visitFieldInsn(GETFIELD, enhancedName, pd.name, propertyType);
	mvGetProperty.visitInsn(ARETURN);
	mvGetProperty.visitMaxs(1, 1);
	mvGetProperty.visitEnd();
    }

    private void addPropertySetter(final NewProperty pd, final ClassVisitor cv) {
	final String propertyType  = Type.getDescriptor(pd.type);
	final String signatureMode = constructSignature(pd, propertyType);
	final String signature = signatureMode != null ? "(" + signatureMode + ")V" : null;

	final String setterName = "set" + pd.name.substring(0, 1).toUpperCase() + pd.name.substring(1);
	final MethodVisitor mvSetProperty = cv.visitMethod(ACC_PUBLIC, setterName, "(" + propertyType + ")V", signature, null);
	final AnnotationVisitor avObservable = mvSetProperty.visitAnnotation(Type.getDescriptor(Observable.class), true);
	avObservable.visitEnd();
	mvSetProperty.visitCode();
	mvSetProperty.visitVarInsn(ALOAD, 0);
	mvSetProperty.visitVarInsn(ALOAD, 1);
	mvSetProperty.visitFieldInsn(PUTFIELD, enhancedName, pd.name, propertyType);
	mvSetProperty.visitInsn(RETURN);
	mvSetProperty.visitMaxs(2, 2);
	mvSetProperty.visitEnd();
    }

    public String getOwner() {
	return owner;
    }

    public String getEnhancedName() {
	return enhancedName;
    }

    /**
     * Changes all the occurences of <code>owner<code> with <code>enhancedName</code>.
     */
    private String fix(String s) {

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
	    if (AdvancedAddPropertyAdapter.this.owner.equals(owner)) {
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
	    if (AdvancedAddPropertyAdapter.this.owner.equals(owner)) {
		mv.visitMethodInsn(opcode, enhancedName, name, fix(desc));
	    } else {
		mv.visitMethodInsn(opcode, owner, name, fix(desc));
	    }
	}

    }
}
