package ua.com.fielden.platform.reflection.asm.impl.remapper;

import com.google.inject.asm.Type;
import com.google.inject.asm.signature.SignatureReader;
import com.google.inject.asm.signature.SignatureVisitor;
import com.google.inject.asm.signature.SignatureWriter;

/**
 * A class responsible for remapping types and names.
 * Subclasses can override the following methods:
 *
 * <ul>
 * <li>{@link #map(String)} - map type</li>
 * <li>{@link #mapFieldName(String, String, String)} - map field name</li>
 * <li>{@link #mapMethodName(String, String, String)} - map method name</li>
 * </ul>
 *
 * @author Eugene Kuleshov
 */
public abstract class Remapper {

    public String mapDesc(final String desc) {
        final Type t = Type.getType(desc);
        switch (t.getSort()) {
            case Type.ARRAY:
                String s = mapDesc(t.getElementType().getDescriptor());
                for (int i = 0; i < t.getDimensions(); ++i) {
                    s = '[' + s;
                }
                return s;
            case Type.OBJECT:
                final String newType = map(t.getInternalName());
                if (newType != null) {
                    return 'L' + newType + ';';
                }
        }
        return desc;
    }

    private Type mapType(final Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                String s = mapDesc(t.getElementType().getDescriptor());
                for (int i = 0; i < t.getDimensions(); ++i) {
                    s = '[' + s;
                }
                return Type.getType(s);
            case Type.OBJECT:
                s = map(t.getInternalName());
                if(s != null) {
                    return Type.getObjectType(s);
                }
        }
        return t;
    }

    public String mapType(final String type) {
        if (type == null) {
            return null;
        }
        return mapType(Type.getObjectType(type)).getInternalName();
    }

    public String[] mapTypes(final String[] types) {
        String[] newTypes = null;
        boolean needMapping = false;
        for (int i = 0; i < types.length; i++) {
            final String type = types[i];
            final String newType = map(type);
            if (newType != null && newTypes == null) {
                newTypes = new String[types.length];
                if (i > 0) {
                    System.arraycopy(types, 0, newTypes, 0, i);
                }
                needMapping = true;
            }
            if (needMapping) {
                newTypes[i] = newType == null
                    ? type
                    : newType;
            }
        }
        return needMapping
           ? newTypes
           : types;
    }

    public String mapMethodDesc(final String desc) {
        if("()V".equals(desc)) {
            return desc;
        }

        final Type[] args = Type.getArgumentTypes(desc);
        String s = "(";
        for (int i = 0; i < args.length; i++) {
            s += mapDesc(args[i].getDescriptor());
        }
        final Type returnType = Type.getReturnType(desc);
        if(returnType == Type.VOID_TYPE) {
            return s + ")V";
        }
        return s + ')' + mapDesc(returnType.getDescriptor());
    }

    public Object mapValue(final Object value) {
        return value instanceof Type ? mapType((Type) value) : value;
    }

    /**
     *
     * @param typeSignature true if signature is a FieldTypeSignature, such as
     *        the signature parameter of the ClassVisitor.visitField or
     *        MethodVisitor.visitLocalVariable methods
     */
    public String mapSignature(final String signature, final boolean typeSignature) {
        if (signature == null) {
            return null;
        }
        final SignatureReader r = new SignatureReader(signature);
        final SignatureWriter w = new SignatureWriter();
        final SignatureVisitor a = createRemappingSignatureAdapter(w);
        if (typeSignature) {
            r.acceptType(a);
        } else {
            r.accept(a);
        }
        return w.toString();
    }

    protected SignatureVisitor createRemappingSignatureAdapter(
        final SignatureVisitor v)
    {
        return new RemappingSignatureAdapter(v, this);
    }

    /**
     * Map method name to the new name. Subclasses can override.
     */
    public String mapMethodName(final String owner, final String name, final String desc) {
        return name;
    }

    /**
     * Map field name to the new name. Subclasses can override.
     */
    public String mapFieldName(final String owner, final String name, final String desc) {
        return name;
    }

    /**
     * Map type name to the new name. Subclasses can override.
     */
    public String map(final String typeName) {
        return typeName;
    }

}