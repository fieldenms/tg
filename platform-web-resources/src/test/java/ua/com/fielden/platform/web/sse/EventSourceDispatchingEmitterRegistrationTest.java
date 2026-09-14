package ua.com.fielden.platform.web.sse;

import org.junit.Test;
import ua.com.fielden.platform.security.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.synchronizedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.web.sse.EventSourceDispatchingEmitter.APP_VERSION_EVENT_NAME;

/// Tests for registering emitters with [EventSourceDispatchingEmitter].
/// They also cover the application version announcement, which registering a new emitter triggers.
///
/// Both the announcement and the resolution of the version run off the calling thread, hence the waiting helpers below.
/// Every dispatching emitter created here shortens the retry delay, so that five failing attempts do not take minutes.
///
public class EventSourceDispatchingEmitterRegistrationTest {

    private static final User USER = newPlainEntity(User.class, 1L);

    private static final String UID = "sse-uid-1", ANOTHER_UID = "sse-uid-2";

    private static final String VERSION = "Application (2026-09-11)", RECOVERED_VERSION = "Application (2026-09-12)";

    private static final String
        ERR_NO_VERSION = "The application version is unavailable.",
        ERR_NO_VERSION_YET = "The application version is not available yet.";

    /// The delay between resolution attempts, short enough for five of them to complete promptly.
    ///
    private static final long RETRY_DELAY_MILLIS = 5;

    /// How long to wait for asynchronous work that is expected to happen.
    ///
    private static final long AWAIT_TIMEOUT_MILLIS = 5_000;

    /// How long to wait before asserting that something has not happened.
    ///
    private static final long SETTLE_MILLIS = 200;

    @Test
    public void active_dispatcher_registers_emitter_and_makes_it_available() {
        final var dispatcher = dispatcherWith(() -> VERSION);
        final var emitter = new RecordingEmitter();

        final var result = dispatcher.registerEmitter(USER, UID, () -> emitter);

        assertTrue("Registration with an active dispatcher should succeed.", result.isSuccessful());
        assertSame("The registered emitter should be returned.", emitter, result.getInstance());
        assertSame("The registered emitter should be in the register.", emitter, dispatcher.getEmitter(USER, UID));
    }

    @Test
    public void inactive_dispatcher_fails_to_register_emitter() {
        final var dispatcher = dispatcherWith(() -> VERSION);
        dispatcher.close();

        final var result = dispatcher.registerEmitter(USER, UID, RecordingEmitter::new);

        assertFalse("Registration with an inactive dispatcher should fail.", result.isSuccessful());
        assertNull("Nothing should be in the register.", dispatcher.getEmitter(USER, UID));
    }

    @Test
    public void duplicate_registration_returns_the_already_registered_emitter() {
        final var dispatcher = dispatcherWith(() -> VERSION);
        final var first = new RecordingEmitter();
        final var second = new RecordingEmitter();
        register(dispatcher, UID, first);

        final var result = dispatcher.registerEmitter(USER, UID, () -> second);

        assertTrue("A duplicate registration should still succeed.", result.isSuccessful());
        assertSame("The emitter registered first should be returned.", first, result.getInstance());
        assertSame("The emitter registered first should remain in the register.", first, dispatcher.getEmitter(USER, UID));
    }

    @Test
    public void duplicate_registration_does_not_announce_the_application_version_again() {
        final var dispatcher = dispatcherWith(() -> VERSION);
        final var first = new RecordingEmitter();
        register(dispatcher, UID, first);
        assertAnnounced(first, VERSION);

        register(dispatcher, UID, new RecordingEmitter());
        settle();

        assertEquals("A duplicate registration should not announce again.", 1, first.announcements.size());
    }

    @Test
    public void every_new_emitter_is_announced_the_application_version() {
        final var dispatcher = dispatcherWith(() -> VERSION);
        final var first = new RecordingEmitter();
        final var second = new RecordingEmitter();

        register(dispatcher, UID, first);
        register(dispatcher, ANOTHER_UID, second);

        assertAnnounced(first, VERSION);
        assertAnnounced(second, VERSION);
    }

