/*
 * TODO consider extracting this javadoc into a standalone module (e.g., platform-javadoc) that has dependencies on all
 * the other platform modules so that javadoc links can refer to any class (currently this is limited to just the
 * dependencies of this module).
*/

/**
 * <h2> Extending / Modifying EQL syntax </h2>
 *
 * <h3> 1. EQL AST (stages 1-3) </h3>
 *
 * The ASTs for stages 1-3 should be modified accordingly. Roughly each language construct is represented by an AST node.
 * AST nodes in stages leading to the last stage must also implement transformation into their respective next stage.
 * The last stage must implement transformation into SQL.
 * <p>
 * Refer to {@link ua.com.fielden.platform.eql.stage1} and its neighbouring packages.
 *
 * <h3> 2. Canonical EQL grammar </h3>
 *
 * <ol>
 *   <li>
 *   Class {@code CanonicalEqlGrammar} in module {@code platform-eql-grammar} declares a static field the value of which
 *   is a BNF instance corresponding to the EQL grammar. This BNF definition should be modified accordingly.
 *   </li>
 *   <li>
 *   The new version of the canonical grammar can be used to generate various artifacts via the {@code GrammarActions} class.
 *   These artifacts include:
 *   <ol>
 *     <li>HTML page with the contents of the BNF.</li>
 *     <li>ANTLR grammar corresponding to the BNF.</li>
 *   </ol>
 *   </li>
 * </ol>
 *
 * <h3> 3. Fluent API </h3>
 *
 * <ol>
 *   <li>
 *   The fluent API should be modified to reflect changes to the grammar.
 *   The point of interest is {@link ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces}.
 *   Depending on the complexity and scale of changes this step might be the most intellectually demanding.
 *   </li>
 *   <li>
 *   The fluent API implementation should be modified / extended as well. This step involves the collection of tokens
 *   corresponding to methods of the API.
 *   In case of introduction of new tokens, {@link ua.com.fielden.platform.entity.query.fluent.EqlSentenceBuilder}
 *   should be enhanced with respective methods, the actual implementation of which is covered during the next steps.
 *   </li>
 * </ol>
 *
 * <h3> 4. ANTLR grammar </h3>
 *
 * <ol>
 *   <li>
 *   After generating the ANTLR grammar from the canonical grammar, it should be merged with the main ANTLR grammar
 *   (i.e., the one used to generate the parser). This might require a bit of handiwork but shouldn't be too difficult.
 *   </li>
 *   <li>
 *   ANTLR Java source code artifacts should be generated from the new version of the ANTLR grammar. This can be
 *   accomplished via the ANTLR Maven plugin. Refer to the {@code platform-eql-grammar} Maven module.
 *   </li>
 * </ol>
 *
 * <h3> 5. ANTLR tokens </h3>
 *
 * <ol>
 * As mentioned previously, if the changes introduced any new tokens, they should be reflected in the new methods
 * of {@link ua.com.fielden.platform.entity.query.fluent.EqlSentenceBuilder}, which is responsible for collecting them.
 * There are 2 cases of tokens that should be considered:
 * <ol>
 *   <li>
 *   Simple tokens (corresponding to fluent API methods that declare <b>no parameters</b>).
 *   <p>
 *   These do not require anything special. They should be handled like all the other simple tokens.
 *   </li>
 *   <li>
 *   Parameterised tokens (corresponding to fluent API methods that <b>do declare parameters</b>).
 *   <p>
 *   Each such token requires its own custom token type that is characterised by the specific information associated
 *   with that token, which directly corresponds to the declared parameters of the fluent API method.
 *   <p>
 *   Refer to {@link ua.com.fielden.platform.eql.antlr.tokens.AbstractParameterisedEqlToken} and its subclasses.
 *   </li>
 * </ol>
 * </ol>
 *
 * <h3> 6. ANTLR visitors </h3>
 *
 * ANTLR visitors form the core of the transformation process that takes a parse tree produced by the generated parser
 * and outputs an AST in the EQL's stage 1 form.
 * <p>
 * The transformation's entry point is {@link ua.com.fielden.platform.eql.antlr.EqlCompiler}. It shouldn't require any
 * modifications, unless the introduced changes affect the <i>top-level</i> rules (e.g., introduce a new query type).
 * <p>
 * Refer to {@link ua.com.fielden.platform.eql.antlr.AbstractEqlVisitor} and its subclasses.
 */
package ua.com.fielden.platform.eql;
