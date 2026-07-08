package ua.com.fielden.platform.web.resources.webui;

import org.junit.Test;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToResultTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action1;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.lang.System.currentTimeMillis;
import static java.util.Set.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.refreshAndEvictDynamicEntries;

/// Unit test for [CriteriaResource#refreshAndEvictDynamicEntries].
/// Exercises the full decision matrix of the eviction sweep — active key, stale orphan, fresh orphan, the no-op fast path, and bump throttling.
///
public class RefreshAndEvictDynamicEntriesTest {

    private static final Class<? extends AbstractEntity<?>> ROOT = Action1.class;
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    /// Three persisted dynamic widths on a single `AddToResultTickManager`:
    /// - A — active in this run, `lastSeen` 100 days ago (well past the 30-day threshold).
    /// - B — orphan (not in `usedDynamicKeys`), `lastSeen` 31 days ago — should be evicted.
    /// - C — orphan, `lastSeen` 5 days ago — within the 30-day window, kept.
    ///
    /// After the sweep: A's `lastSeen` is bumped to "now" (proving active keys never age out via the sweep),
    /// B is removed from all three dynamic maps, C is untouched.
    /// The recording silent-tick-adjuster is invoked exactly once.
    ///
    /// Then a second sweep is run with no `usedDynamicKeys`.
    /// The remaining persisted keys (A bumped, C still fresh) are not stale, so the silent-tick-adjuster must not be invoked at all — proving the "nothing to do → no I/O" contract.
    ///
    @Test
    public void evicts_orphan_after_30_days_keeps_active_keys_alive_no_io_when_nothing_to_do() {
        final var tick = new AddToResultTickManager();
        final var nowMillis = currentTimeMillis();

        // A: active key, lastSeen 100 days ago — should be bumped, not evicted (active wins over staleness).
        tick.setDynamicWidth(ROOT, "A", 100);
        tick.setDynamicLastSeen(ROOT, "A", nowMillis - 100 * DAY_MS);

        // B: orphan, lastSeen 31 days ago — past the 30-day threshold, should be evicted.
        tick.setDynamicWidth(ROOT, "B", 200);
        tick.setDynamicLastSeen(ROOT, "B", nowMillis - 31 * DAY_MS);

        // C: orphan, lastSeen 5 days ago — within the 30-day window, kept.
        tick.setDynamicWidth(ROOT, "C", 300);
        tick.setDynamicLastSeen(ROOT, "C", nowMillis - 5 * DAY_MS);

        // Recording silent adjuster that applies the mutation directly to the test tick.
        final var invocations = new AtomicInteger();
        final Consumer<Consumer<IAddToResultTickManager>> silentTickAdjuster = consumer -> {
            invocations.incrementAndGet();
            consumer.accept(tick);
        };

        // First sweep: simulate emission with "A" as the actively overridden key.
        refreshAndEvictDynamicEntries(tick, ROOT, silentTickAdjuster, of("A"));

        // Exactly one invocation — the sweep is supposed to persist once when there is anything to do.
        assertEquals(1, invocations.get());

        // A's width preserved, lastSeen bumped to roughly "now".
        assertEquals(Optional.of(100), tick.getDynamicWidth(ROOT, "A"));
        assertTrue(
            "A's lastSeen should have been bumped to near 'now'",
            tick.getDynamicLastSeen(ROOT, "A").orElseThrow() >= nowMillis - 1000
        );

        // B fully evicted from all three dynamic maps.
        assertEquals(Optional.empty(), tick.getDynamicWidth(ROOT, "B"));
        assertEquals(Optional.empty(), tick.getDynamicGrowFactor(ROOT, "B"));
        assertEquals(Optional.empty(), tick.getDynamicLastSeen(ROOT, "B"));

        // C unchanged — within the 30-day window.
        assertEquals(Optional.of(300), tick.getDynamicWidth(ROOT, "C"));
        assertEquals(Optional.of(nowMillis - 5 * DAY_MS), tick.getDynamicLastSeen(ROOT, "C"));

        // Second sweep: no overrides this run. Remaining keys (A bumped to "now", C still 5 days ago) are not stale.
        // Nothing to bump, nothing to evict — the silent-tick-adjuster must not fire.
        invocations.set(0);
        refreshAndEvictDynamicEntries(tick, ROOT, silentTickAdjuster, of());

        assertEquals(0, invocations.get());

        // Third sweep: "A" is used again, but its lastSeen was just bumped — well within the bump interval.
        // The throttle must suppress the re-stamp, so the silent-tick-adjuster must not fire.
        refreshAndEvictDynamicEntries(tick, ROOT, silentTickAdjuster, of("A"));

        assertEquals(0, invocations.get());
    }

    /// Bump throttling and healing:
    /// - a used key with a fresh `lastSeen` (within the one-day bump interval) must not trigger any persistence;
    /// - a used key with a persisted width but no recorded `lastSeen` must be stamped once, becoming evictable later.
    ///
    @Test
    public void bump_is_throttled_by_interval_and_stamps_missing_lastSeen() {
        final var tick = new AddToResultTickManager();
        final var nowMillis = currentTimeMillis();

        // D: used key, lastSeen one hour ago — within the bump interval, must not be re-stamped.
        final long dLastSeen = nowMillis - DAY_MS / 24;
        tick.setDynamicWidth(ROOT, "D", 100);
        tick.setDynamicLastSeen(ROOT, "D", dLastSeen);

        // E: used key with a width but no lastSeen — must be healed with a fresh stamp.
        tick.setDynamicWidth(ROOT, "E", 200);

        final var invocations = new AtomicInteger();
        final Consumer<Consumer<IAddToResultTickManager>> silentTickAdjuster = consumer -> {
            invocations.incrementAndGet();
            consumer.accept(tick);
        };

        refreshAndEvictDynamicEntries(tick, ROOT, silentTickAdjuster, of("D", "E"));

        // Exactly one persistence pass — solely for E's healing; D must remain untouched.
        assertEquals(1, invocations.get());
        assertEquals(Optional.of(dLastSeen), tick.getDynamicLastSeen(ROOT, "D"));
        assertTrue(
            "E's lastSeen should have been stamped to near 'now'",
            tick.getDynamicLastSeen(ROOT, "E").orElseThrow() >= nowMillis - 1000
        );

        // Second sweep with the same used keys: both are now within the bump interval — no persistence at all.
        invocations.set(0);
        refreshAndEvictDynamicEntries(tick, ROOT, silentTickAdjuster, of("D", "E"));

        assertEquals(0, invocations.get());
    }

}
