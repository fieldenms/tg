package ua.com.fielden.platform.reflection.asm.impl;

import static java.util.regex.Pattern.quote;
import static ua.com.fielden.platform.criteria.generator.impl.TypeDiffSerialiser.TYPE_DIFF_SERIALISER;
import static ua.com.fielden.platform.cypher.Checksum.sha256;

import java.security.SecureRandom;
import java.util.Map;
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
    private static final String DOT = ".";
    private static final String DOT_REPLACEMENT = "$$$";

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

    /**
     * Generates a new type name for given non-generated {@code originalType} and type transformations defined in {@code typeDiff} map.
     * <p>
     * Examples:
     *   ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity$$TgEntity_b357467a5ed44cbd8acbc5a9a3923c47
     */
    public static String generateTypeName(final Class<?> originalType, final Map<String, Object> typeDiff) {
        return originalType.getName() + APPENDIX + "_" + sha256(TYPE_DIFF_SERIALISER.serialise(typeDiff));
    }

    /**
     * Generates a new criteria type name for given non-generated {@code originalCriteriaType} and type transformations defined in {@code typeDiff} map.
     * <p>
     * {@code managedType} defines real managed type, from which criteria type is being generated.
     * E.g. Entity Centre can have calculated property "doubleProp := 2 * prop" and that "doubleProp" may be added to selection criteria.
     * <p>
     * Examples:
     *   ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_744D297BD157657F9CB33C140695371539E0AA30DFC22665E714BC9278A18612ua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity
     *   ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_D1EF2F89E5B8C903DEAA022F478632D228CBD0A78205393AC75D514EB49C2BDDua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity$$TgEntity_1D67ABFEB644B0E3CDC6D145A51CA6FC385EFCE71C5DBFFE77F81D7BADCC1158
     */
    public static String generateCriteriaTypeName(final Class<?> originalCriteriaType, final Map<String, Object> typeDiff, final Class<?> managedType) {
        return generateTypeName(originalCriteriaType, typeDiff) + encodeFullName(managedType.getName());
    }

    /**
     * Encodes full class name into form, suitable for insertion into other class name.
     * <p>
     * Examples:
     *   ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity => ua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity
     */
    private static String encodeFullName(final String name) {
        return name.replace(DOT, DOT_REPLACEMENT);
    }

    /**
     * Decodes encoded class name ({@link #encodeFullName(String)}) into class name, suitable for loading.
     * <p>
     * Examples:
     *   ua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity => ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity
     */
    private static String decodeFullName(final String encodedName) {
        return encodedName.replace(DOT_REPLACEMENT, DOT);
    }

    /**
     * Decomposes criteria type name to get original non-generated entity type, from which it was generated.
     * <p>
     * Examples:
     *        ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_744D297BD157657F9CB33C140695371539E0AA30DFC22665E714BC9278A18612ua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity
     *     => ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance
     *
     *        ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity$$TgEntity_b357467a5ed44cbd8acbc5a9a3923c47
     *     => ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity
     */
    public static String decodeOriginalTypeFrom(final String generatedTypeName) {
        final String[] originalAndSuffix = generatedTypeName.split(quote(APPENDIX + "_"));
        return originalAndSuffix[0];
    }

    /**
     * Decomposes criteria type name to get original non-generated entity type, from which it was generated.
     * <p>
     * Examples:
     *   ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_744D297BD157657F9CB33C140695371539E0AA30DFC22665E714BC9278A18612ua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity
     *   ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_D1EF2F89E5B8C903DEAA022F478632D228CBD0A78205393AC75D514EB49C2BDDua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity$$TgEntity_1D67ABFEB644B0E3CDC6D145A51CA6FC385EFCE71C5DBFFE77F81D7BADCC1158
     *   both to => ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity
     */
    public static String decodeOriginalTypeFromCriteriaType(final String criteriaTypeName) {
        final String[] originalAndSuffix = criteriaTypeName.split(quote(APPENDIX + "_")); 
        return decodeFullName(originalAndSuffix[1].substring(64 /*SHA256 length*/));
    }

    /**
     * Decomposes criteria type name to get original generated entity type, from which it was generated.
     * <p>
     * Examples:
     *   ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_D1EF2F89E5B8C903DEAA022F478632D228CBD0A78205393AC75D514EB49C2BDDua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity$$TgEntity_1D67ABFEB644B0E3CDC6D145A51CA6FC385EFCE71C5DBFFE77F81D7BADCC1158
     *     => ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity$$TgEntity_1D67ABFEB644B0E3CDC6D145A51CA6FC385EFCE71C5DBFFE77F81D7BADCC1158
     *   ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance$$TgEntity_D1EF2F89E5B8C903DEAA022F478632D228CBD0A78205393AC75D514EB49C2BDDua$$$com$$$fielden$$$platform$$$sample$$$domain$$$crit_gen$$$TopLevelEntity
     *     => ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity
     */
    public static String decodeOriginalGeneratedTypeFromCriteriaType(final String criteriaTypeName) {
        final String[] originalAndSuffix = criteriaTypeName.split(quote(APPENDIX + "_")); 
        final var sha256AndManagedType = originalAndSuffix[1] + (originalAndSuffix.length > 2 ? APPENDIX + "_" + originalAndSuffix[2] : "");
        return decodeFullName(sha256AndManagedType.substring(64 /*SHA256 length*/));
    }

}