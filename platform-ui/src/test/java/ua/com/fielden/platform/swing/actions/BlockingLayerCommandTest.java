package ua.com.fielden.platform.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.junit.Test;

import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;

/**
 * Test for {@link BlockingLayerCommand} for sequential execution and exception handling.
 * 
 * @author TG Team
 * 
 */
public class BlockingLayerCommandTest extends TestCase {
    /** Indicates if the sequence of multi-threaded actions performed. */
    private Boolean sequenceFinished = false, exceptionHandled = false;

    private class TestCommand extends BlockingLayerCommand<Void> {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final TestCommand[] commands;
        private final long millis;
        private final boolean turnOnIncrementalLocking, lastAction;
        /** 0 - no exception, 1 - preAction, 2 - action, 3 - postAction. */
        private final int exceptionStage;

        public TestCommand(final String name, final BlockingIndefiniteProgressLayer blockingLayer, final boolean turnOnIncrementalLocking, final boolean lastAction, final long millis, final int exceptionStage, final TestCommand... commands) {
            super(name, blockingLayer);
            this.name = name;
            this.commands = commands;
            this.millis = millis;
            this.turnOnIncrementalLocking = turnOnIncrementalLocking;
            this.lastAction = lastAction;
            this.exceptionStage = exceptionStage;
        }

        @Override
        protected boolean preAction() {
            System.out.println(getName() + ": preAction.");
            if (turnOnIncrementalLocking) {
                getProvider().getBlockingLayer().enableIncrementalLocking();
            }
            final boolean b = super.preAction();
            assertTrue("Pre-action : should be already blocked.", getProvider().getBlockingLayer().isLocked());
            assertTrue("Pre-action : Incremental Locking should be turned on.", getProvider().getBlockingLayer().isIncrementalLocking());

            if (exceptionStage == 1) {
                throw new RuntimeException("Pre-action exception.");
            }
            System.out.println("\t" + getName() + ": preAction done.");
            return b;
        }

        @Override
        protected Void action(final ActionEvent e) throws Exception {
            System.out.println(getName() + ": action.");
            Thread.sleep(millis);
            assertTrue("Action : should be blocked.", getProvider().getBlockingLayer().isLocked());
            assertTrue("Action : Incremental Locking should be turned on.", getProvider().getBlockingLayer().isIncrementalLocking());
            if (exceptionStage == 2) {
                throw new RuntimeException("Action exception.");
            }
            System.out.println("\t" + getName() + ": action done.");
            return null;
        }

        @Override
        protected void postAction(final Void value) {
            System.out.println(getName() + ": postAction.");
            assertTrue("Post-action : still should be blocked.", getProvider().getBlockingLayer().isLocked());
            assertTrue("Post-action : Incremental Locking still should be turned on.", getProvider().getBlockingLayer().isIncrementalLocking());

            for (final TestCommand tc : commands) {
                tc.actionPerformed(null);
            }
            super.postAction(value);
            if (exceptionStage == 3) {
                throw new RuntimeException("Post-action exception.");
            }
            System.out.println("\t" + getName() + ": postAction done.");
            if (lastAction) {
                assertFalse("Last command post-action : should be un-locked.", getProvider().getBlockingLayer().isLocked());
                assertFalse("Last command post-action : Incremental Locking should be turned off.", getProvider().getBlockingLayer().isIncrementalLocking());
                synchronized (BlockingLayerCommandTest.this) {
                    BlockingLayerCommandTest.this.notify();
                    sequenceFinished = true;
                }
            } else {
                assertTrue("Post-action : still should be blocked.", getProvider().getBlockingLayer().isLocked());
                assertTrue("Post-action : Incremental Locking still should be turned on.", getProvider().getBlockingLayer().isIncrementalLocking());
            }
        }

        @Override
        protected void handlePreAndPostActionException(final Throwable ex) {
            exceptionHandled = true;
            synchronized (BlockingLayerCommandTest.this) {
                BlockingLayerCommandTest.this.notify();
                sequenceFinished = true;
            }
        }

        public String getName() {
            return name;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sequenceFinished = false;
        exceptionHandled = false;
    }

