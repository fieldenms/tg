package ua.com.fielden.platform.processors.metamodel.elements;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.lang.model.element.TypeElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.testing.compile.CompilationRule;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.security.user.User;

/**
 * A test case for {@link MetaModelsElement}.
 *
 * @author TG Team
 *
 */
public class MetaModelsElementTest {

    public @Rule CompilationRule rule = new CompilationRule();
    private MetaModelFinder metaModelFinder;
    private MetaModelsElement metaModelsElement;
    
    @Before
    public void setup() {
        metaModelFinder = new MetaModelFinder(rule.getElements(), rule.getTypes());
        final TypeElement typeElement = rule.getElements().getTypeElement(MetaModels.class.getCanonicalName());
        metaModelsElement = new MetaModelsElement(typeElement, metaModelFinder.findMetaModels(typeElement));
    }

    @Test
    public void construction_of_meta_models_element_ignores_non_meta_model_fields() {
      assertEquals(Set.of(AttachmentMetaModel.class.getSimpleName(), UserMetaModel.class.getSimpleName()), metaModelsElement.getMetaModels().stream().map(MetaModelElement::getSimpleName).collect(toSet()));
    }

    @Test
    public void meta_models_do_not_contain_duplicate_models() {
      assertEquals(2, metaModelsElement.getMetaModels().size());
    }

    /**
     * A type for testing purposes. Represents entry point for meta-models.
     */
    public static final class MetaModels {
        public static final AttachmentMetaModel Attachment_ = new AttachmentMetaModel();
        public static final AttachmentMetaModel Attachment_duplicate = new AttachmentMetaModel();
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