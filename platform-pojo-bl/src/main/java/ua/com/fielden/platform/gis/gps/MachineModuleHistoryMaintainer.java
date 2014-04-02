package ua.com.fielden.platform.gis.gps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A singleton abstraction for maintaining module-machine associations.
 * 
 * @author TG Team
 * 
 * @param <MESSAGE>
 * @param <MACHINE>
 * @param <MODULE>
 */
public class MachineModuleHistoryMaintainer<MESSAGE extends AbstractAvlMessage, MACHINE extends AbstractAvlMachine<MESSAGE>, MODULE extends AbstractAvlModule /*, ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>*/> {
    private final Comparator<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> moduleAndDateComparator = new Comparator<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>>() {
        @Override
        public int compare(final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association1, final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association2) {
            final int moduleCompare = association1.getModule().compareTo(association2.getModule());
            if (moduleCompare != 0) {
                return moduleCompare;
            } else {
                return association1.getFrom().compareTo(association2.getFrom());
            }
        }
    };
    private final Comparator<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> machineAndDateComparator = new Comparator<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>>() {
        @Override
        public int compare(final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association1, final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association2) {
            final int machineCompare = association1.getMachine().compareTo(association2.getMachine());
            if (machineCompare != 0) {
                return machineCompare;
            } else {
                return association1.getFrom().compareTo(association2.getFrom());
            }
        }
    };
    private final List<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> moduleSortedAssociations;
    private final List<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> machineSortedAssociations;

    public MachineModuleHistoryMaintainer(final List<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> associations) {
        this.moduleSortedAssociations = new ArrayList<>(associations);
        Collections.sort(this.moduleSortedAssociations, moduleAndDateComparator);

        this.machineSortedAssociations = new ArrayList<>(associations);
        Collections.sort(this.machineSortedAssociations, machineAndDateComparator);
    }

    public Option<MACHINE> get(final MODULE module, final Date date) {
        final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> assoc = findContaining(date, module, moduleSortedAssociations, moduleAndDateComparator);
        return assoc == null ? new Option<MACHINE>(null) : new Option<MACHINE>(assoc.getMachine());
    }

    public Option<MODULE> get(final MACHINE machine, final Date date) {
        final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> assoc = findContaining(date, machine, machineSortedAssociations, machineAndDateComparator);
        return assoc == null ? new Option<MODULE>(null) : new Option<MODULE>(assoc.getModule());
    }

    private AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> createSampleModuleAssociation(final Date date, final MODULE module) {
        final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association = new AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>() {
            private static final long serialVersionUID = 2678157290073520879L;
            private MODULE module;

            @Override
            protected AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setModule(final MODULE value) {
                this.module = value;
                return this;
            }

            @Override
            public MODULE getModule() {
                return module;
            }

            @Override
            protected AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setMachine(final MACHINE machine) {
                return null;
            }

            @Override
            public MACHINE getMachine() {
                return null;
            }
        };
        association.setFrom(date);
        association.setMachine(null); // irrelevant for comparison
        association.setModule(module);
        return association;
    }

    private AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> createSampleMachineAssociation(final Date date, final MACHINE machine) {
        final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association = new AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>() {
            private static final long serialVersionUID = 2678157290073520879L;
            private MACHINE machine;

            @Override
            protected AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setMachine(final MACHINE value) {
                this.machine = value;
                return this;
            }

            @Override
            public MACHINE getMachine() {
                return machine;
            }

            @Override
            protected AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> setModule(final MODULE module) {
                return null;
            }

            @Override
            public MODULE getModule() {
                return null;
            }
        };
        association.setFrom(date);
        association.setMachine(machine);
        association.setModule(null); // irrelevant for comparison
        return association;
    }

    /**
     * Finds an association which fully contains the specified 'date'. Returns 'null' if there is no such association.
     * 
     * @param date
     * @param establishedIntervals
     * @return
     */
    protected AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> findContaining(final Date date, final MODULE module, final List<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> associations, final Comparator<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> comparator) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be 'null'.");
        }
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be 'null'.");
        }
        final int index = Collections.binarySearch(associations, createSampleModuleAssociation(date, module));

        final int foundAssociationIndex;
        if (index >= 0) {
            foundAssociationIndex = index;
        } else {
            final int i = (-index - 1 - 1);
            foundAssociationIndex = (i >= 0 && i <= associations.size() - 1) ? i : -1;
        }

        if (foundAssociationIndex < 0) {
            return null;
        } else {
            final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association = associations.get(foundAssociationIndex);
            return association.getModule().compareTo(module) == 0 && association.getFrom().getTime() <= date.getTime()
                    && (association.getTo() == null || date.getTime() < association.getTo().getTime()) ? association : null;
        }
    }

    /**
     * Finds an association which fully contains the specified 'date'. Returns 'null' if there is no such association.
     * 
     * @param date
     * @param establishedIntervals
     * @return
     */
    protected AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> findContaining(final Date date, final MACHINE machine, final List<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> associations, final Comparator<AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>> comparator) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be 'null'.");
        }
        if (machine == null) {
            throw new IllegalArgumentException("Machine cannot be 'null'.");
        }
        final int index = Collections.binarySearch(associations, createSampleMachineAssociation(date, machine));

        final int foundAssociationIndex;
        if (index >= 0) {
            foundAssociationIndex = index;
        } else {
            final int i = (-index - 1 - 1);
            foundAssociationIndex = (i >= 0 && i <= associations.size() - 1) ? i : -1;
        }

        if (foundAssociationIndex < 0) {
            return null;
        } else {
            final AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE> association = associations.get(foundAssociationIndex);
            return association.getMachine().compareTo(machine) == 0 && association.getFrom().getTime() <= date.getTime()
                    && (association.getTo() == null || date.getTime() < association.getTo().getTime()) ? association : null;
        }
    }
}
