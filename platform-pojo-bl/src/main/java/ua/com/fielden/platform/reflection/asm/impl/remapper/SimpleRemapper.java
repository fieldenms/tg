package ua.com.fielden.platform.reflection.asm.impl.remapper;

import java.util.Collections;
import java.util.Map;

public class SimpleRemapper extends Remapper {

    private final Map<String, String> mapping;

    public SimpleRemapper(final Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public SimpleRemapper(final String oldName, final String newName) {
        this.mapping = Collections.singletonMap(oldName, newName);
    }

    public String mapMethodName(final String owner, final String name, final String desc) {
        final String s = map(owner + '.' + name + desc);
        return s == null ? name : s;
    }

    public String mapFieldName(final String owner, final String name, final String desc) {
        final String s = map(owner + '.' + name);
        return s == null ? name : s;
    }

    public String map(final String key) {
        return mapping.get(key);
    }

}
