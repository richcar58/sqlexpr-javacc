# SqlExprParser

This project provides a SQL-like expression parser that can be used anywhere in program logic, no SQL database required.  The expression language is a subset of the standard SQL expression language used in WHERE clauses.  Expressions are strings that can be parsed using the `SqlExprEvaluator.parse(String)` method.  If an expression is valid, a BooleanExpression object is returned.  This object, along with a map of name/values pairs, can be evaluated using the `SqlExprEvaluator.match(BooleanExpression, Map)` method.  For convenience, the `SqlExprEvaluator.match(String, Map)` method parses and evaluates an expression.  Here's an example usage of SqlExprParser:

>       // A simple conjunction.
>       String sqlText = "name = 'Bud' AND tenant_id = 'Accounting'";
>       var props = new HashMap<String,Object>();
>       props.put("name", "Bud");
>       props.put("tenant_id", "Accounting");
>       boolean b = SqlExprEvaluator.match(sqlText, props);
>       assert b;  // b is true
>
>       // A more complex example.
>       sqlText = "int1 > 66 AND int2 <> 5 AND (name LIKE 'Jo%n' OR range BETWEEN 200 AND 300)";
>       var props = new HashMap<String,Object>();
>       props.put("int1", 100);
>       props.put("int2", 9);
>       props.put("name", "John");
>       props.put("range", 250);
>       boolean b = SqlExprEvaluator.match(sqlText, props);
>       assert b;  // b is true



## Implementation and Syntax
The [BooleanExpression](https://activemq.apache.org/components/classic/documentation/maven/apidocs/org/apache/activemq/filter/BooleanExpression.html) implementation is borrowed from [Apache ActiveMQ](https://activemq.apache.org/), though ActiveMQ-specific capabilities have been removed.  The SqlExprParser is generated using the [JavaCC](https://javacc.github.io/javacc/) parser generator.

The language recognized by the parser includes the following arithmetic, comparison and logical operators in order of precedence, high to low:

>       (unary) +, -
>       *, /, % (mod)
>       +, -
>       (comparison) =, >=, >, <=, <, <>, IS, LIKE, IN
>       NOT
>       AND
>       OR

Parentheses can be used to change the default precedence.  The parser also supports decimal, octal and hexadecimal integers, as well as floating point and exponential number formats.  The SQL underscore (_) and percent sign (%) wildcards are respected, though these can be escaped as shown below.

>       // For the LIKE to evaluate to true, the value of firstName must start with "George_"
>       "firstName LIKE 'George#_%' ESCAPE '#'";

## Building SqlExprParser
    
Building SqlExprParser is a two step process.  The first step uses JavaCC to generate parser code based on the SqlExprParser.jj language definition.  The output of this step resides in the net.magneticpotato.sqlexpr.javacc.parser package and is saved in SqlExprParser's GitHub repository with all other source code.  The parser code does not normally need to be regenerated, but can be if SqlExprParser.jj content or JavaCC options change.