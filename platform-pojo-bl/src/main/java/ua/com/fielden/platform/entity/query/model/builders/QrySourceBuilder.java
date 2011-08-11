package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsEntity;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QrySourceBuilder extends AbstractTokensBuilder {

    protected QrySourceBuilder(final AbstractTokensBuilder parent) {
	super(parent);
    }

    private boolean isEntityTypeAsSourceTest() {
	return getSize() == 2 && TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE.equals(firstCat()) && TokenCategory.QRY_SOURCE_ALIAS.equals(secondCat());
    }

    private boolean isEntityModelsAsSourceTest() {
	return getSize() == 2 && TokenCategory.QRY_MODELS_AS_QRY_SOURCE.equals(firstCat()) && TokenCategory.QRY_SOURCE_ALIAS.equals(secondCat());
    }

    private boolean isEntityModelAsSourceTest() {
	return getSize() == 2 && TokenCategory.QRY_MODEL_AS_QRY_SOURCE.equals(firstCat()) && TokenCategory.QRY_SOURCE_ALIAS.equals(secondCat());
    }

    @Override
    public boolean isClosing() {
	return isEntityTypeAsSourceTest() || isEntityModelsAsSourceTest() || isEntityModelAsSourceTest();
    }

    private Pair<TokenCategory, Object> getResultForEntityTypeAsSource() {
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_SOURCE, new EntQuerySourceAsEntity((Class) firstValue(), (String) secondValue()));
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (isEntityTypeAsSourceTest()) {
	    return getResultForEntityTypeAsSource();
	} else {
	    throw new RuntimeException("Not implemented yet");
	}
    }
}
