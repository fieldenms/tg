package ua.com.fielden.platform.reflection.asm.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public class AsmUtils {

    /**
     * Returns the type signature according to the java class format representing {@code type}.
     * <p>
     * Note: Generic arrays are not handled correctly.
     * @param type
     * @return
     */
    public static String getTypeSignature(final Type type) {
        final StringBuilder sb = new StringBuilder();

        if (type instanceof ParameterizedType paramType) {
            sb.append(getTypeSignature(paramType.getRawType()));
            final int newLength = sb.length() - 1;
            sb.deleteCharAt(newLength); // remove last ";" to make room for type arguments
            // we also need to remember newLength to insert "<" before type arguments

            Arrays.stream(paramType.getActualTypeArguments()).map(AsmUtils::getTypeSignature).forEach(sig -> sb.append(sig));
            if (!sb.isEmpty()) {
                sb.insert(newLength, '<');
                sb.append('>');
            }

            sb.append(';');
        }
        else {
            sb.append(net.bytebuddy.jar.asm.Type.getDescriptor(PropertyTypeDeterminator.classFrom(type)));
        }

        return sb.toString();
    }


}
