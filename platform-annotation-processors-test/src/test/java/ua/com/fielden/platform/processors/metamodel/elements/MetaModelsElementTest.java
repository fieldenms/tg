package ua.com.fielden.platform.processors.metamodel.elements;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;
import ua.com.fielden.platform.security.user.User;

import javax.lang.model.element.TypeElement;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

/**
 * A test case for {@link MetaModelsElement}.
 *
 * @author TG Team
 *
 */
public class MetaModelsElementTest {

    @ClassRule
    public static ProcessingRule rule = new ProcessingRule();
    private static MetaModelFinder metaModelFinder;
    private static MetaModelsElement metaModelsElement;

    @BeforeClass
    public static void setupOnce() {
        metaModelFinder = new MetaModelFinder(rule.getProcessingEnvironment());
        final TypeElement typeElement = rule.getElements().getTypeElement(MetaModels.class.getCanonicalName());
        metaModelsElement = new MetaModelsElement(typeElement, metaModelFinder.streamMetaModels(typeElement).iterator());
    }

    @Test
    public void construction_of_meta_models_element_ignores_non_meta_model_fields() {
        assertEquals(Set.of(AttachmentMetaModel.class.getSimpleName(), UserMetaModel.class.getSimpleName()), metaModelsElement.getMetaModels().stream().map(mme -> mme.getSimpleName().toString()).collect(toSet()));
    }

    /**
     * A type for testing purposes. Represents entry point for meta-models.
     */
    public static final class MetaModels {
        public static final AttachmentMetaModel Attachment_ = new AttachmentMetaModel();
        public static final UserMetaModel User_ = new UserMetaModel();
        public static final String someStringField = "";
        public static final int someIntField = 0;
    }

    /**
     * A test meta-model for entity {@link Attachment}.
     */
    public static class AttachmentMetaModel extends EntityMetaModel {
        @Override
        public Class<Attachment> getEntityClass() {
            return Attachment.class;
        }
    }

    /**
     * A test meta-model for entity {@link User}.
     */
    public static class UserMetaModel extends EntityMetaModel {
        @Override
        public Class<User> getEntityClass() {
            return User.class;
        }
    }

}