    @Test
    public void test_sequential_execution_and_blocking() throws InterruptedException {
        final BlockingIndefiniteProgressLayer blockingLayer = new BlockingIndefiniteProgressLayer(new JPanel(), "Locking...");
        final TestCommand first = new TestCommand("First after Main (Shorter)", blockingLayer, false, false, 300, 0);
        final TestCommand sec = new TestCommand("Sec after Main (Longer)", blockingLayer, false, true, 500, 0);
        final TestCommand main = new TestCommand("Main", blockingLayer, true, false, 200, 0, first, sec);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                main.actionPerformed(null);
            }
        });
        // main.actionPerformed(null);

        // Multi-threading test should not be finished until last thread is completed!
        synchronized (this) {
            while (!sequenceFinished) {
                wait();
            }
        }
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_1() throws InterruptedException {
        exception_handling_test(1, 1);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_2() throws InterruptedException {
        exception_handling_test(2, 1);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_3() throws InterruptedException {
        exception_handling_test(3, 1);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_4() throws InterruptedException {
        exception_handling_test(1, 2);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_5() throws InterruptedException {
        exception_handling_test(2, 2);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_6() throws InterruptedException {
        exception_handling_test(3, 2);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_7() throws InterruptedException {
        exception_handling_test(1, 3);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_8() throws InterruptedException {
        exception_handling_test(2, 3);
    }

    @Test
    public void test_exception_handling_at_all_stages_of_sequential_execution_9() throws InterruptedException {
        exception_handling_test(3, 3);
    }

    private void exception_handling_test(final int exceptionStage, final int commandNumber) throws InterruptedException {
        final BlockingIndefiniteProgressLayer blockingLayer = new BlockingIndefiniteProgressLayer(new JPanel(), "Locking...");
        final TestCommand first = new TestCommand("First after Main (Shorter)", blockingLayer, false, false, 300, commandNumber == 2 ? exceptionStage : 0);
        final TestCommand sec = new TestCommand("Sec after Main (Longer)", blockingLayer, false, true, 500, commandNumber == 3 ? exceptionStage : 0);
        final TestCommand main = new TestCommand("Main", blockingLayer, true, false, 200, commandNumber == 1 ? exceptionStage : 0, first, sec);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                main.actionPerformed(null);
            }
        });
        // main.actionPerformed(null);

        // Multi-threading test should not be finished until last thread is completed!
        synchronized (this) {
            while (!sequenceFinished) {
                wait();
            }
            assertTrue("Exception should be handled.", exceptionHandled);
        }
    }

    public void test_parallel_execution_of_couple_of_commands_for_different_blocking_layers() throws InterruptedException {
        System.out.println("==============test_parallel_execution_of_couple_of_commands_for_different_blocking_layers================");
        final BlockingIndefiniteProgressLayer blockingLayer1 = new BlockingIndefiniteProgressLayer(new JPanel(), "Locking 1...");
        final BlockingIndefiniteProgressLayer blockingLayer2 = new BlockingIndefiniteProgressLayer(new JPanel(), "Locking 2...");
        final BlockingIndefiniteProgressLayer blockingLayer3 = new BlockingIndefiniteProgressLayer(new JPanel(), "Locking 3...");
        final TestCommand first = new TestCommand("First (Longer)", blockingLayer1, true, true, 3000, 0);
        final TestCommand sec = new TestCommand("Sec (Shorter)", blockingLayer2, true, false, 300, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void postAction(final Void value) {
                super.postAction(value);
                assertFalse(getName() + " post-action : should be un-locked!", getProvider().getBlockingLayer().isLocked());
                assertFalse(getName() + " post-action : incremental Locking should be turned off.", getProvider().getBlockingLayer().isIncrementalLocking());
            }
        };
        final TestCommand third = new TestCommand("Third (Middle)", blockingLayer3, true, false, 900, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void postAction(final Void value) {
                super.postAction(value);
                assertFalse(getName() + " post-action : should be un-locked!", getProvider().getBlockingLayer().isLocked());
                assertFalse(getName() + " post-action : incremental Locking should be turned off.", getProvider().getBlockingLayer().isIncrementalLocking());
            }
        };

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                first.actionPerformed(null);
                sec.actionPerformed(null);
                third.actionPerformed(null);
            }
        });

        // Multi-threading test should not be finished until last thread is completed!
        synchronized (this) {
            while (!sequenceFinished) {
                wait();
            }
        }
    }

}