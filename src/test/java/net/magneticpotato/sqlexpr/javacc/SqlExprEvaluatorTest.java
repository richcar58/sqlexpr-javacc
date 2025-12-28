package net.magneticpotato.sqlexpr.javacc;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.magneticpotato.sqlexpr.javacc.SqlExprException;

@Test(groups= {"unit"})
public class SqlExprEvaluatorTest 
{
    /* ********************************************************************** */
    /*                              Test Methods                              */
    /* ********************************************************************** */    
    /* ---------------------------------------------------------------------- */
    /* evalTest1:                                                             */
    /* ---------------------------------------------------------------------- */
	@Test(enabled=true)
	public void evalTest1() throws SqlExprException
	{
		// Reusable map.
		var props = new HashMap<String,Object>();
		long millis = System.currentTimeMillis();
		
		// -----------------------------------------------------------------
        // Test a simple conjunction.
        String sqlText = "name = '" + "Bud" + "' AND tenant_id = '" + "iplantc.org" + "'";
        
        // --- TRUE
        props.put("name", "Bud");
        props.put("tenant_id", "iplantc.org");
        var b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);
        
        // --- FALSE
        props.put("tenant_id", "bad_tenant");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);
        
        // --- FALSE
        props.put("name", "Harry");
        props.put("tenant_id", "iplantc.org");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);
        
        // --- FALSE
        // Fails before getting to tenant_id test.
        props.clear();
        props.put("name", "Harry");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);

        // --- FALSE
        // Fails even though tenant_id is referenced but not set.
        props.put("name", "Bud");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);

        // ----------------------------------------------------------------- 
        // Test more complex expressions.
        props.clear();
        sqlText = "int1 > 66 AND int2 <> 5 AND (name LIKE 'Jo%n' OR range BETWEEN 200 AND 300)";
        
        // --- TRUE
        props.put("int1", 100);
        props.put("int2", 9);
        props.put("name", "John");
        props.put("range", 250);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);

        // --- TRUE
        props.put("name", "Betsy");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);
        
        // --- TRUE
        props.put("range", 200);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);
        
        // --- TRUE
        props.put("range", 300);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);
        
        // --- FALSE
        props.put("range", 199);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);
        
        // --- FALSE
        props.put("range", 301);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);

        // --- FALSE
        props.put("range", 300.0001);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);
	
        // ----------------------------------------------------------------- 
        // Short-circuit and missing value testing.
        props.clear();
        sqlText = "int1 = 1 OR int2 = 2 OR int3 = 3";
        
        // --- FALSE
        // This shows that missing values cause clauses to evaluate to false without
        // causing an error.
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b); 
        
        // --- TRUE
        // This shows that missing int1 and int2 values cause their clauses to evaluate
        // to false, but int3 is still true so the overall expression evaluates to true.
        // This tolerant behavior appears to be a feature not a bug.
        props.put("int3", 3);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  

        // -----------------------------------------------------------------
        // Test datetime ranges.  Dates can only be handled using epoch time.
        props.clear();
        sqlText = "date BETWEEN " + (millis - 1000) + " AND " + (millis + 3000);
        
        // --- TRUE
        props.put("date", System.currentTimeMillis());
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- FALSE
        props.put("date", System.currentTimeMillis() + 3000);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);  
        
        // -----------------------------------------------------------------
        // Test wildcard character.
        props.clear();
        sqlText = "name NOT LIKE 'Bi__y'";

        // --- TRUEassertTrue
        props.put("name", "Bily");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- FALSE
        props.put("name", "Billy");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);  
        
        // --------------------------------------------------------------FR---
        // Note that any character can be used to escape _ and % in LIKE clauses.
        props.clear();
        sqlText = "name LIKE 'George#_%' ESCAPE '#'";
        
        // --- TRUE
        props.put("name", "George_");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- TRUE
        props.put("name", "George_1");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- TRUE
        props.put("name", "George_123");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- FALSE
        props.put("name", "George");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);  
        
        // --- FALSE
        props.put("nTRUEame", "George123");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);  
        
        // -----------------------------------------------------------------
        // Test IN.
        props.clear();
        sqlText = "country IN ('UK', 'US')";
        
        // --- TRUE
        props.put("country", "UK");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- TRUE
        props.put("country", "US");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- FALSE
        props.put("country", "FR");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);  
        
        // -----------------------------------------------------------------
        // Test NULL
        props.clear();
        sqlText = "missing is NULL";

        // --- TRUE
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  

        // --- TRUE
        props.put("missing", null);
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertTrue(b);  
        
        // --- FALSE
        props.put("missing", "I'm here!");
        b = SqlExprEvaluator.match(sqlText, props);
        Assert.assertFalse(b);  
	}
	
    /* ---------------------------------------------------------------------- */
    /* evalTest2:                                                             */
    /* ---------------------------------------------------------------------- */
	/** Tests type restrictions for various operators. */
	@Test(enabled=false)  // Set to true to run tests.
	public void evalTest2() throws SqlExprException
	{
		// Reusable map.
		var props = new HashMap<String,Object>();
		String sqlText = "price IN (10, 20)";
    
		// The match call will fail with a parse error:
		// Encountered " <DECIMAL_LITERAL> "10 "" at line 1, column 11. Was expecting: <STRING_LITERAL> ...
		// ** Conclusion: IN can only take String list elements.
		props.put("price", 10);
		var b = SqlExprEvaluator.match(sqlText, props);
		Assert.assertFalse(b); 
	}
}
