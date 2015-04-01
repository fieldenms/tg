package ua.com.fielden.platform.web.resources.webui;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for criteria serves as a back-end mechanism of centre management. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>save centre -- POST request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class CentreResource<CRITERIA_TYPE extends AbstractEntity<?>> extends ServerResource {
    private final static Logger logger = Logger.getLogger(CentreResource.class);

    private final RestServerUtil restUtil;

    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final IGlobalDomainTreeManager gdtm;

    public CentreResource(
            final RestServerUtil restUtil,

            final EntityCentre centre,
            final IGlobalDomainTreeManager gdtm,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;

        miType = centre.getMenuItemType();
        this.gdtm = gdtm;
    }

    /**
     * Handles POST request resulting from tg-entity-centre <code>save()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final ICentreDomainTreeManagerAndEnhancer freshCentre = gdtm.getEntityCentreManager(miType, CriteriaResource.FRESH_CENTRE_NAME);
        ((GlobalDomainTreeManager) gdtm).removeCentre(miType, CriteriaResource.FRESH_CENTRE_NAME);

        final ICentreDomainTreeManagerAndEnhancer principleCentre = CriteriaResource.getCurrentPrincipleCentreManager(gdtm, miType);

        final ICentreDomainTreeManagerAndEnhancer differenciesCentre = createDiff(freshCentre, principleCentre, CriteriaResource.getEntityType(miType));

        ((GlobalDomainTreeManager) gdtm).overrideCentre(miType, CriteriaResource.DIFFERENCIES_CENTRE_NAME, differenciesCentre);
        gdtm.saveEntityCentreManager(miType, CriteriaResource.DIFFERENCIES_CENTRE_NAME);

        return restUtil.rawListJSONRepresentation("OK");
    }

    /**
     * Handles DELETE request resulting from tg-entity-centre <code>discard()</code> method.
     */
    @Delete
    @Override
    public Representation delete() {
        if (gdtm.isChangedEntityCentreManager(miType, CriteriaResource.FRESH_CENTRE_NAME)) {
            gdtm.discardEntityCentreManager(miType, CriteriaResource.FRESH_CENTRE_NAME);
        } else {
            final String message = "Can not discard the centre that was not changed.";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        return restUtil.resultJSONRepresentation(Result.successful(null));
    }

    private ICentreDomainTreeManagerAndEnhancer createDiff(final ICentreDomainTreeManagerAndEnhancer freshCentre, final ICentreDomainTreeManagerAndEnhancer principleCentre, final Class<AbstractEntity<?>> root) {
        final ICentreDomainTreeManagerAndEnhancer differenciesCentre = ((GlobalDomainTreeManager) gdtm).copyCentre(freshCentre);

        for (final String property : differenciesCentre.getFirstTick().checkedProperties(root)) {
            if (AbstractDomainTree.isDoubleCriterion(CriteriaResource.managedType(root, differenciesCentre), property)) {
                if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getExclusive(root, property), principleCentre.getFirstTick().getExclusive(root, property))) {
                    differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE, root, property);
                }
                if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getExclusive2(root, property), principleCentre.getFirstTick().getExclusive2(root, property))) {
                    differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property);
                }
            }
            final Class<?> propertyType = StringUtils.isEmpty(property) ? CriteriaResource.managedType(root, differenciesCentre) : PropertyTypeDeterminator.determinePropertyType(CriteriaResource.managedType(root, differenciesCentre), property);
            if (EntityUtils.isDate(propertyType)) {
                if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getDatePrefix(root, property), principleCentre.getFirstTick().getDatePrefix(root, property))) {
                    differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.DATE_PREFIX, root, property);
                }
                if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getDateMnemonic(root, property), principleCentre.getFirstTick().getDateMnemonic(root, property))) {
                    differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.DATE_MNEMONIC, root, property);
                }
                if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getAndBefore(root, property), principleCentre.getFirstTick().getAndBefore(root, property))) {
                    differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.AND_BEFORE, root, property);
                }
            }

            if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getOrNull(root, property), principleCentre.getFirstTick().getOrNull(root, property))) {
                differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.OR_NULL, root, property);
            }
            if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getNot(root, property), principleCentre.getFirstTick().getNot(root, property))) {
                differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.NOT, root, property);
            }

            if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getValue(root, property), principleCentre.getFirstTick().getValue(root, property))) {
                differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.VALUE, root, property);
            }
            if (AbstractDomainTree.isDoubleCriterionOrBoolean(CriteriaResource.managedType(root, differenciesCentre), property)) {
                if (!EntityUtils.equalsEx(differenciesCentre.getFirstTick().getValue2(root, property), principleCentre.getFirstTick().getValue2(root, property))) {
                    differenciesCentre.getFirstTick().markMetaValuePresent(MetaValueType.VALUE2, root, property);
                }
            }
        }

        return differenciesCentre;
    }
}
