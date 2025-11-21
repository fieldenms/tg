package ua.com.fielden.platform.continuation;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.continuation.exceptions.ContinuationException;
import ua.com.fielden.platform.entity.IContinuationData;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.apache.logging.log4j.LogManager.getLogger;

/// A storage for data collected by continuations.
/// The data is maintained in a [ScopedValue] containing a [ConcurrentHashMap], ensuring that all
/// companions within the same scope have access to all data captured by continuations.
///
/// A [ConcurrentHashMap] is used because [#putMoreData(String, IContinuationData)] mutates the map.
/// When Virtual Threads are created within the same bound context, this choice provides the necessary
/// concurrency safety for such mutations.
///
/// Use [#runWithMoreData(Map, Runnable)] to initialise this storage and execute an operation within the
/// same context that does not return a result.
/// For operations that produce a result, use [#callWithMoreData(Map, Supplier)].
///
/// **IMPORTANT:**
/// Calling [#runWithMoreData(Map, Runnable)] or [#callWithMoreData(Map, Supplier)] in a context where a scoped value is already bound
/// will mutate the existing bound value by adding entries from the provided map.
///
public class NeedMoreDataStorage {

    private final static Logger LOGGER = getLogger();

    public static final String
            ERR_CANNOT_ADD_MORE_DATA = "Cannot add more data because the storage is not bound in this context.",
            WARN_UNBOUND_STORAGE = "The need more data storage is not bound in this context. This likely indicates a programming error.";

    private NeedMoreDataStorage() {}

    private static final ScopedValue<Map<String, IContinuationData>> STORAGE = ScopedValue.newInstance();

    /// Initialises the scoped storage with `moreData` and executes `op` within this context.
    /// If the scoped storage already contains data, it will be enriched with the entries from `moreData`.
    ///
    public static void runWithMoreData(final Map<String, IContinuationData> moreData, final Runnable op) {
        ScopedValue.where(STORAGE, initStorage(moreData)).run(op);
    }

    /// Initialises the scoped storage with `moreData` and executes `op` within this context.
    /// If the scoped storage already contains data, it will be enriched with the entries from `moreData`.
    ///
    /// Unlike [#runWithMoreData(Map, Runnable)], this method executes an operation that returns a result.
    ///
    public static <R> R callWithMoreData(final Map<String, IContinuationData> moreData, final Supplier<R> op) {
        return ScopedValue.where(STORAGE, initStorage(moreData)).call(op::get);
    }

    /// Creates a new map to be used as a scoped value.
    /// If a scoped value is already bound, the map is enriched with entries from `moreData`.
    ///
    private static Map<String, IContinuationData> initStorage(final Map<String, IContinuationData> moreData) {
        final var storage = STORAGE.isBound()
                            ? STORAGE.get()
                            : new ConcurrentHashMap<String, IContinuationData>();
        storage.putAll(moreData);
        return storage;
    }

    /// Returns an immutable map of continuation data if a storage is bound in the current invocation context.
    /// Otherwise, throws a [ContinuationException], indicating an invalid operation due to missing storage binding.
    ///
    public static Map<String, IContinuationData> moreData() {
        if (STORAGE.isBound()) {
            return Collections.unmodifiableMap(STORAGE.get());
        } else {
            throw new ContinuationException(ERR_CANNOT_ADD_MORE_DATA);
        }
    }

    /// A convenient method to add a single “more data” entry for the specified key.
    /// If `moreData == null`, the value associated with `key` is removed.
    ///
    /// **WARNING:**
    /// This method mutates the scoped storage.
    /// Use with care when called from Virtual Threads created within the same bound context.
    ///
    /// **IMPORTANT:**
    /// 1. Throws a [ContinuationException] if the storage is not bound in the current context.
    /// 2. Replaces the existing value if one is already associated with the given key.
    ///
    /// @param key      the continuation key identifying the data entry
    /// @param moreData the data to associate with the key
    ///
    public static void putMoreData(final String key, final IContinuationData moreData) {
        if (STORAGE.isBound()) {
            if (moreData == null) {
                STORAGE.get().remove(key);
            }
            else {
                STORAGE.get().put(key, moreData);
            }
        } else {
            throw new ContinuationException(ERR_CANNOT_ADD_MORE_DATA);
        }
    }

    /// A convenient way to obtain “more data” associated with the specified key.
    /// Returns an empty optional if no corresponding data is found, or if the “need more data” storage is not bound
    /// in the current context — the latter case likely indicates a programming error.
    ///
    /// @param key the companion object property identifying the continuation
    /// @return an optional containing the requested data if found; otherwise, empty
    ///
    @SuppressWarnings("unchecked")
    public static <E extends IContinuationData> Optional<E> moreData(final String key) {
        if (STORAGE.isBound()) {
            return Optional.ofNullable((E) STORAGE.get().get(key));
        } else {
            LOGGER.error(WARN_UNBOUND_STORAGE);
            return Optional.empty();
        }
    }

    /// A predicate that determines whether a scoped storage is bound in the current context.
    ///
    public static boolean isBound() {
        return STORAGE.isBound();
    }

}
