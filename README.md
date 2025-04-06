# SqlExprParser

This project provides a parser and evaluator for boolean expressions written in a SQL-like language.  The language leverages SQL's simplicity and wide adoption, and is especially well-suited to represent end-user conditional logic, such as user-defined email routing or message queue selection.  No SQL database is required.  

The language is a subset of the standard SQL expression language used in WHERE clauses.  Expressions are strings that can be parsed using the `SqlExprEvaluator.parse(String)` method.  If an expression is valid, a BooleanExpression object is returned.  This object, along with a map of name/value pairs, is evaluated using the `SqlExprEvaluator.match(BooleanExpression, Map)` method.  For convenience, the `SqlExprEvaluator.match(String, Map)` method both parses and evaluates an expression.  Here's an example usage of SqlExprParser:

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

See the test programs in the `src/test/java` subtree for more examples.

## Implementation and Syntax
SqlExprParser shamelessly borrows open source code from [Apache ActiveMQ](https://activemq.apache.org/) and the Texas Advanced Computing Center's [Tapis](https://github.com/tapis-project) project.  In particular, implementations of ActiveMQ's [BooleanExpression](https://activemq.apache.org/components/classic/documentation/maven/apidocs/org/apache/activemq/filter/BooleanExpression.html) are used to evaluate SqlExprParser expressions.  SqlExprParser is generated using the [JavaCC](https://javacc.github.io/javacc/) parser generator.

SqlExprParser recognizes a language that includes the following arithmetic, comparison and logical operators in order of precedence, high to low:

>       (unary) +, -
>       *, /, % (mod)
>       +, -
>       (comparison) =, >=, >, <=, <, <>, IS, LIKE, BETWEEN, IN
>       NOT
>       AND
>       OR

Parentheses can be used to change the default precedence.  The parser supports decimal, octal and hexadecimal integers, as well as floating point and exponential number formats.  The SQL underscore (_) and percent sign (%) wildcards are respected, though these can be escaped as shown below.

>       // For the LIKE to evaluate to true, the value of firstName must start with "George_"
>       "firstName LIKE 'George#_%' ESCAPE '#'";

## Building SqlExprParser
    
Instructions assume Linux or a Unix-like operating system.

Building SqlExprParser is a two step process, both of which are automated using Maven (3.9.9+) and the SqlExprParser pom.xml file.  From SqlExprParser's top-level directory, simple issue this command on the command line to compile the code and package it in *target/sqlexprlib.jar*: 

>       mvn clean install
  
The above Maven command will also install sqlexpr-javacc in your local Maven (~/.m2) repository.  Compilation assumes Java 21 or above, but the compiled class files will execute on JVMs supporting Java 17 or above.  
  
The first step in the build uses JavaCC to generate parser source code based on the language defined in *SqlExprParser.jj*.  The output of this step resides in the `net.magneticpotato.sqlexpr.javacc.parser` package and is saved in SqlExprParser's GitHub repository with all other source code.  This parser code does not normally need to be regenerated, but can be if *SqlExprParser.jj* content or the JavaCC options used to generate the code change.

### Other Ways to Generate Parser Source Code

If you'd like to regenerate the parser source code from within an IDE such as Eclipse or IntelliJ, run the `BuildSqlExprParser` program.  The IDE will automatically arrange for the required dependencies to be on the Java CLASSPATH and run JavaCC.  

Alternatively, you can manually assign the dependencies from the pom.xml file to a CLASSPATH environment variable and run JavaCC from the command line.  Below is an example of the commands needed to generate the parser source code (using dependency versions at the time of this writing).  We assume that Java JDK and JavaCC are on the PATH and that the current directory is *sqlexpr-javacc/src/main/resources*.

>       export CLASSPATH=<path-to-ActiveMQ>/activemq-client-6.1.6.jar:<path-to-JavaCC>/javacc.jar
>       scripts/javacc SqlExprParser.jj

## Support

Support is on a best effort basis by creating GitHub issues on this repository.  The developer/maintainer can be contacted at *rcdev58 at pm.me*.

## Licenses

SqlExprParser is licensed under the MIT license in the top-level directory.  Licenses it uses are in the `licenses` directory.  
