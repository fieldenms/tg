package ua.com.fielden.platform.processors.security.tokens;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import com.google.common.base.Stopwatch;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.attachment.AttachmentDownload_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanRead_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanSave_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.AttachmentMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.DashboardRefreshFrequencyMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserRoleMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequencyUnit_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequencyUnit_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanSave_Token;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanRead_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRolesUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.utils.StreamUtils;

/**
 * Annotation processor that builds and generates a hierarchy of security token nodes.
 * 
 * @author TG Team
 */
@SupportedAnnotationTypes("*")
public class SecurityTokensProcessor extends AbstractProcessor {
    public static final Set<Class<? extends ISecurityToken>> PLATFORM_TOKENS;
    private static final String GENERATED_PKG_NAME = "tokens";
    private static final String GENERATED_CLASS_SIMPLE_NAME = "TokensHierarchy";
    private static final String GENERATED_CLASS_NAME = "%s.%s".formatted(GENERATED_PKG_NAME, GENERATED_CLASS_SIMPLE_NAME);
    private static final String INDENT = "    ";

    static {
        PLATFORM_TOKENS = Set.of(
                User_CanSave_Token.class,
                User_CanRead_Token.class,
                User_CanReadModel_Token.class,
                User_CanDelete_Token.class,
                UserMaster_CanOpen_Token.class,
                UserRole_CanSave_Token.class,
                UserRole_CanRead_Token.class,
                UserRole_CanReadModel_Token.class,
                UserRole_CanDelete_Token.class,
                UserRoleMaster_CanOpen_Token.class,
                UserAndRoleAssociation_CanRead_Token.class,
                UserAndRoleAssociation_CanReadModel_Token.class,
                UserRolesUpdater_CanExecute_Token.class,
                UserRoleTokensUpdater_CanExecute_Token.class,
                Attachment_CanSave_Token.class,
                Attachment_CanRead_Token.class,
                Attachment_CanReadModel_Token.class,
                Attachment_CanDelete_Token.class,
                AttachmentMaster_CanOpen_Token.class,
                AttachmentDownload_CanExecute_Token.class,
                DashboardRefreshFrequencyUnit_CanRead_Token.class,
                DashboardRefreshFrequencyUnit_CanReadModel_Token.class,
                DashboardRefreshFrequency_CanSave_Token.class,
                DashboardRefreshFrequency_CanRead_Token.class,
                DashboardRefreshFrequency_CanReadModel_Token.class,
                DashboardRefreshFrequency_CanDelete_Token.class,
                DashboardRefreshFrequencyMaster_CanOpen_Token.class,
                DomainExplorer_CanRead_Token.class,
                DomainExplorer_CanReadModel_Token.class,
                KeyNumber_CanRead_Token.class,
                KeyNumber_CanReadModel_Token.class,
                GraphiQL_CanExecute_Token.class);
    }

    private final String classSimpleName = this.getClass().getSimpleName();

    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Map<String, String> options;

    private SecurityTokenFinder tokenFinder;
    private int roundNumber;
    private final Set<TypeElement> collectedTokens = new HashSet<>();
    private boolean aborted = false;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();

