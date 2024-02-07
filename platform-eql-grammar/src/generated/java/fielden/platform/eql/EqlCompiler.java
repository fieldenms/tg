package fielden.platform.eql;

import java.util.*;

@SuppressWarnings("all")
public interface EqlCompiler {
  public static fielden.platform.eql.EqlAST.Query parse_Query(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.select)) return parse_Select(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.expr)) return parse_Expression(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.Select parse_Select(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Class class_ = (java.lang.Class) _a.arguments.get(0);
    _b = il.ac.technion.cs.fling.internal.grammar.rules.Opt.abbreviate(parse__Where2(w), 1);
    java.util.Optional<fielden.platform.eql.EqlAST.Where> where =
        (java.util.Optional<fielden.platform.eql.EqlAST.Where>) _b.get(0);
    fielden.platform.eql.EqlAST.Model model = parse_Model(w);
    return new fielden.platform.eql.EqlAST.Select(class_, where, model);
  }

  public static fielden.platform.eql.EqlAST.Expression parse_Expression(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.Expression();
  }

  public static fielden.platform.eql.EqlAST.Where parse_Where(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    fielden.platform.eql.EqlAST.Condition condition = parse_Condition(w);
    _b =
        il.ac.technion.cs.fling.internal.grammar.rules.NoneOrMore.abbreviate(
            parse__LogicalOp2(w), 2);
    java.util.List<fielden.platform.eql.EqlAST.LogicalOp> logicalOp =
        (java.util.List<fielden.platform.eql.EqlAST.LogicalOp>) _b.get(0);
    java.util.List<fielden.platform.eql.EqlAST.Condition> condition2 =
        (java.util.List<fielden.platform.eql.EqlAST.Condition>) _b.get(1);
    return new fielden.platform.eql.EqlAST.Where(condition, logicalOp, condition2);
  }

  public static fielden.platform.eql.EqlAST.Model parse_Model(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String string = (java.lang.String) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Model(string);
  }

  public static fielden.platform.eql.EqlAST.Condition parse_Condition(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    fielden.platform.eql.EqlAST.Operand operand = parse_Operand(w);
    fielden.platform.eql.EqlAST.ComparisonOperation comparisonOperation =
        parse_ComparisonOperation(w);
    return new fielden.platform.eql.EqlAST.Condition(operand, comparisonOperation);
  }

  public static fielden.platform.eql.EqlAST.LogicalOp parse_LogicalOp(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.and)) return parse_And(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.or)) return parse_Or(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.Operand parse_Operand(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.now,
        fielden.platform.eql.fling.EqlGrammar.Σ.expr,
        fielden.platform.eql.fling.EqlGrammar.Σ.val,
        fielden.platform.eql.fling.EqlGrammar.Σ.iVal,
        fielden.platform.eql.fling.EqlGrammar.Σ.param,
        fielden.platform.eql.fling.EqlGrammar.Σ.param,
        fielden.platform.eql.fling.EqlGrammar.Σ.iParam,
        fielden.platform.eql.fling.EqlGrammar.Σ.iParam,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp)) return parse_SingleOperand(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfProps,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfProps,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfProps,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfProps,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfValues,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfValues,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfParams,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfIParams,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfParams,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfIParams,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfModels,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfModels,
        fielden.platform.eql.fling.EqlGrammar.Σ.anyOfExpressions,
        fielden.platform.eql.fling.EqlGrammar.Σ.allOfExpressions)) return parse_MultiOperand(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.ComparisonOperation parse_ComparisonOperation(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.isNull,
        fielden.platform.eql.fling.EqlGrammar.Σ.isNotNull)) return parse_UnaryOperator(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.eq,
        fielden.platform.eql.fling.EqlGrammar.Σ.gt,
        fielden.platform.eql.fling.EqlGrammar.Σ.lt,
        fielden.platform.eql.fling.EqlGrammar.Σ.ge,
        fielden.platform.eql.fling.EqlGrammar.Σ.le,
        fielden.platform.eql.fling.EqlGrammar.Σ.like,
        fielden.platform.eql.fling.EqlGrammar.Σ.iLike,
        fielden.platform.eql.fling.EqlGrammar.Σ.notLike,
        fielden.platform.eql.fling.EqlGrammar.Σ.notILike)) return parse_ComparisonOperation1(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.UnaryOperator parse_UnaryOperator(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.isNull)) return parse_UnaryOperator1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.isNotNull)) return parse_UnaryOperator2(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator parse_BinaryOperator(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.eq)) return parse_BinaryOperator1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.gt)) return parse_BinaryOperator2(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.lt)) return parse_BinaryOperator3(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.ge)) return parse_BinaryOperator4(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.le)) return parse_BinaryOperator5(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.like)) return parse_BinaryOperator6(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.iLike)) return parse_BinaryOperator7(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.notLike)) return parse_BinaryOperator8(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.notILike)) return parse_BinaryOperator9(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.SingleOperand parse_SingleOperand(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp)) return parse_AnyProp(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.val,
        fielden.platform.eql.fling.EqlGrammar.Σ.iVal)) return parse_Val(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.param,
        fielden.platform.eql.fling.EqlGrammar.Σ.param,
        fielden.platform.eql.fling.EqlGrammar.Σ.iParam,
        fielden.platform.eql.fling.EqlGrammar.Σ.iParam)) return parse_Param(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.now)) return parse_SingleOperand1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.expr)) return parse_SingleOperand2(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.MultiOperand parse_MultiOperand(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfProps)) return parse_MultiOperand1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfProps)) return parse_MultiOperand2(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfProps)) return parse_MultiOperand3(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfProps)) return parse_MultiOperand4(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfValues)) return parse_MultiOperand5(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfValues)) return parse_MultiOperand6(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfParams)) return parse_MultiOperand7(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfIParams)) return parse_MultiOperand8(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfParams)) return parse_MultiOperand9(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfIParams)) return parse_MultiOperand10(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfModels)) return parse_MultiOperand11(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfModels)) return parse_MultiOperand12(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.anyOfExpressions))
      return parse_MultiOperand13(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.allOfExpressions))
      return parse_MultiOperand14(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.AnyProp parse_AnyProp(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop,
        fielden.platform.eql.fling.EqlGrammar.Σ.prop)) return parse_Prop(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp,
        fielden.platform.eql.fling.EqlGrammar.Σ.extProp)) return parse_ExtProp(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.Val parse_Val(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.val)) return parse_Val1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.iVal)) return parse_Val2(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.Param parse_Param(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.param)) return parse_Param1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.param)) return parse_Param2(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.iParam)) return parse_Param3(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.iParam)) return parse_Param4(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.Prop parse_Prop(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.prop)) return parse_Prop1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.prop)) return parse_Prop2(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.prop)) return parse_Prop3(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.ExtProp parse_ExtProp(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a = w.get(0);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.extProp)) return parse_ExtProp1(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.extProp)) return parse_ExtProp2(w);
    if (il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.extProp)) return parse_ExtProp3(w);
    return null;
  }

  public static fielden.platform.eql.EqlAST.And parse_And(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.And();
  }

  public static fielden.platform.eql.EqlAST.Or parse_Or(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.Or();
  }

  public static fielden.platform.eql.EqlAST.ComparisonOperation1 parse_ComparisonOperation1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    fielden.platform.eql.EqlAST.BinaryOperator binaryOperator = parse_BinaryOperator(w);
    fielden.platform.eql.EqlAST.Operand operand = parse_Operand(w);
    return new fielden.platform.eql.EqlAST.ComparisonOperation1(binaryOperator, operand);
  }

  public static fielden.platform.eql.EqlAST.UnaryOperator1 parse_UnaryOperator1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.UnaryOperator1();
  }

  public static fielden.platform.eql.EqlAST.UnaryOperator2 parse_UnaryOperator2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.UnaryOperator2();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator1 parse_BinaryOperator1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator1();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator2 parse_BinaryOperator2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator2();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator3 parse_BinaryOperator3(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator3();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator4 parse_BinaryOperator4(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator4();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator5 parse_BinaryOperator5(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator5();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator6 parse_BinaryOperator6(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator6();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator7 parse_BinaryOperator7(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator7();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator8 parse_BinaryOperator8(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator8();
  }

  public static fielden.platform.eql.EqlAST.BinaryOperator9 parse_BinaryOperator9(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.BinaryOperator9();
  }

  public static fielden.platform.eql.EqlAST.SingleOperand1 parse_SingleOperand1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    return new fielden.platform.eql.EqlAST.SingleOperand1();
  }

  public static fielden.platform.eql.EqlAST.SingleOperand2 parse_SingleOperand2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.entity.query.model.ExpressionModel expressionModel =
        (ua.com.fielden.platform.entity.query.model.ExpressionModel) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.SingleOperand2(expressionModel);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand1 parse_MultiOperand1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String[] strings = (java.lang.String[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand1(strings);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand2 parse_MultiOperand2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.processors.metamodel.IConvertableToPath[] iConvertableToPaths =
        (ua.com.fielden.platform.processors.metamodel.IConvertableToPath[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand2(iConvertableToPaths);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand3 parse_MultiOperand3(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String[] strings = (java.lang.String[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand3(strings);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand4 parse_MultiOperand4(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.processors.metamodel.IConvertableToPath[] iConvertableToPaths =
        (ua.com.fielden.platform.processors.metamodel.IConvertableToPath[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand4(iConvertableToPaths);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand5 parse_MultiOperand5(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Object[] objects = (java.lang.Object[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand5(objects);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand6 parse_MultiOperand6(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Object[] objects = (java.lang.Object[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand6(objects);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand7 parse_MultiOperand7(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String[] strings = (java.lang.String[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand7(strings);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand8 parse_MultiOperand8(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String[] strings = (java.lang.String[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand8(strings);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand9 parse_MultiOperand9(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String[] strings = (java.lang.String[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand9(strings);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand10 parse_MultiOperand10(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String[] strings = (java.lang.String[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand10(strings);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand11 parse_MultiOperand11(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[]
        primitiveResultQueryModels =
            (ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[])
                _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand11(primitiveResultQueryModels);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand12 parse_MultiOperand12(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[]
        primitiveResultQueryModels =
            (ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[])
                _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand12(primitiveResultQueryModels);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand13 parse_MultiOperand13(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.entity.query.model.ExpressionModel[] expressionModels =
        (ua.com.fielden.platform.entity.query.model.ExpressionModel[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand13(expressionModels);
  }

  public static fielden.platform.eql.EqlAST.MultiOperand14 parse_MultiOperand14(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.entity.query.model.ExpressionModel[] expressionModels =
        (ua.com.fielden.platform.entity.query.model.ExpressionModel[]) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.MultiOperand14(expressionModels);
  }

  public static fielden.platform.eql.EqlAST.Val1 parse_Val1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Object object = (java.lang.Object) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Val1(object);
  }

  public static fielden.platform.eql.EqlAST.Val2 parse_Val2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Object object = (java.lang.Object) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Val2(object);
  }

  public static fielden.platform.eql.EqlAST.Param1 parse_Param1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String string = (java.lang.String) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Param1(string);
  }

  public static fielden.platform.eql.EqlAST.Param2 parse_Param2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Enum enum_ = (java.lang.Enum) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Param2(enum_);
  }

  public static fielden.platform.eql.EqlAST.Param3 parse_Param3(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String string = (java.lang.String) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Param3(string);
  }

  public static fielden.platform.eql.EqlAST.Param4 parse_Param4(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Enum enum_ = (java.lang.Enum) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Param4(enum_);
  }

  public static fielden.platform.eql.EqlAST.Prop1 parse_Prop1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String string = (java.lang.String) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Prop1(string);
  }

  public static fielden.platform.eql.EqlAST.Prop2 parse_Prop2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.processors.metamodel.IConvertableToPath iConvertableToPath =
        (ua.com.fielden.platform.processors.metamodel.IConvertableToPath) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Prop2(iConvertableToPath);
  }

  public static fielden.platform.eql.EqlAST.Prop3 parse_Prop3(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Enum enum_ = (java.lang.Enum) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.Prop3(enum_);
  }

  public static fielden.platform.eql.EqlAST.ExtProp1 parse_ExtProp1(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.String string = (java.lang.String) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.ExtProp1(string);
  }

  public static fielden.platform.eql.EqlAST.ExtProp2 parse_ExtProp2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    ua.com.fielden.platform.processors.metamodel.IConvertableToPath iConvertableToPath =
        (ua.com.fielden.platform.processors.metamodel.IConvertableToPath) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.ExtProp2(iConvertableToPath);
  }

  public static fielden.platform.eql.EqlAST.ExtProp3 parse_ExtProp3(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<?> _b;
    _a = w.remove(0);
    java.lang.Enum enum_ = (java.lang.Enum) _a.arguments.get(0);
    return new fielden.platform.eql.EqlAST.ExtProp3(enum_);
  }

  public static java.util.List<java.lang.Object> parse__Where2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<java.lang.Object> _b;
    if (w.isEmpty()) return java.util.Collections.emptyList();
    _a = w.get(0);
    if (!(il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ, fielden.platform.eql.fling.EqlGrammar.Σ.where)))
      return java.util.Collections.emptyList();
    fielden.platform.eql.EqlAST.Where where = parse_Where(w);
    return java.util.Arrays.asList(where);
  }

  public static java.util.List<java.lang.Object> parse__LogicalOp2(
      java.util.List<il.ac.technion.cs.fling.internal.compiler.Invocation> w) {
    il.ac.technion.cs.fling.internal.compiler.Invocation _a;
    java.util.List<java.lang.Object> _b;
    if (w.isEmpty()) return java.util.Collections.emptyList();
    _a = w.get(0);
    if (!(il.ac.technion.cs.fling.internal.util.Is.included(
        _a.σ,
        fielden.platform.eql.fling.EqlGrammar.Σ.and,
        fielden.platform.eql.fling.EqlGrammar.Σ.or))) return java.util.Collections.emptyList();
    fielden.platform.eql.EqlAST.LogicalOp logicalOp = parse_LogicalOp(w);
    fielden.platform.eql.EqlAST.Condition condition = parse_Condition(w);
    java.util.List<java.lang.Object> _c = parse__LogicalOp2(w);
    return java.util.Arrays.asList(logicalOp, condition, _c);
  }
}
