package net.magneticpotato.sqlexpr.javacc.parser;

import jakarta.jms.InvalidSelectorException;

import org.apache.activemq.filter.BooleanExpression;
import org.testng.annotations.Test;

/** Basic tests of the generated parser.
 * 
 * @author rcardone
 */
@Test(groups= {"unit"})
public class ParserTest 
{
    /* ********************************************************************** */
    /*                              Test Methods                              */
    /* ********************************************************************** */    
    /* ---------------------------------------------------------------------- */
    /* goodFilters:                                                           */
    /* ---------------------------------------------------------------------- */
    @Test(enabled=true)
    public void goodFilters() throws InvalidSelectorException
    {
        // Test a simple conjunction.
        String sqlText = "name = '" + "Bud" + "' AND tenant_id = '" + "iplantc.org" + "'";
        var ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n1");
//        System.out.println(ast);
        
        // Test more complex expressions.
        sqlText = "int1 > 66 AND int2 <> 5 AND (name LIKE 'Jo%n' OR range BETWEEN 200 AND 300)";
        ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n2");
//        System.out.println(ast);
        
        // Test datetime ranges.  Dates can only be handled using epoch time.
        long millis = System.currentTimeMillis();
        sqlText = "date BETWEEN " + (millis - 10000) + " AND " + (millis - 1000);
        ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n3");
//        System.out.println(ast);

        // Test NOT LIKE filter.
        sqlText = "name NOT LIKE 'Bi__y'";
        ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n4");
//        System.out.println(ast);
        
        // Test escape characters.
        // Note that any character can be used to escape _ and % in LIKE clauses.
        sqlText = "name LIKE 'George#_%' ESCAPE '#'";
        ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n5");
//        System.out.println(ast);
        
        // Test IN.
        sqlText = "country IN ('UK', 'US')";
        ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n6");
//        System.out.println(ast);
        
        // Test NULL
        sqlText = "missing is NULL";
        ast = SqlExprParser.parse(sqlText);
//        System.out.println("\n7");
//        System.out.println(ast);
        
        // Test mod
        sqlText = "x > 15 % 10";
        ast = SqlExprParser.parse(sqlText);
    }

    /* ---------------------------------------------------------------------- */
    /* badFilters:                                                            */
    /* ---------------------------------------------------------------------- */
    /** Each of these expressions are expected to be invalid and throw a parser
     * exception.  If no exception is thrown, this test fails.
     * 
     */
    @Test(enabled=true)
    public void badFilters()
    {
        // Test a simple conjunction.
        boolean exceptionOccurred = false;
        String sqlText = "name = '" + "Bud" + "' tenant_id = '" + "iplantc.org" + "'";
        BooleanExpression ast;
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);
        
        // Test more complex expressions.
        exceptionOccurred = false;
        sqlText = "int1 > AND int2 <> 5 AND (name LIKE 'Jo%n' OR range BETWEEN 200 AND 300)";
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);
        
        // Test datetime ranges.  Dates can only be handled using epoch time.
        exceptionOccurred = false;
        long millis = System.currentTimeMillis();
        sqlText = "date BETWEEN " + (millis - 10000) + " AND ";
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);

        // Test NOT LIKE filter.
        exceptionOccurred = false;
        sqlText = "name NOT LIKE '";
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);
        
        // Test escape characters.
        // Note that any character can be used to escape _ and % in LIKE clauses.
        exceptionOccurred = false;
        sqlText = "LIKE 'George\\_%' ESCAPE '\\'";
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);
        
        // Test IN.
        exceptionOccurred = false;
        sqlText = "country IN ('UK', 'US'";
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);
        
        // Test REGEX as an indication that all function calls have been removed
        // from the language.  If we want to add function calls in the future, we
        // can selective remove the built-in function by deregistering them:
        // --> FunctionCallExpression.deregisterFunction("REGEX");
        exceptionOccurred = false;
        sqlText = "REGEX('^a.c', 'abc')"; // hardcoded regex and value example
        try {ast = SqlExprParser.parse(sqlText);}
            catch (InvalidSelectorException e) {exceptionOccurred = true;}
        if (!exceptionOccurred) throw new IllegalArgumentException(sqlText);
  }
}
