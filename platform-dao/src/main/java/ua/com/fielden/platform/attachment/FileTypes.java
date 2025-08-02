package ua.com.fielden.platform.attachment;

import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Set.of;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/**
 * Association between a file type and its mime types.
 */
enum FileTypes {

    TAR(of("application/x-tar", "application/x-gtar")),
    ZIP(of("application/zip", "application/x-zip-compressed")),
    GZIP(of("application/gzip")),
    OTHER(of("other"));

    private final Set<String> mimes;

    FileTypes(final Set<String> mimes) {
        this.mimes = mimes;
    }

    public static @Nonnull FileTypes fromMime(final CharSequence cs) {
        return mimeToFileTypeMap.getOrDefault(cs.toString(), OTHER);
    }

    private static final Map<String, FileTypes> mimeToFileTypeMap = Arrays.stream(values())
            .filter(ft -> ft != OTHER)
            .flatMap(ft -> ft.mimes.stream().map(m -> t2(m, ft)))
            .collect(toImmutableMap(t2 -> t2._1, t2 -> t2._2));
}
