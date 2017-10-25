package fielden.close_leave.producers;

import static ua.com.fielden.platform.error.Result.failure;

import com.google.inject.Inject;

import fielden.close_leave.OpenTgCloseLeaveExampleMasterAction;
import fielden.close_leave.TgCloseLeaveExample;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/** 
 * A producer for new instances of entity {@link OpenTgCloseLeaveExampleMasterAction}.
 * 
 * @author Developers
 *
 */
public class OpenTgCloseLeaveExampleMasterActionProducer extends DefaultEntityProducerWithContext<OpenTgCloseLeaveExampleMasterAction> {

    @Inject
    public OpenTgCloseLeaveExampleMasterActionProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, OpenTgCloseLeaveExampleMasterAction.class, coFinder);
    }

    @Override
    protected OpenTgCloseLeaveExampleMasterAction provideDefaultValues(final OpenTgCloseLeaveExampleMasterAction entity) {
        if (currentEntityInstanceOf(TgCloseLeaveExample.class) && chosenPropertyRepresentsThisColumn()) {
            // edit current TgCloseLeaveExample row by clicking 'this' column on TgCloseLeaveExample centre
            entity.setKey(refetch(currentEntity(TgCloseLeaveExample.class), "key"));
        } else if (currentEntityInstanceOf(TgCloseLeaveExample.class) && chosenPropertyEmpty()) {
            // primary action on TgCloseLeaveExample centre (edit entity)
            entity.setKey(refetch(currentEntity(TgCloseLeaveExample.class), "key"));
        } else if (currentEntityNotEmpty() && chosenPropertyNotEmpty()) {
            // click on property representing TgCloseLeaveExample to edit it: this could be TgCloseLeaveExample centre or any other
            final TgCloseLeaveExample clickedTgCloseLeaveExample = currentEntity().get(chosenProperty()); // this must be TgCloseLeaveExample instance, otherwise this is a developer error, no need to check this one more time
            if (clickedTgCloseLeaveExample != null) {
                entity.setKey(refetch(clickedTgCloseLeaveExample, "key"));
            } else {
                throw failure("There is no TgCloseLeaveExample to open.");
            }
        } else if (masterEntityEmpty() && selectedEntitiesEmpty()) {
            // '+' action on TgCloseLeaveExample centre
            entity.setKey(co(TgCloseLeaveExample.class).new_());
        } else {
            // it is recommended to throw 'unsupported case' exception otherwise
            throw failure("Not supported yet.");
        }
        return entity;
    }

}