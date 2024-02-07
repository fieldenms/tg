package fielden.platform.eql;

import java.util.*;

@SuppressWarnings("all")
public interface EqlAST {
  interface Query {}

  public class Select implements Query {
    public final java.lang.Class select;
    public final java.util.Optional<Where> where;
    public final Model model;

    public Select(java.lang.Class select, java.util.Optional<Where> where, Model model) {
      this.select = select;
      this.where = where;
      this.model = model;
    }
  }

  public class Expression implements Query {
    public Expression() {}
  }

  public class Where {
    public final Condition condition;
    public final java.util.List<LogicalOp> logicalOp;
    public final java.util.List<Condition> condition2;

    public Where(
        Condition condition,
        java.util.List<LogicalOp> logicalOp,
        java.util.List<Condition> condition2) {
      this.condition = condition;
      this.logicalOp = logicalOp;
      this.condition2 = condition2;
    }
  }

  public class Model {
    public final java.lang.String model;

    public Model(java.lang.String model) {
      this.model = model;
    }
  }

  public class Condition {
    public final Operand operand;
    public final ComparisonOperation comparisonOperation;

    public Condition(Operand operand, ComparisonOperation comparisonOperation) {
      this.operand = operand;
      this.comparisonOperation = comparisonOperation;
    }
  }

  interface LogicalOp {}

  interface Operand {}

  interface ComparisonOperation {}

  interface UnaryOperator extends ComparisonOperation {}

  interface BinaryOperator {}

  interface SingleOperand extends Operand {}

  interface MultiOperand extends Operand {}

  interface AnyProp extends SingleOperand {}

  interface Val extends SingleOperand {}

  interface Param extends SingleOperand {}

  interface Prop extends AnyProp {}

  interface ExtProp extends AnyProp {}

  public class And implements LogicalOp {
    public And() {}
  }

  public class Or implements LogicalOp {
    public Or() {}
  }

  public class ComparisonOperation1 implements ComparisonOperation {
    public final BinaryOperator binaryOperator;
    public final Operand operand;

    public ComparisonOperation1(BinaryOperator binaryOperator, Operand operand) {
      this.binaryOperator = binaryOperator;
      this.operand = operand;
    }
  }

  public class UnaryOperator1 implements UnaryOperator {
    public UnaryOperator1() {}
  }

  public class UnaryOperator2 implements UnaryOperator {
    public UnaryOperator2() {}
  }

  public class BinaryOperator1 implements BinaryOperator {
    public BinaryOperator1() {}
  }

  public class BinaryOperator2 implements BinaryOperator {
    public BinaryOperator2() {}
  }

  public class BinaryOperator3 implements BinaryOperator {
    public BinaryOperator3() {}
  }

  public class BinaryOperator4 implements BinaryOperator {
    public BinaryOperator4() {}
  }

  public class BinaryOperator5 implements BinaryOperator {
    public BinaryOperator5() {}
  }

  public class BinaryOperator6 implements BinaryOperator {
    public BinaryOperator6() {}
  }

  public class BinaryOperator7 implements BinaryOperator {
    public BinaryOperator7() {}
  }

  public class BinaryOperator8 implements BinaryOperator {
    public BinaryOperator8() {}
  }

  public class BinaryOperator9 implements BinaryOperator {
    public BinaryOperator9() {}
  }

  public class SingleOperand1 implements SingleOperand {
    public SingleOperand1() {}
  }

  public class SingleOperand2 implements SingleOperand {
    public final ua.com.fielden.platform.entity.query.model.ExpressionModel expr;

    public SingleOperand2(ua.com.fielden.platform.entity.query.model.ExpressionModel expr) {
      this.expr = expr;
    }
  }

  public class MultiOperand1 implements MultiOperand {
    public final java.lang.String[] anyOfProps;

    public MultiOperand1(java.lang.String[] anyOfProps) {
      this.anyOfProps = anyOfProps;
    }
  }

  public class MultiOperand2 implements MultiOperand {
    public final ua.com.fielden.platform.processors.metamodel.IConvertableToPath[] anyOfProps;

    public MultiOperand2(
        ua.com.fielden.platform.processors.metamodel.IConvertableToPath[] anyOfProps) {
      this.anyOfProps = anyOfProps;
    }
  }

  public class MultiOperand3 implements MultiOperand {
    public final java.lang.String[] allOfProps;

    public MultiOperand3(java.lang.String[] allOfProps) {
      this.allOfProps = allOfProps;
    }
  }

  public class MultiOperand4 implements MultiOperand {
    public final ua.com.fielden.platform.processors.metamodel.IConvertableToPath[] allOfProps;

