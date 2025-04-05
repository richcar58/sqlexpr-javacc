package net.magneticpotato.sqlexpr.javacc;

import java.util.Map;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.filter.BooleanExpression;
import org.apache.activemq.filter.MessageEvaluationContext;

import jakarta.jms.InvalidSelectorException;
import net.magneticpotato.sqlexpr.javacc.parser.SqlExprParser;

/** This class uses the ActiveMQ selector parser and evaluator to process
 * SQL expressions that return true or false.
 * 
 * @author rcardone
 */
public class SqlExprEvaluator 
{
    /* ********************************************************************** */
    /*                             Public Methods                             */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* parse:                                                                 */
    /* ---------------------------------------------------------------------- */
	/** Parse a string that represents an SQL expression that evaluates to 
	 * true or false.
	 * 
	 *  @param sqlText a sql expression that evaluates to true or false
	 */
    public static BooleanExpression parse(String sqlText) 
     throws SqlExprException
    {
        // The filter should have been checked for obvious problems by the
        // time it gets here, but we want to avoid NPEs no matter what. 
        // Empty and blank strings trigger normal exception handling below.
        if (sqlText == null) {
        	String msg = "Null expression cannot be parsed.";
        	throw new SqlExprException(msg);
        }
        
        // Attempt to parse the filter.
        BooleanExpression expr = null;
        try {expr = SqlExprParser.parse(sqlText);}
        catch (InvalidSelectorException e) {
            
            // Try to collect as much information as the parser makes available.
            String outermsg = e.getMessage();
            String innermsg = null;
            if (e.getCause() != null) innermsg = e.getCause().getMessage();
            
            // Create the new combined error message.
            String msg = "";
            if (outermsg != null) msg = outermsg;
            if (innermsg != null) {
                // Add spacing if we need it.
                if (!msg.isEmpty()) msg += " [";
                msg += innermsg;
                if (!msg.isEmpty()) msg += "]";
            }
            throw new SqlExprException(msg, e);
        }
        
        return expr;
    }
    
    /* ---------------------------------------------------------------------- */
    /* match:                                                                 */
    /* ---------------------------------------------------------------------- */
    /** Parse the SQL text string and then call the real matching method.
     */
    public static boolean match(String sqlText, Map<String, Object> properties) 
     throws SqlExprException
    {
        // Parse the filter.  Null filters are checked in the parse routine.
        return match(parse(sqlText), properties);
    }
        
   /* ---------------------------------------------------------------------- */
    /* match:                                                                 */
    /* ---------------------------------------------------------------------- */
    /** Determine whether the SQL-compliant boolean expression evaluates to TRUE
     * given the specified property values.  This method assumes the parsed 
     * expr is valid and then substitutes the property values if they are provided, 
     * and then evaluates the boolean expression.  
     * 
     * Evaluation succeeds if all identifiers that are referenced have been 
     * replaced with concrete values and those values cause the expression to 
     * evaluate to true. Evaluation will be short-circuited if the truth value
     * of the expression or a subclause is known before evaluation completes, 
     * similar to the way Java short-circuits || and && expressions.  This means
     * that missing values that are not referenced do not cause an error, but
     * missing values that are needed cause the whole expression to return false.
     * 
     * The following URL provides details on evaluation process:
     * 
     *   http://docs.oracle.com/javaee/7/api/javax/jms/Message.html
     * 
     * The property values can only be Boolean, Byte, Short, Integer, Long, 
     * Float, Double, and String; any other values will cause an exception.
     * Property names cannot be null or the empty string.
     *  
     * @param expr the non-null SQL expression to be evaluated
     * @param properties the key/value pairs used for substitution in the filter,
     *            can be null or empty
     * @return true if the filter evaluates to true, false otherwise
     * @throws SqlExprException 
     */
    public static boolean match(BooleanExpression expr, Map<String, Object> properties) 
     throws SqlExprException
    {
        // The easiest (and safest) way to evaluate a filter expression
        // using the provided key/value properties is to use the native
        // ActiveMQ data types and copy all properties to the message.
        //
        // Note that the property values can be be one of the primitive
        // type classes or String, but nothing else.
        ActiveMQTextMessage message = new ActiveMQTextMessage();
        if (properties != null)
            try {message.setProperties(properties);}
             catch (Exception e) {
                 String msg = "Unable to assign input properties.";
                 throw new SqlExprException(msg + " (" + e.getMessage() + ")");
             }
        
        // Set up the context that the evaluation code requires.
        MessageEvaluationContext ctx = new MessageEvaluationContext();
        ctx.setMessageReference(message);
        
        // Evaluate the message with its properties.
        boolean result = false;
        try {result = expr.matches(ctx);}
         catch (Exception e) {
             String msg = "Unable to evaluate SQL expression: " + expr.toString();
             throw new SqlExprException(msg + " (" + e.getMessage() + ")", e);
         }
        return result;
    }
}