    @Test
    public void emitter_registered_while_the_version_is_unresolved_is_announced_once_it_resolves() {
        final var resolutions = new AtomicInteger();
        final var dispatcher = dispatcherResolving(resolutions, VERSION);
        final var emitter = new RecordingEmitter();

        register(dispatcher, UID, emitter);

        assertAnnounced(emitter, VERSION);
        assertEquals("The version should have been resolved once.", 1, resolutions.get());
    }

    @Test
    public void emitter_registered_after_the_version_is_resolved_is_announced_without_resolving_again() {
        final var resolutions = new AtomicInteger();
        final var dispatcher = dispatcherResolving(resolutions, VERSION);
        final var first = new RecordingEmitter();
        register(dispatcher, UID, first);
        assertAnnounced(first, VERSION);

        final var second = new RecordingEmitter();
        register(dispatcher, ANOTHER_UID, second);

        assertAnnounced(second, VERSION);
        assertEquals("The version should not have been resolved again.", 1, resolutions.get());
    }

    @Test
    public void emitter_that_fails_to_receive_the_announcement_becomes_deregistered() {
        final var dispatcher = dispatcherWith(() -> VERSION);
        final var emitter = new FailingEmitter();
        emitter.onClose = () -> dispatcher.deregisterEmitter(USER, UID);

        final var result = dispatcher.registerEmitter(USER, UID, () -> emitter);

        assertTrue("Registration should succeed even though the announcement fails.", result.isSuccessful());
        awaitUntil("the failing emitter leaves the register", () -> dispatcher.getEmitter(USER, UID) == null);
        assertTrue("The failing emitter should have closed itself.", emitter.closed);
    }

    @Test
    public void registration_succeeds_when_the_version_supplier_throws() {
        final var dispatcher = dispatcherWith(() -> {
            throw new IllegalStateException(ERR_NO_VERSION);
        });
        final var emitter = new RecordingEmitter();

        final var result = dispatcher.registerEmitter(USER, UID, () -> emitter);

        assertTrue("A throwing supplier should not prevent registration.", result.isSuccessful());
        assertSame("The emitter should be in the register.", emitter, dispatcher.getEmitter(USER, UID));
        settle();
        assertTrue("Nothing should be announced.", emitter.announcements.isEmpty());
    }

    @Test
    public void failing_resolution_is_attempted_five_times_and_then_stops() {
        final var resolutions = new AtomicInteger();
        final var dispatcher = dispatcherFailingToResolve(resolutions);

        register(dispatcher, UID, new RecordingEmitter());

        awaitUntil("all five attempts are made", () -> resolutions.get() == 5);
        settle();
        assertEquals("There should be no attempt beyond the fifth.", 5, resolutions.get());
    }

    @Test
    public void resolution_that_succeeds_after_failures_announces_to_all_registered_emitters() {
        final var resolutions = new AtomicInteger();
        final var dispatcher = dispatcherWith(() -> {
            if (resolutions.incrementAndGet() < 3) {
                throw new IllegalStateException(ERR_NO_VERSION_YET);
            }
            return RECOVERED_VERSION;
        });
        final var first = new RecordingEmitter();
        final var second = new RecordingEmitter();

        register(dispatcher, UID, first);
        register(dispatcher, ANOTHER_UID, second);

        assertAnnounced(first, RECOVERED_VERSION);
        assertAnnounced(second, RECOVERED_VERSION);
    }

