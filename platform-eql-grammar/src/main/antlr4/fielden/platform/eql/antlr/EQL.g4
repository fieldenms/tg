// This grammar was generated. Timestamp: 2024-02-26T15:43:48.195266260+02:00[Europe/Kyiv]

grammar EQL;

start : query EOF;

query :
      select # Query_Select
    | standaloneExpression # Query_StandaloneExpression
    | standaloneCondExpr # Query_StandaloneCondExpr
    | orderBy # Query_OrderBy
;

select :
      selectFrom # Select_SelectFrom
    | sourcelessSelect # Select_SourcelessSelect
;

selectFrom :
      selectSource alias=AS? join? where? groupBy? selectEnd
;

selectSource :
      token=SELECT
;

sourcelessSelect :
      SELECT groupBy? selectEnd
;

selectEnd :
      anyYield # SelectEnd_AnyYield
    | model # SelectEnd_Model
;

where :
      WHERE condition
;

condition :
      predicate                          # PredicateCondition
    | left=condition AND right=condition # AndCondition
    | left=condition OR right=condition  # OrCondition
    | BEGIN condition END                # CompoundCondition
    | NOTBEGIN condition END             # NegatedCompoundCondition
;

predicate :
      unaryPredicate # Predicate_UnaryPredicate
    | comparisonPredicate # Predicate_ComparisonPredicate
    | quantifiedComparisonPredicate # Predicate_QuantifiedComparisonPredicate
    | likePredicate # Predicate_LikePredicate
    | membershipPredicate # Predicate_MembershipPredicate
    | singleConditionPredicate # Predicate_SingleConditionPredicate
;

unaryPredicate :
      left=comparisonOperand unaryComparisonOperator
;

comparisonPredicate :
      left=comparisonOperand op=comparisonOperator right=comparisonOperand
;

quantifiedComparisonPredicate :
      left=comparisonOperand op=comparisonOperator quantifiedOperand
;

likePredicate :
      left=comparisonOperand op=likeOperator right=comparisonOperand
;

membershipPredicate :
      left=comparisonOperand op=membershipOperator membershipOperand
;

unaryComparisonOperator :
      token=ISNULL
    | token=ISNOTNULL
;

likeOperator :
      token=LIKE
    | token=ILIKE
    | token=LIKEWITHCAST
    | token=ILIKEWITHCAST
    | token=NOTLIKE
    | token=NOTLIKEWITHCAST
    | token=NOTILIKEWITHCAST
    | token=NOTILIKE
;

comparisonOperand :
      singleOperand # ComparisonOperand_SingleOperand
    | expr # ComparisonOperand_Expr
    | multiOperand # ComparisonOperand_MultiOperand
;

comparisonOperator :
      token=EQ
    | token=GT
    | token=LT
    | token=GE
    | token=LE
    | token=NE
;

quantifiedOperand :
      token=ALL
    | token=ANY
;

expr :
      BEGINEXPR exprBody ENDEXPR
;

exprBody :
      singleOperandOrExpr (arithmeticalOperator singleOperandOrExpr)*
;

singleOperandOrExpr :
      singleOperand
    | expr
;

arithmeticalOperator :
      token=ADD
    | token=SUB
    | token=DIV
    | token=MULT
    | token=MOD
;

singleOperand :
      prop
    | extProp
    | val
    | param
    | EXPR
    | MODEL
    | unaryFunction
    | ifNull
    | NOW
    | dateDiffInterval
    | dateAddInterval
    | round
    | concat
    | caseWhen
;

unaryFunction :
      funcName=unaryFunctionName argument=singleOperandOrExpr
;

unaryFunctionName :
      token=UPPERCASE
    | token=LOWERCASE
    | token=SECONDOF
    | token=MINUTEOF
    | token=HOUROF
    | token=DAYOF
    | token=MONTHOF
    | token=YEAROF
    | token=DAYOFWEEKOF
    | token=ABSOF
    | token=DATEOF
;

ifNull :
      IFNULL nullable=singleOperandOrExpr THEN other=singleOperandOrExpr
;

dateDiffInterval :
      COUNT unit=dateDiffIntervalUnit BETWEEN startDate=singleOperandOrExpr AND endDate=singleOperandOrExpr
;

dateDiffIntervalUnit :
      token=SECONDS
    | token=MINUTES
    | token=HOURS
    | token=DAYS
    | token=MONTHS
    | token=YEARS
;

dateAddInterval :
      ADDTIMEINTERVALOF left=singleOperandOrExpr unit=dateAddIntervalUnit TO right=singleOperandOrExpr
;

