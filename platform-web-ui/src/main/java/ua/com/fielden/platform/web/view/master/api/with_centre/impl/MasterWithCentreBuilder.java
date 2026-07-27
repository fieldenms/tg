package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.IMasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.api.helpers.IComplete;
import ua.com.fielden.platform.web.view.master.api.with_centre.IMasterWithCentre0;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class MasterWithCentreBuilder<T extends AbstractFunctionalEntityWithCentreContext<?>> implements IMasterWithCentreBuilder<T>, IMasterWithCentre0<T>, IComplete<T> {

    private Class<T> type;
    private EntityCentre<?> entityCentre;
    private boolean saveOnActivate = false;
    private Optional<JsCode> customCode = empty();
    private Optional<JsCode> customCodeOnAttach = empty();
    private Optional<JsCode> customImports = empty();

    @Override
    public IMasterWithCentre0<T> forEntityWithSaveOnActivate(final Class<T> type) {
        this.type = type;
        this.saveOnActivate = true;
        return this;
    }

    @Override
    public IComplete<T> withCentre(final EntityCentre<?> entityCentre) {
        if (!entityCentre.isRunAutomatically()) {
            throw new EntityMasterConfigurationException(format("Master for [%s] is misconfigured: entity centre for [%s] is missing 'run-automatically' configuration option.", type.getSimpleName(), entityCentre.getEntityType().getSimpleName()));
        }
        this.entityCentre = entityCentre;
        return this;
    }

    @Override
    public IMaster<T> done() {
        if (entityCentre != null) {
            return new MasterWithCentre<>(type, saveOnActivate, entityCentre, customCode, customCodeOnAttach, customImports);
        } else {
            return new MasterWithPolymorphicCentre<>(type, saveOnActivate, customCode, customCodeOnAttach, customImports);
        }
    }
    
    
    /**
     * Injects custom JavaScript code into respective master implementation. This code will be executed after 
     * master component creation.
     * 
     * @param customCode
     * @return
     */
    public MasterWithCentreBuilder<T> injectCustomCode(final JsCode customCode) {
        this.customCode = Optional.of(customCode);
        return this;
    }
    
    /**
     * Injects custom JavaScript code into respective master implementation. This code will be executed every time
     * master component is attached to client application's DOM.
     * 
     * @param customCode
     * @return
     */
    public MasterWithCentreBuilder<T> injectCustomCodeOnAttach(final JsCode customCode) {
        this.customCodeOnAttach = Optional.of(customCode);
        return this;
    }

    /**
     * Injects custom JavaScript imports into respective master implementation.
     *
     * @param customImports
     * @return
     */
    public MasterWithCentreBuilder<T> injectCustomImports(final JsCode customImports) {
        this.customImports = of(customImports);
        return this;
    }

}