    @Test
    public void blank_version_is_not_announced_and_is_not_resolved_again() {
        final var resolutions = new AtomicInteger();
        final var dispatcher = dispatcherResolving(resolutions, "");
        final var first = new RecordingEmitter();
        register(dispatcher, UID, first);
        awaitUntil("the version is resolved", () -> resolutions.get() == 1);

        final var second = new RecordingEmitter();
        register(dispatcher, ANOTHER_UID, second);
        settle();

        assertEquals("A blank version should not be resolved again.", 1, resolutions.get());
        assertTrue("Nothing should be announced to the first emitter.", first.announcements.isEmpty());
        assertTrue("Nothing should be announced to the second emitter.", second.announcements.isEmpty());
    }

    /// Creates a dispatching emitter whose retry delay is short enough for a test.
    ///
    private static EventSourceDispatchingEmitter dispatcherWith(final Supplier<String> appVersionSupplier) {
        return new EventSourceDispatchingEmitter(appVersionSupplier) {
            @Override
            long appVersionRetryDelayMillis() {
                return RETRY_DELAY_MILLIS;
            }
        };
    }

    /// Creates a dispatching emitter that resolves to `version`, counting the attempts in `resolutions`.
    ///
    private static EventSourceDispatchingEmitter dispatcherResolving(final AtomicInteger resolutions, final String version) {
        return dispatcherWith(() -> {
            resolutions.incrementAndGet();
            return version;
        });
    }

    /// Creates a dispatching emitter whose resolution always fails, counting the attempts in `resolutions`.
    ///
    private static EventSourceDispatchingEmitter dispatcherFailingToResolve(final AtomicInteger resolutions) {
        return dispatcherWith(() -> {
            resolutions.incrementAndGet();
            throw new IllegalStateException(ERR_NO_VERSION);
        });
    }

    /// Waits for `emitter` to be announced to, and asserts that the announcement carries `version`.
    ///
    private static void assertAnnounced(final RecordingEmitter emitter, final String version) {
        awaitUntil("the emitter is announced to", () -> emitter.announcements.size() == 1);
        assertEquals(new Announcement(APP_VERSION_EVENT_NAME, version), emitter.announcements.getFirst());
    }

    /// Registers `emitter` for `uid`, asserting that registration succeeded.
    /// This also consumes the returned result, which must not be silently discarded.
    ///
    private static void register(final EventSourceDispatchingEmitter dispatcher, final String uid, final IEventSourceEmitter emitter) {
        assertTrue("Registration should succeed.", dispatcher.registerEmitter(USER, uid, () -> emitter).isSuccessful());
    }

    /// Waits until `condition` holds, failing the test if it does not hold in time.
    ///
    private static void awaitUntil(final String description, final BooleanSupplier condition) {
        final var deadline = currentTimeMillis() + AWAIT_TIMEOUT_MILLIS;
        while (currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            sleepFor(2);
        }
        fail("Timed out waiting until %s.".formatted(description));
    }

    /// Gives pending asynchronous work a chance to run, before asserting that something has not happened.
    ///
    private static void settle() {
        sleepFor(SETTLE_MILLIS);
    }

    /// Sleeps for `millis`, failing the test if interrupted.
    ///
    private static void sleepFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting.");
        }
    }

    /// One event captured by [RecordingEmitter].
    ///
    private record Announcement(String name, String data) {}

    /// A stand-in for [EventSourceEmitter] that records what it was sent.
    ///
    private static class RecordingEmitter implements IEventSourceEmitter {

        final List<Announcement> announcements = synchronizedList(new ArrayList<>());

        volatile boolean closed = false;

        volatile Runnable onClose = () -> {};

        @Override
        public void event(final String name, final String data) {
            announcements.add(new Announcement(name, data));
        }

        @Override
        public void data(final String data) {
        }

        @Override
        public void comment(final String comment) {
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                onClose.run();
            }
        }

    }

    /// A stand-in for an emitter whose connection is already broken.
    /// [EventSourceEmitter] swallows a write failure and closes itself, which deregisters it, and so does this.
    ///
    private static final class FailingEmitter extends RecordingEmitter {

        @Override
        public void event(final String name, final String data) {
            close();
        }

    }

}
