package ua.com.fielden.platform.reflection.asm.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.utils.Pair;

import org.kohsuke.asm3.AnnotationVisitor;
import org.kohsuke.asm3.ClassAdapter;
import org.kohsuke.asm3.ClassVisitor;
import org.kohsuke.asm3.FieldVisitor;
import org.kohsuke.asm3.MethodAdapter;
import org.kohsuke.asm3.MethodVisitor;
import org.kohsuke.asm3.Opcodes;
import org.kohsuke.asm3.Type;


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
     * Additionally, modifies and records the name of the class currently being traversed.
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
     * Its primary use is to correctly construct collectional properties based on the type
     * parameter as specified in the IsProperty annotation descriptor.
     *
     * @param pd
     * @param propertyType
     * @return
     */
    private String constructSignature(final NewProperty pd, final String propertyType) {
	final IsProperty adIsProperty = (IsProperty) pd.getAnnotationByType(IsProperty.class);
	final String signature = adIsProperty != null && adIsProperty.value() != Void.class ? Type.getDescriptor(adIsProperty.value()) : null;
	final String signatureMode = signature == null ? propertyType : propertyType.substring(0, propertyType.length() - 1) + "<" + signature + ">;";
	return signatureMode;
    }

    private void addPropertyField(final NewProperty pd, final ClassVisitor cv) {
	final String propertyType = Type.getDescriptor(pd.type);
	final String signatureMode = constructSignature(pd, propertyType);
	final FieldVisitor fvProperty = cv.visitField(ACC_PRIVATE, pd.name, propertyType, "Z".equals(signatureMode) ? null : signatureMode, null);

	addRequiredAnnotations(pd);

	// add other annotations to the field being generated
	for (final Annotation annotation : pd.annotations) {
	    processAnnotationParam(fvProperty.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true), annotation);
	}

	// finalise field generation
	fvProperty.visitEnd();
    }

    private void addRequiredAnnotations(final NewProperty pd) {
	// mark the field as generated
	pd.addAnnotation(new Generated(){
	    @Override
	    public Class<Generated> annotationType() {
		return Generated.class;
	    }});

	// the generated field should correspond to a property
	// thus it should have annotation IsProperty, but the annotation descriptor list may already contain it
	// therefore add IsProperty just in case -- it will not be added if already present
	pd.addAnnotation(new IsProperty() {
	    @Override
	    public Class<IsProperty> annotationType() {
		return IsProperty.class;
	    }

	    @Override
	    public Class<?> value() {
		return Void.class;
	    }

	    @Override
	    public String linkProperty() {
	        return "----dummy-property----";
	    }
	});
	// the same goes about the Title annotation as property should have title and description
	pd.addAnnotation(new Title() {
	    @Override
	    public Class<Title> annotationType() {
		return Title.class;
	    }

	    @Override
	    public String value() {
		return pd.title;
	    }

	    @Override
	    public String desc() {
		return pd.desc;
	    }

	});
    }

    private void processAnnotationParam(final AnnotationVisitor av, final Annotation annotation) {
	final List<String> params = Reflector.annotataionParams(annotation.getClass());

	for (final String name : params) {
	    final Pair<Class<?>, Object> pair = Reflector.getAnnotationParamValue(annotation, name);
	    final Class<?> type = pair.getKey();
	    final Object value = pair.getValue();
	    processValueForAnnotation(av, name, type, value);
	}
	av.visitEnd();
    }

    private void processValueForAnnotation(final AnnotationVisitor av, final String name, final Class<?> originalType, final Object value) {
	final Class<?> type = (Annotation.class.isAssignableFrom(originalType) && originalType.getName().contains("$")) ? originalType.getInterfaces()[0] : originalType;

	if (Enum.class.isAssignableFrom(type)) {
	    av.visitEnum(name, Type.getDescriptor(type), value.toString());
	} else if (value instanceof Class) { // if the parameter value is a class then need to use its description
	    av.visit(name, Type.getType((Class) value));
	} else if (value instanceof Annotation) {
	    final AnnotationVisitor avAnnotation = av.visitAnnotation(name, Type.getDescriptor(type));
	    processAnnotationParam(avAnnotation, (Annotation) value);
	} else if (type.isArray()) {
	    final Class<?> arrayType = type.getComponentType();
	    final Object[] array = (Object[]) value;
	    final AnnotationVisitor avArray = av.visitArray(name);
	    for (final Object arVal : array) {
		processValueForAnnotation(avArray, null, arrayType, arVal);
	    }
	    avArray.visitEnd();
	} else if (value != null){
	    av.visit(name, value);
	}
    }

    private void addPropertyGetter(final NewProperty pd, final ClassVisitor cv) {
	final String propertyType = Type.getDescriptor(pd.type);
	final String signatureMode = constructSignature(pd, propertyType);
	final String signature = signatureMode != null ? "()" + signatureMode : null;

	final String getterName = "get" + pd.name.substring(0, 1).toUpperCase() + pd.name.substring(1);
	final MethodVisitor mvGetProperty = cv.visitMethod(ACC_PUBLIC, getterName, "()" + propertyType,
		"Z".equals(propertyType) ? null : signature, null);
	mvGetProperty.visitCode();
	mvGetProperty.visitVarInsn(ALOAD, 0);
	mvGetProperty.visitFieldInsn(GETFIELD, enhancedName, pd.name, propertyType);
	if ("Z".equals(propertyType)) {
	    mvGetProperty.visitInsn(IRETURN);
	} else {
	    mvGetProperty.visitInsn(ARETURN);
	}
	mvGetProperty.visitMaxs(1, 1);
	mvGetProperty.visitEnd();
    }

    private void addPropertySetter(final NewProperty pd, final ClassVisitor cv) {
	final String propertyType = Type.getDescriptor(pd.type);
	final String signatureMode = constructSignature(pd, propertyType);
	final String signature = signatureMode != null ? "(" + signatureMode + ")V" : null;

	final String setterName = "set" + pd.name.substring(0, 1).toUpperCase() + pd.name.substring(1);
	final MethodVisitor mvSetProperty = cv.visitMethod(ACC_PUBLIC, setterName, "(" + propertyType + ")V",
		"Z".equals(propertyType) ? null : signature, null);
	final AnnotationVisitor avObservable = mvSetProperty.visitAnnotation(Type.getDescriptor(Observable.class), true);
	avObservable.visitEnd();
	mvSetProperty.visitCode();
	mvSetProperty.visitVarInsn(ALOAD, 0);
	if ("Z".equals(propertyType)) {
	    mvSetProperty.visitVarInsn(ILOAD, 1);
	} else {
	    mvSetProperty.visitVarInsn(ALOAD, 1);
	}
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
     * Changes all the occurrences of <code>owner<code> with <code>enhancedName</code>.
     */
    private String fix(String signature) {

	if (signature != null) {
	    if (signature.indexOf(owner + ";") != -1) {
		signature = signature.replaceAll(Pattern.quote(owner + ";"), Matcher.quoteReplacement(enhancedName + ";"));
	    }
	}

	return signature;
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

    private boolean bool;

    public boolean getBool() {
	return bool;
    }

    public void setBool(final boolean value) {
	bool = value;
    }

    public static void main(final String[] args) {
	System.out.println(Type.getDescriptor(float.class));
    }

}