    public MultiOperand4(
        ua.com.fielden.platform.processors.metamodel.IConvertableToPath[] allOfProps) {
      this.allOfProps = allOfProps;
    }
  }

  public class MultiOperand5 implements MultiOperand {
    public final java.lang.Object[] anyOfValues;

    public MultiOperand5(java.lang.Object[] anyOfValues) {
      this.anyOfValues = anyOfValues;
    }
  }

  public class MultiOperand6 implements MultiOperand {
    public final java.lang.Object[] allOfValues;

    public MultiOperand6(java.lang.Object[] allOfValues) {
      this.allOfValues = allOfValues;
    }
  }

  public class MultiOperand7 implements MultiOperand {
    public final java.lang.String[] anyOfParams;

    public MultiOperand7(java.lang.String[] anyOfParams) {
      this.anyOfParams = anyOfParams;
    }
  }

  public class MultiOperand8 implements MultiOperand {
    public final java.lang.String[] anyOfIParams;

    public MultiOperand8(java.lang.String[] anyOfIParams) {
      this.anyOfIParams = anyOfIParams;
    }
  }

  public class MultiOperand9 implements MultiOperand {
    public final java.lang.String[] allOfParams;

    public MultiOperand9(java.lang.String[] allOfParams) {
      this.allOfParams = allOfParams;
    }
  }

  public class MultiOperand10 implements MultiOperand {
    public final java.lang.String[] allOfIParams;

    public MultiOperand10(java.lang.String[] allOfIParams) {
      this.allOfIParams = allOfIParams;
    }
  }

  public class MultiOperand11 implements MultiOperand {
    public final ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[] anyOfModels;

    public MultiOperand11(
        ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[] anyOfModels) {
      this.anyOfModels = anyOfModels;
    }
  }

  public class MultiOperand12 implements MultiOperand {
    public final ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[] allOfModels;

    public MultiOperand12(
        ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel[] allOfModels) {
      this.allOfModels = allOfModels;
    }
  }

  public class MultiOperand13 implements MultiOperand {
    public final ua.com.fielden.platform.entity.query.model.ExpressionModel[] anyOfExpressions;

    public MultiOperand13(
        ua.com.fielden.platform.entity.query.model.ExpressionModel[] anyOfExpressions) {
      this.anyOfExpressions = anyOfExpressions;
    }
  }

  public class MultiOperand14 implements MultiOperand {
    public final ua.com.fielden.platform.entity.query.model.ExpressionModel[] allOfExpressions;

    public MultiOperand14(
        ua.com.fielden.platform.entity.query.model.ExpressionModel[] allOfExpressions) {
      this.allOfExpressions = allOfExpressions;
    }
  }

  public class Val1 implements Val {
    public final java.lang.Object val;

    public Val1(java.lang.Object val) {
      this.val = val;
    }
  }

  public class Val2 implements Val {
    public final java.lang.Object iVal;

    public Val2(java.lang.Object iVal) {
      this.iVal = iVal;
    }
  }

  public class Param1 implements Param {
    public final java.lang.String param;

    public Param1(java.lang.String param) {
      this.param = param;
    }
  }

  public class Param2 implements Param {
    public final java.lang.Enum param;

    public Param2(java.lang.Enum param) {
      this.param = param;
    }
  }

  public class Param3 implements Param {
    public final java.lang.String iParam;

    public Param3(java.lang.String iParam) {
      this.iParam = iParam;
    }
  }

  public class Param4 implements Param {
    public final java.lang.Enum iParam;

    public Param4(java.lang.Enum iParam) {
      this.iParam = iParam;
    }
  }

  public class Prop1 implements Prop {
    public final java.lang.String prop;

    public Prop1(java.lang.String prop) {
      this.prop = prop;
    }
  }

  public class Prop2 implements Prop {
    public final ua.com.fielden.platform.processors.metamodel.IConvertableToPath prop;

    public Prop2(ua.com.fielden.platform.processors.metamodel.IConvertableToPath prop) {
      this.prop = prop;
    }
  }

  public class Prop3 implements Prop {
    public final java.lang.Enum prop;

    public Prop3(java.lang.Enum prop) {
      this.prop = prop;
    }
  }

  public class ExtProp1 implements ExtProp {
    public final java.lang.String extProp;

    public ExtProp1(java.lang.String extProp) {
      this.extProp = extProp;
    }
  }

  public class ExtProp2 implements ExtProp {
    public final ua.com.fielden.platform.processors.metamodel.IConvertableToPath extProp;

    public ExtProp2(ua.com.fielden.platform.processors.metamodel.IConvertableToPath extProp) {
      this.extProp = extProp;
    }
  }