dateAddIntervalUnit :
      token=SECONDS
    | token=MINUTES
    | token=HOURS
    | token=DAYS
    | token=MONTHS
    | token=YEARS
;

round :
      ROUND singleOperandOrExpr TO
;

concat :
      CONCAT singleOperandOrExpr (WITH singleOperandOrExpr)* END
;

caseWhen :
      CASEWHEN condition THEN singleOperandOrExpr (WHEN condition THEN singleOperandOrExpr)* (OTHERWISE otherwiseOperand=singleOperandOrExpr)? caseWhenEnd
;

caseWhenEnd :
      token=END
    | token=ENDASINT
    | token=ENDASBOOL
    | token=ENDASSTR
    | token=ENDASDECIMAL
;

prop :
      token=PROP
;

extProp :
      token=EXTPROP
;

val :
      token=VAL
    | token=IVAL
;

param :
      token=PARAM
    | token=IPARAM
;

multiOperand :
      token=ANYOFPROPS
    | token=ALLOFPROPS
    | token=ANYOFVALUES
    | token=ALLOFVALUES
    | token=ANYOFPARAMS
    | token=ANYOFIPARAMS
    | token=ALLOFPARAMS
    | token=ALLOFIPARAMS
    | token=ANYOFMODELS
    | token=ALLOFMODELS
    | token=ANYOFEXPRESSIONS
    | token=ALLOFEXPRESSIONS
;

membershipOperator :
      token=IN
    | token=NOTIN
;

membershipOperand :
      token=VALUES
    | token=PROPS
    | token=PARAMS
    | token=IPARAMS
    | token=MODEL
;

singleConditionPredicate :
      token=EXISTS
    | token=NOTEXISTS
    | token=EXISTSANYOF
    | token=NOTEXISTSANYOF
    | token=EXISTSALLOF
    | token=NOTEXISTSALLOF
    | token=CRITCONDITION
    | token=CONDITION
    | token=NEGATEDCONDITION
;

join :
      joinOperator alias=AS? joinCondition join?
;

joinOperator :
      token=JOIN
    | token=LEFTJOIN
;

joinCondition :
      ON condition
;

groupBy :
      GROUPBY operand=singleOperandOrExpr groupBy?
;

anyYield :
      yield1 # AnyYield_Yield1
    | yieldMany # AnyYield_YieldMany
;

yield1 :
      YIELD operand=yieldOperand model_=yield1Model
;

yieldMany :
      YIELDALL? aliasedYield+ model_=yieldManyModel
;

aliasedYield :
      YIELD operand=yieldOperand alias=yieldAlias
;

yieldOperand :
      singleOperandOrExpr
    | COUNTALL
    | yieldOperandFunction
;

yieldOperandFunction :
      funcName=yieldOperandFunctionName argument=singleOperandOrExpr
;

yieldOperandFunctionName :
      token=MAXOF
    | token=MINOF
    | token=SUMOF
    | token=COUNTOF
    | token=AVGOF
    | token=SUMOFDISTINCT
    | token=COUNTOFDISTINCT
    | token=AVGOFDISTINCT
;

yieldAlias :
      token=AS
    | token=ASREQUIRED
;

yield1Model :
      token=MODELASENTITY
    | token=MODELASPRIMITIVE
;

yieldManyModel :
      token=MODELASENTITY
    | token=MODELASAGGREGATE
;

model :
      token=MODEL
    | token=MODELASENTITY
    | token=MODELASAGGREGATE
;

standaloneExpression :
      EXPR operand=yieldOperand (arithmeticalOperator yieldOperand)* MODEL
;

standaloneCondExpr :
      COND standaloneCondition MODEL
;

standaloneCondition :
      predicate
    | left=standaloneCondition AND right=standaloneCondition
    | left=standaloneCondition OR right=standaloneCondition
;

orderBy :
      ORDERBY orderByOperand+ MODEL
;

orderByOperand :
      singleOperandOrExpr order
    | YIELD order
    | ORDER
;

order :
      token=ASC
    | token=DESC
;

