package ua.com.fielden.platform.attachment;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

/**
 * Association between a file type and its mime types.
 */
enum FileTypes {

    TAR("application/x-tar", "application/x-gtar"),
    ZIP("application/zip", "application/x-zip-compressed"),
    GZIP("application/gzip");

    private final List<String> mimes;

    FileTypes(String mime, String... mimes) {
        this.mimes = concatList(List.of(mime), List.of(mimes));
    }

    public static @Nullable FileTypes fromMime(final CharSequence cs) {
        return mimeToFileTypeMap.get(cs.toString());
    }

    private static final Map<String, FileTypes> mimeToFileTypeMap = Arrays.stream(values())
            .flatMap(mt -> mt.mimes.stream().map(m -> t2(m, mt)))
            .collect(toImmutableMap(t2 -> t2._1, t2 -> t2._2));
}
