package ua.com.fielden.platform.reflection.asm.impl;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * A service for naming new classes created dynamically as the result of new property introduction or modification of existing.
 * 
 * @author TG Team
 * 
 */
public final class DynamicTypeNamingService {
    private static final ThreadLocal<SecureRandom> random = ThreadLocal.withInitial(() -> new SecureRandom());
    public static final String APPENDIX = "$$TgEntity";

    private DynamicTypeNamingService() { }

    /**
     * Generates a new random type name for a given one.
     * If {@code name} already represents a generated type, its {@link #APPENDIX} part gets ignored to ensure the uniform naming pattern.
     *
     * @param name
     * @return
     */
    public static String nextTypeName(final String name) {
        final String baseName = name.contains(APPENDIX) ? name.substring(0, name.indexOf(APPENDIX)) : name;
        return enhancedName(baseName);
    }

    private static String enhancedName(final String name) {
        return name + APPENDIX + "_" + randomUUID().toString().replace("-", "");
    }

    /**
     * Instantiates {@link UUID} using thread-local instance {@code random} to speedup generation.
     *
     * @return
     */
    private static UUID randomUUID() {
        final byte[] randomBytes = new byte[16];
        random.get().nextBytes(randomBytes); // let's reuse SecureRandom
        // the following transformations are copied from the UUID implementation
        randomBytes[6] &= 0x0f; // clear version
        randomBytes[6] |= 0x40; // set to version 4
        randomBytes[8] &= 0x3f; //clear variant
        randomBytes[8] |= (byte) 0x80;  // set to IETF variant
        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);
        return new UUID(msb, lsb);
    }

}