        this.roundNumber = 0;
        this.tokenFinder = new SecurityTokenFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());

        messager.printMessage(Kind.NOTE, "%s initialized.".formatted(classSimpleName));
        if (!this.options.isEmpty()) {
            messager.printMessage(Kind.NOTE, "Options: [%s]".formatted(
                    options.keySet().stream().map(k -> "%s=%s".formatted(k, options.get(k))).sorted().collect(joining(", "))));
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        roundNumber = roundNumber + 1;
        final Stopwatch stopwatchProcess = Stopwatch.createStarted();

        messager.printMessage(Kind.NOTE, ">>> %s: PROCESSING ROUND %d START >>>".formatted(classSimpleName, roundNumber));
        if (aborted) {
            messager.printMessage(Kind.NOTE, "Aborted in a prior round, skipping.");
        }
        else {
            messager.printMessage(Kind.NOTE, "annotations: [%s]".formatted(
                    annotations.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));
            final Set<? extends Element> rootElements = roundEnv.getRootElements();
            messager.printMessage(Kind.NOTE, "rootElements: [%s]".formatted(
                    rootElements.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));

            try {
                doProcess(annotations, roundEnv);
            }
            // we want to catch both checked and unchecked exceptions to record the fact that it was thrown 
            // and be able to skip subsequent rounds
            catch (final Exception e) {
                aborted = true;
                messager.printMessage(Kind.ERROR, "Exception was thrown, aborting.");
                messager.printMessage(Kind.ERROR, "%s\n%s".formatted(e.toString(),
                        Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"))));
            }
        }

        stopwatchProcess.stop();
        messager.printMessage(Kind.NOTE, "<<< %s: PROCESSING ROUND %d END [%s millis] <<<".formatted(classSimpleName, roundNumber,
                stopwatchProcess.elapsed(TimeUnit.MILLISECONDS)));

        return false;
    }

    private void doProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) throws Exception {
        collectedTokens.addAll(collectTokens(roundEnv.getRootElements()));
        if (roundEnv.processingOver()) {
            if (shouldGenerate()) {
                generate();
            }
            else {
                messager.printMessage(Kind.NOTE, "No generation is required.");
            }
        }
    }

    private boolean shouldGenerate() {
        // don't generate if no tokens were collected and generated class already exists
        return !(collectedTokens.isEmpty() && elementUtils.getTypeElement(GENERATED_CLASS_NAME) != null);
    }

    private Set<TypeElement> collectTokens(final Set<? extends Element> rootElements) throws Exception {
        return rootElements.stream()
                .filter(el -> el.asType().getKind().equals(TypeKind.DECLARED))
                .map(el -> (TypeElement) el)
                .filter(el -> tokenFinder.isSecurityToken(el.asType()))
                .collect(Collectors.toSet());
    }

    /**
     * Returns a set of security tokens to be included for generation.
     * 
     * @return
     */
    private Set<TypeElement> getIncludedTokens() {
        final Set<TypeElement> tokens = new HashSet<>();
        tokens.addAll(collectedTokens);
        PLATFORM_TOKENS.stream().map(el -> tokenFinder.getTypeElement(el)).forEach(el -> tokens.add(el));

        return tokens;
    }

    private void generate() throws Exception {
        final Set<TypeElement> includedTokens = getIncludedTokens();
        verifyTokens(includedTokens);
        messager.printMessage(Kind.NOTE, "Included tokens: [%s]".formatted(includedTokens.stream().map(el -> el.getSimpleName().toString())
                .collect(joining(", "))));
        generateStructure(includedTokens);
    }

    private void generateStructure(final Collection<? extends TypeElement> tokens) {
        messager.printMessage(Kind.NOTE, "Generating %s".formatted(GENERATED_CLASS_NAME));

        final String tokenTreeFieldName = "TOKENS";
        final String tokenMapFieldName = "TOKEN_NAMES_MAP";

        final SortedSet<SecurityTokenNode> topLevelTokens = buildTokenNodes(tokens);
        // code for populating the token tree
        final CodeBlock.Builder tokenTreePopulationCode = CodeBlock.builder();
        topLevelTokens.forEach(node -> tokenTreePopulationCode.addStatement(buildTokenTreePopulationStatement(tokenTreeFieldName, node)));
        messager.printMessage(Kind.NOTE, "Top-level tokens: [%s]".formatted(topLevelTokens.stream().map(tok -> tok.getToken())
                .collect(joining(", "))));

        final TypeSpec ts = TypeSpec.classBuilder(GENERATED_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC)
                // public final static SortedSet<SecurityTokenNode> TOKENS;
                .addField(ParameterizedTypeName.get(SortedSet.class, SecurityTokenNode.class), tokenTreeFieldName,
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                // public final static Map<String, String> TOKEN_NAMES_MAP;
                .addField(ParameterizedTypeName.get(Map.class, String.class, String.class), tokenMapFieldName,
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                /*
                 static {...}
                 */
                .addStaticBlock(CodeBlock.builder()
                        // TOKENS = new TreeSet<SecurityTokenNode>();
                        .addStatement("%s = new $T()".formatted(tokenTreeFieldName), ParameterizedTypeName.get(TreeSet.class,
                                SecurityTokenNode.class))
                        // TOKEN_NAMES_MAP = new HashMap<String, String>();
                        .addStatement("%s = new $T()".formatted(tokenMapFieldName),
                                ParameterizedTypeName.get(HashMap.class, String.class, String.class))
                        // // build the tree of tokens
                        // TOKENS.add(...)
                        .add(tokenTreePopulationCode.build())
                        // // TODO fill in the map 
                        // TOKEN_NAMES_MAP.put(..., ...);
                        .build())
                .build();

        final JavaFile javaFile = JavaFile.builder(GENERATED_PKG_NAME, ts).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            messager.printMessage(Kind.ERROR, ex.getMessage());
            return;
        }

        messager.printMessage(Kind.NOTE, "Generated %s.%s".formatted(javaFile.packageName, ts.name));
    }

    /**
     * Builds code that populates the token tree with a token node and its sub-nodes recursively.
     * For example, assume that {@code fieldName = "tokens"}:
     * 
     * <pre>
     * tokens.add(new SecurityTokenNode("example.Token")
     *         .add(new SecurityTokenNode("example.SubToken")));
     * </pre>
     * 
     * @see #buildTokenTreePopulationStatementRecursive(SecurityTokenNode)
     * 
     * @param fieldName
     * @param node
     * @return
     */
    private String buildTokenTreePopulationStatement(final String fieldName, final SecurityTokenNode node) {
        return "%s.add(%s)".formatted(fieldName, buildTokenTreePopulationStatementRecursive(node));
    }

    /**
     * Recursively builds a token tree population statement with a token node and its sub-nodes.
     * 
     * @param node
     * @return
     */
    private String buildTokenTreePopulationStatementRecursive(final SecurityTokenNode node) {
        final StringBuilder statement = new StringBuilder("new SecurityTokenNode(\"%s\", \"%s\", \"%s\")".formatted(
                node.getToken(), /*shortDesc*/"", /*longDesc*/""));
        for (final SecurityTokenNode subTokenNode : node.getSubTokenNodes()) {
        statement.append(".add(%s)".formatted(buildTokenTreePopulationStatementRecursive(subTokenNode)));
        }
        return statement.toString();
    }

    /**
     * Performs verification of a collection of security tokens. Throws a runtime exception in case verification was not passed.
     * 
     * @param tokens
     * @throws SecurityException
     */
    private void verifyTokens(final Collection<? extends TypeElement> tokens) {
        // simple class names uniquely identify security tokens and entities!
        if (StreamUtils.distinct(tokens.stream(), el -> el.getSimpleName().toString()).count() != tokens.size()) {
            throw new SecurityException(SecurityTokenProvider.ERR_DUPLICATE_SECURITY_TOKENS);
        }
    }

    /**
     * Transforms a flat collection of security tokens into a hierarchy of {@link SecurityTokenNode} nodes.
     * <p>
     * The result is a forest of trees (i.e., multiple trees), ordered according to the comparator, implemented by {@link SecurityTokenNode}.
     * Tree roots represent the top-level tokens.
     *
     * @param allTokens
     * @return
     */
    private SortedSet<SecurityTokenNode> buildTokenNodes(final Collection<? extends TypeElement> allTokens) {
        final Map<String, SecurityTokenNode> topTokenNodes = new HashMap<>();
        allTokens.forEach(token -> addTokenToHierarchy(token, topTokenNodes));
        return new TreeSet<>(topTokenNodes.values());
    }

    /**
     * Adds the {@link SecurityTokenNode} instance for specified token into the specified topTokenNodes hierarchy if it doesn't exists.
     * Populates the token hierarchy {@code topTokenNodes} with a token represented by {@code tokenElement} and its sub-tokens if they
     * don't already exist.
     *
     * @param tokenElement token to be inserted
     * @param topTokenNodes token hierarchy
     */
    private void addTokenToHierarchy(final TypeElement tokenElement, final Map<String, SecurityTokenNode> topTokenNodes) {
        // First get a list of super classes and then for each such class that doesn't exist in the hierarchy of SecurityTokenNodes, create a node and add it to the hierarchy.
        final List<TypeElement> tokenHierarchy = genHierarchyPath(tokenElement);
        tokenHierarchy.stream().reduce((SecurityTokenNode) null, (tokenNode, tokenEl) -> {
            final String tokenName = tokenEl.getQualifiedName().toString();
            // Argument tokenNode can only be null if tokenClass is the top most class, implementing ISecurityToken.
            // Otherwise tokenNode was created for a super class of tokenClass.
            SecurityTokenNode nextNode = tokenNode == null ? topTokenNodes.get(tokenName) : tokenNode
                    .getSubTokenNode(tokenName);
            // If there is no next token node for tokenClass then create a new one, and
            // add it to the hierarchy as a sub-node of tokenNode or, if tokenNode is null, nextNode becomes top most node.
            if (nextNode == null) {
                // a token for the next node is a sub class of the token represented by tokenNode
                nextNode = new SecurityTokenNode(tokenName, determineShortDesc(tokenElement), determineLongDesc(tokenElement));
                // Is next token the top most?
                if (tokenNode == null) {
                    topTokenNodes.put(tokenName, nextNode);
                }
                else {
                    tokenNode.add(nextNode);
                }
            }
            return nextNode;
        }, (prev, next) -> next);
    }

    /**
     * Linearises the class hierarchy of specified token starting from class that directly implements ISecurityToken to the class specified as token.
     *
     * @param token
     * @return
     */
    private List<TypeElement> genHierarchyPath(final TypeElement token) {
        final TypeMirror rootTokenType = tokenFinder.getTypeElement(ISecurityToken.class).asType();
        final List<TypeElement> hierarchy = Stream.iterate(
                /*seed*/    token,
                /*hasNext*/ t -> typeUtils.isSubtype(t.asType(), rootTokenType),
                /*next*/    t -> tokenFinder.toTypeElement(t.getSuperclass()))
                .collect(Collectors.toList()); // collect to modifiable list
        Collections.reverse(hierarchy);
        return hierarchy;
    }

    private String determineLongDesc(final TypeElement tokenElement) {
        return "";
    }

    private String determineShortDesc(final TypeElement tokenElement) {
        return "";
    }

}