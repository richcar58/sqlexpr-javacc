package net.magneticpotato.sqlexpr.javacc.generator;

/** This class generates the sql expression language subset supported for tapis searching.
 * The default location of the javacc grammar file is relative to the project root directory.
 * If the current directory is something other than the project root directory, the path
 * to the grammar file can be passed in as an argument.
 * 
 * See the project README file for usage information.
 * 
 * @author rcardone
 */
public class BuildSqlExprParser 
{
    // Grammar file pathname relative to project directory.
    public static final String DEFAULT_GRAMMAR_FILE = "src/main/resources/SqlExprParser.jj";
    
    // Run the javacc parser generator to create the sql expression language.
    public static void main(String[] args) throws Exception 
    {
        // Optional user input overrides the default.
        String path = args.length > 0 ? args[0] : DEFAULT_GRAMMAR_FILE;
        System.out.println("Javacc input file: " + path);
        
        // We assume that we are in the project root directory.
        int result = org.javacc.parser.Main.mainProgram(new String[] {path});
        if (result != 0) System.out.println("ERROR: JavaCC parser parser code: " + result);
    }
}