  public class ExtProp3 implements ExtProp {
    public final java.lang.Enum extProp;

    public ExtProp3(java.lang.Enum extProp) {
      this.extProp = extProp;
    }
  }

  public static class Visitor {
    public final void visit(fielden.platform.eql.EqlAST.Query query) {
      if (query instanceof fielden.platform.eql.EqlAST.Select) {
        visit((fielden.platform.eql.EqlAST.Select) query);
      } else if (query instanceof fielden.platform.eql.EqlAST.Expression) {
        visit((fielden.platform.eql.EqlAST.Expression) query);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Select select) {
      try {
        this.whileVisiting(select);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
      {
        select.where.ifPresent(
            _x_ -> {
              visit((fielden.platform.eql.EqlAST.Where) _x_);
            });
      }
      {
        visit((fielden.platform.eql.EqlAST.Model) select.model);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Expression expression) {
      try {
        this.whileVisiting(expression);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Where where) {
      try {
        this.whileVisiting(where);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
      {
        visit((fielden.platform.eql.EqlAST.Condition) where.condition);
      }
      {
        where.logicalOp.stream()
            .forEach(
                _x_ -> {
                  visit((fielden.platform.eql.EqlAST.LogicalOp) _x_);
                });
      }
      {
        where.condition2.stream()
            .forEach(
                _x_2 -> {
                  visit((fielden.platform.eql.EqlAST.Condition) _x_2);
                });
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Model model) {
      try {
        this.whileVisiting(model);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Condition condition) {
      try {
        this.whileVisiting(condition);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
      {
        visit((fielden.platform.eql.EqlAST.Operand) condition.operand);
      }
      {
        visit((fielden.platform.eql.EqlAST.ComparisonOperation) condition.comparisonOperation);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.LogicalOp logicalOp) {
      if (logicalOp instanceof fielden.platform.eql.EqlAST.And) {
        visit((fielden.platform.eql.EqlAST.And) logicalOp);
      } else if (logicalOp instanceof fielden.platform.eql.EqlAST.Or) {
        visit((fielden.platform.eql.EqlAST.Or) logicalOp);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Operand operand) {
      if (operand instanceof fielden.platform.eql.EqlAST.SingleOperand) {
        visit((fielden.platform.eql.EqlAST.SingleOperand) operand);
      } else if (operand instanceof fielden.platform.eql.EqlAST.MultiOperand) {
        visit((fielden.platform.eql.EqlAST.MultiOperand) operand);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.ComparisonOperation comparisonOperation) {
      if (comparisonOperation instanceof fielden.platform.eql.EqlAST.UnaryOperator) {
        visit((fielden.platform.eql.EqlAST.UnaryOperator) comparisonOperation);
      } else if (comparisonOperation instanceof fielden.platform.eql.EqlAST.ComparisonOperation1) {
        visit((fielden.platform.eql.EqlAST.ComparisonOperation1) comparisonOperation);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.UnaryOperator unaryOperator) {
      if (unaryOperator instanceof fielden.platform.eql.EqlAST.UnaryOperator1) {
        visit((fielden.platform.eql.EqlAST.UnaryOperator1) unaryOperator);
      } else if (unaryOperator instanceof fielden.platform.eql.EqlAST.UnaryOperator2) {
        visit((fielden.platform.eql.EqlAST.UnaryOperator2) unaryOperator);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator binaryOperator) {
      if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator1) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator1) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator2) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator2) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator3) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator3) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator4) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator4) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator5) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator5) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator6) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator6) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator7) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator7) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator8) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator8) binaryOperator);
      } else if (binaryOperator instanceof fielden.platform.eql.EqlAST.BinaryOperator9) {
        visit((fielden.platform.eql.EqlAST.BinaryOperator9) binaryOperator);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.SingleOperand singleOperand) {
      if (singleOperand instanceof fielden.platform.eql.EqlAST.AnyProp) {
        visit((fielden.platform.eql.EqlAST.AnyProp) singleOperand);
      } else if (singleOperand instanceof fielden.platform.eql.EqlAST.Val) {
        visit((fielden.platform.eql.EqlAST.Val) singleOperand);
      } else if (singleOperand instanceof fielden.platform.eql.EqlAST.Param) {
        visit((fielden.platform.eql.EqlAST.Param) singleOperand);
      } else if (singleOperand instanceof fielden.platform.eql.EqlAST.SingleOperand1) {
        visit((fielden.platform.eql.EqlAST.SingleOperand1) singleOperand);
      } else if (singleOperand instanceof fielden.platform.eql.EqlAST.SingleOperand2) {
        visit((fielden.platform.eql.EqlAST.SingleOperand2) singleOperand);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand multiOperand) {
      if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand1) {
        visit((fielden.platform.eql.EqlAST.MultiOperand1) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand2) {
        visit((fielden.platform.eql.EqlAST.MultiOperand2) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand3) {
        visit((fielden.platform.eql.EqlAST.MultiOperand3) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand4) {
        visit((fielden.platform.eql.EqlAST.MultiOperand4) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand5) {
        visit((fielden.platform.eql.EqlAST.MultiOperand5) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand6) {
        visit((fielden.platform.eql.EqlAST.MultiOperand6) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand7) {
        visit((fielden.platform.eql.EqlAST.MultiOperand7) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand8) {
        visit((fielden.platform.eql.EqlAST.MultiOperand8) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand9) {
        visit((fielden.platform.eql.EqlAST.MultiOperand9) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand10) {
        visit((fielden.platform.eql.EqlAST.MultiOperand10) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand11) {
        visit((fielden.platform.eql.EqlAST.MultiOperand11) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand12) {
        visit((fielden.platform.eql.EqlAST.MultiOperand12) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand13) {
        visit((fielden.platform.eql.EqlAST.MultiOperand13) multiOperand);
      } else if (multiOperand instanceof fielden.platform.eql.EqlAST.MultiOperand14) {
        visit((fielden.platform.eql.EqlAST.MultiOperand14) multiOperand);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.AnyProp anyProp) {
      if (anyProp instanceof fielden.platform.eql.EqlAST.Prop) {
        visit((fielden.platform.eql.EqlAST.Prop) anyProp);
      } else if (anyProp instanceof fielden.platform.eql.EqlAST.ExtProp) {
        visit((fielden.platform.eql.EqlAST.ExtProp) anyProp);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Val val) {
      if (val instanceof fielden.platform.eql.EqlAST.Val1) {
        visit((fielden.platform.eql.EqlAST.Val1) val);
      } else if (val instanceof fielden.platform.eql.EqlAST.Val2) {
        visit((fielden.platform.eql.EqlAST.Val2) val);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Param param) {
      if (param instanceof fielden.platform.eql.EqlAST.Param1) {
        visit((fielden.platform.eql.EqlAST.Param1) param);
      } else if (param instanceof fielden.platform.eql.EqlAST.Param2) {
        visit((fielden.platform.eql.EqlAST.Param2) param);
      } else if (param instanceof fielden.platform.eql.EqlAST.Param3) {
        visit((fielden.platform.eql.EqlAST.Param3) param);
      } else if (param instanceof fielden.platform.eql.EqlAST.Param4) {
        visit((fielden.platform.eql.EqlAST.Param4) param);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Prop prop) {
      if (prop instanceof fielden.platform.eql.EqlAST.Prop1) {
        visit((fielden.platform.eql.EqlAST.Prop1) prop);
      } else if (prop instanceof fielden.platform.eql.EqlAST.Prop2) {
        visit((fielden.platform.eql.EqlAST.Prop2) prop);
      } else if (prop instanceof fielden.platform.eql.EqlAST.Prop3) {
        visit((fielden.platform.eql.EqlAST.Prop3) prop);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.ExtProp extProp) {
      if (extProp instanceof fielden.platform.eql.EqlAST.ExtProp1) {
        visit((fielden.platform.eql.EqlAST.ExtProp1) extProp);
      } else if (extProp instanceof fielden.platform.eql.EqlAST.ExtProp2) {
        visit((fielden.platform.eql.EqlAST.ExtProp2) extProp);
      } else if (extProp instanceof fielden.platform.eql.EqlAST.ExtProp3) {
        visit((fielden.platform.eql.EqlAST.ExtProp3) extProp);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.And and) {
      try {
        this.whileVisiting(and);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Or or) {
      try {
        this.whileVisiting(or);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.ComparisonOperation1 comparisonOperation1) {
      try {
        this.whileVisiting(comparisonOperation1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
      {
        visit((fielden.platform.eql.EqlAST.BinaryOperator) comparisonOperation1.binaryOperator);
      }
      {
        visit((fielden.platform.eql.EqlAST.Operand) comparisonOperation1.operand);
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.UnaryOperator1 unaryOperator1) {
      try {
        this.whileVisiting(unaryOperator1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.UnaryOperator2 unaryOperator2) {
      try {
        this.whileVisiting(unaryOperator2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator1 binaryOperator1) {
      try {
        this.whileVisiting(binaryOperator1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator2 binaryOperator2) {
      try {
        this.whileVisiting(binaryOperator2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator3 binaryOperator3) {
      try {
        this.whileVisiting(binaryOperator3);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator4 binaryOperator4) {
      try {
        this.whileVisiting(binaryOperator4);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator5 binaryOperator5) {
      try {
        this.whileVisiting(binaryOperator5);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator6 binaryOperator6) {
      try {
        this.whileVisiting(binaryOperator6);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator7 binaryOperator7) {
      try {
        this.whileVisiting(binaryOperator7);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator8 binaryOperator8) {
      try {
        this.whileVisiting(binaryOperator8);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.BinaryOperator9 binaryOperator9) {
      try {
        this.whileVisiting(binaryOperator9);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.SingleOperand1 singleOperand1) {
      try {
        this.whileVisiting(singleOperand1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.SingleOperand2 singleOperand2) {
      try {
        this.whileVisiting(singleOperand2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand1 multiOperand1) {
      try {
        this.whileVisiting(multiOperand1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand2 multiOperand2) {
      try {
        this.whileVisiting(multiOperand2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand3 multiOperand3) {
      try {
        this.whileVisiting(multiOperand3);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand4 multiOperand4) {
      try {
        this.whileVisiting(multiOperand4);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand5 multiOperand5) {
      try {
        this.whileVisiting(multiOperand5);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand6 multiOperand6) {
      try {
        this.whileVisiting(multiOperand6);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand7 multiOperand7) {
      try {
        this.whileVisiting(multiOperand7);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand8 multiOperand8) {
      try {
        this.whileVisiting(multiOperand8);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand9 multiOperand9) {
      try {
        this.whileVisiting(multiOperand9);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand10 multiOperand10) {
      try {
        this.whileVisiting(multiOperand10);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand11 multiOperand11) {
      try {
        this.whileVisiting(multiOperand11);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand12 multiOperand12) {
      try {
        this.whileVisiting(multiOperand12);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand13 multiOperand13) {
      try {
        this.whileVisiting(multiOperand13);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.MultiOperand14 multiOperand14) {
      try {
        this.whileVisiting(multiOperand14);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Val1 val1) {
      try {
        this.whileVisiting(val1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Val2 val2) {
      try {
        this.whileVisiting(val2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Param1 param1) {
      try {
        this.whileVisiting(param1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Param2 param2) {
      try {
        this.whileVisiting(param2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Param3 param3) {
      try {
        this.whileVisiting(param3);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Param4 param4) {
      try {
        this.whileVisiting(param4);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Prop1 prop1) {
      try {
        this.whileVisiting(prop1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Prop2 prop2) {
      try {
        this.whileVisiting(prop2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.Prop3 prop3) {
      try {
        this.whileVisiting(prop3);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.ExtProp1 extProp1) {
      try {
        this.whileVisiting(extProp1);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.ExtProp2 extProp2) {
      try {
        this.whileVisiting(extProp2);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public final void visit(fielden.platform.eql.EqlAST.ExtProp3 extProp3) {
      try {
        this.whileVisiting(extProp3);
      } catch (java.lang.Exception __) {
        __.printStackTrace();
      }
    }

    public void whileVisiting(fielden.platform.eql.EqlAST.Select select)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Expression expression)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Where where) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Model model) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Condition condition)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.And and) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Or or) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.ComparisonOperation1 comparisonOperation1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.UnaryOperator1 unaryOperator1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.UnaryOperator2 unaryOperator2)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator1 binaryOperator1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator2 binaryOperator2)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator3 binaryOperator3)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator4 binaryOperator4)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator5 binaryOperator5)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator6 binaryOperator6)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator7 binaryOperator7)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator8 binaryOperator8)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.BinaryOperator9 binaryOperator9)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.SingleOperand1 singleOperand1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.SingleOperand2 singleOperand2)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand1 multiOperand1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand2 multiOperand2)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand3 multiOperand3)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand4 multiOperand4)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand5 multiOperand5)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand6 multiOperand6)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand7 multiOperand7)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand8 multiOperand8)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand9 multiOperand9)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand10 multiOperand10)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand11 multiOperand11)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand12 multiOperand12)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand13 multiOperand13)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.MultiOperand14 multiOperand14)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Val1 val1) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Val2 val2) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Param1 param1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Param2 param2)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Param3 param3)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Param4 param4)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Prop1 prop1) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Prop2 prop2) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.Prop3 prop3) throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.ExtProp1 extProp1)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.ExtProp2 extProp2)
        throws java.lang.Exception {}

    public void whileVisiting(fielden.platform.eql.EqlAST.ExtProp3 extProp3)
        throws java.lang.Exception {}
  }
}