ABSOF : 'absOf' ;
ADD : 'add' ;
ADDTIMEINTERVALOF : 'addTimeIntervalOf' ;
ALL : 'all' ;
ALLOFEXPRESSIONS : 'allOfExpressions' ;
ALLOFIPARAMS : 'allOfIParams' ;
ALLOFMODELS : 'allOfModels' ;
ALLOFPARAMS : 'allOfParams' ;
ALLOFPROPS : 'allOfProps' ;
ALLOFVALUES : 'allOfValues' ;
AND : 'and' ;
ANY : 'any' ;
ANYOFEXPRESSIONS : 'anyOfExpressions' ;
ANYOFIPARAMS : 'anyOfIParams' ;
ANYOFMODELS : 'anyOfModels' ;
ANYOFPARAMS : 'anyOfParams' ;
ANYOFPROPS : 'anyOfProps' ;
ANYOFVALUES : 'anyOfValues' ;
AS : 'as' ;
ASC : 'asc' ;
ASREQUIRED : 'asRequired' ;
AVGOF : 'avgOf' ;
AVGOFDISTINCT : 'avgOfDistinct' ;
BEGIN : 'begin' ;
BEGINEXPR : 'beginExpr' ;
BETWEEN : 'between' ;
CASEWHEN : 'caseWhen' ;
CONCAT : 'concat' ;
COND : 'cond' ;
CONDITION : 'condition' ;
COUNT : 'count' ;
COUNTALL : 'countAll' ;
COUNTOF : 'countOf' ;
COUNTOFDISTINCT : 'countOfDistinct' ;
CRITCONDITION : 'critCondition' ;
DATEOF : 'dateOf' ;
DAYOF : 'dayOf' ;
DAYOFWEEKOF : 'dayOfWeekOf' ;
DAYS : 'days' ;
DESC : 'desc' ;
DIV : 'div' ;
END : 'end' ;
ENDASBOOL : 'endAsBool' ;
ENDASDECIMAL : 'endAsDecimal' ;
ENDASINT : 'endAsInt' ;
ENDASSTR : 'endAsStr' ;
ENDEXPR : 'endExpr' ;
EQ : 'eq' ;
EXISTS : 'exists' ;
EXISTSALLOF : 'existsAllOf' ;
EXISTSANYOF : 'existsAnyOf' ;
EXPR : 'expr' ;
EXTPROP : 'extProp' ;
GE : 'ge' ;
GROUPBY : 'groupBy' ;
GT : 'gt' ;
HOUROF : 'hourOf' ;
HOURS : 'hours' ;
IFNULL : 'ifNull' ;
ILIKE : 'iLike' ;
ILIKEWITHCAST : 'iLikeWithCast' ;
IN : 'in' ;
IPARAM : 'iParam' ;
IPARAMS : 'iParams' ;
ISNOTNULL : 'isNotNull' ;
ISNULL : 'isNull' ;
IVAL : 'iVal' ;
JOIN : 'join' ;
LE : 'le' ;
LEFTJOIN : 'leftJoin' ;
LIKE : 'like' ;
LIKEWITHCAST : 'likeWithCast' ;
LOWERCASE : 'lowerCase' ;
LT : 'lt' ;
MAXOF : 'maxOf' ;
MINOF : 'minOf' ;
MINUTEOF : 'minuteOf' ;
MINUTES : 'minutes' ;
MOD : 'mod' ;
MODEL : 'model' ;
MODELASAGGREGATE : 'modelAsAggregate' ;
MODELASENTITY : 'modelAsEntity' ;
MODELASPRIMITIVE : 'modelAsPrimitive' ;
MONTHOF : 'monthOf' ;
MONTHS : 'months' ;
MULT : 'mult' ;
NE : 'ne' ;
NEGATEDCONDITION : 'negatedCondition' ;
NOTBEGIN : 'notBegin' ;
NOTEXISTS : 'notExists' ;
NOTEXISTSALLOF : 'notExistsAllOf' ;
NOTEXISTSANYOF : 'notExistsAnyOf' ;
NOTILIKE : 'notILike' ;
NOTILIKEWITHCAST : 'notILikeWithCast' ;
NOTIN : 'notIn' ;
NOTLIKE : 'notLike' ;
NOTLIKEWITHCAST : 'notLikeWithCast' ;
NOW : 'now' ;
ON : 'on' ;
OR : 'or' ;
ORDER : 'order' ;
ORDERBY : 'orderBy' ;
OTHERWISE : 'otherwise' ;
PARAM : 'param' ;
PARAMS : 'params' ;
PROP : 'prop' ;
PROPS : 'props' ;
ROUND : 'round' ;
SECONDOF : 'secondOf' ;
SECONDS : 'seconds' ;
SELECT : 'select' ;
SUB : 'sub' ;
SUMOF : 'sumOf' ;
SUMOFDISTINCT : 'sumOfDistinct' ;
THEN : 'then' ;
TO : 'to' ;
UPPERCASE : 'upperCase' ;
VAL : 'val' ;
VALUES : 'values' ;
WHEN : 'when' ;
WHERE : 'where' ;
WITH : 'with' ;
YEAROF : 'yearOf' ;
YEARS : 'years' ;
YIELD : 'yield' ;
YIELDALL : 'yieldAll' ;

WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT : '//' .*? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

