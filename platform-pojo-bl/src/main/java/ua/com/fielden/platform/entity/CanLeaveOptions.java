package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/// Represents the buttons available in the confirmation dialog shown when the `canLeave` method of an entity master is invoked.
///
@KeyType(String.class)
@CompanionObject(CanLeaveOptionsCo.class)
public class CanLeaveOptions extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(CanLeaveOptions.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    /// Represents the available sets of confirmation dialog options.
    ///
    public enum Options {
        /// This option applies when the underlying entity cannot be modified
        /// (e.g., a master with a center), but the business logic determines that
        /// leaving the master is premature and the user must complete certain actions
        /// before exiting. In this case, two buttons with specific roles are required:
        ///
        /// - `Yes` — indicates that the user wants to leave the entity master,
        ///           regardless of the reason returned by [ICustomisableCanLeave#cannotLeaveReason()].
        /// - `No`  — indicates that the user does not want to leave the entity master.
        ///
        YES_NO,

        /// This option applies when the underlying entity has unsaved changes.
        /// In this case, three buttons with specific roles are required:
        ///
        /// - `Yes`    — indicates that the user wants to leave the entity master and save any changes to the underlying entity.
        /// - `No`     — indicates that the user wants to leave the entity master without saving any changes to the underlying entity.
        /// - `Cancel` — indicates that the user does not want to leave the entity master in order to review the changes.
        ///
        YES_NO_CANCEL;

        /// Returns a corresponding `CanLeaveOptions` instance for this enum value.
        ///
        public CanLeaveOptions getCanLeaveOptions() {
            return (CanLeaveOptions) new CanLeaveOptions().setKey(this.name());
        }
    }
}
