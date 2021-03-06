package ua.com.fielden.platform.reflection.asm.impl;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * A service for naming new classes created dynamically as the result of new property introduction or modification of existing.
 * 
 * @author TG Team
 * 
 */
public final class DynamicTypeNamingService {
    public static final String APPENDIX = "$$TgEntity";
    
    private DynamicTypeNamingService() { }

    private static String enhancedName(final String name) {
        return name + APPENDIX + "_" + StringUtils.remove(UUID.randomUUID().toString(), "-");
    }

    public static String nextTypeName(final String name) {
        final String baseName = name.contains(APPENDIX) ? name.substring(0, name.indexOf(APPENDIX)) : name;
        return enhancedName(baseName);
    }

}
