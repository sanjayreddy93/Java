import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple calculator program reading arithmetic expressions from the standard
 * input, evaluating them, and printing the results on the standard output.
 */
public class Calc {
	/**
	 * Evaluates an arithmetic expression. The grammar of accepted expressions
	 * is the following:
	 * 
	 * <code>
	 * 
	 *   expr ::= factor | expr ('+' | '-') expr
	 *   factor ::= term | factor ('*' | '/') factor
	 *   term ::= '-' term | '(' expr ')' | number | id | function | binding
	 *   number ::= int | decimal
	 *   int ::= '0' | posint
	 *   posint ::= ('1' - '9') | posint ('0' - '9')
	 *   decimal ::= int '.' ('0' - '9') | '.' ('0' - '9')
	 *   id ::= ('a' - 'z' | 'A' - 'Z' | '_') | id ('a' - 'z' | 'A' - 'Z' | '_' | '0' - '9')
	 *   function ::= ('sqrt' | 'log' | 'sin' | 'cos') '(' expr ')'
	 *   binding ::= id '=' expr
	 * 
	 * </code>
	 * 
	 * The binary operators are left-associative, with multiplication and division
	 * taking precedence over addition and subtraction.
	 * 
	 * Functions are implemented in terms of the respective static methods of
	 * the class java.lang.Math.
	 * 
	 * The bindings produced during the evaluation of the given expression
	 * are stored in a map, where they remain available for the evaluation
	 * of subsequent expressions.
	 * 
	 * Before leaving this method, the value of the given expression is bound
	 * to the special variable named "_".
	 * 
	 * @param expr well-formed arithmetic expression
	 * @return the value of the given expression
	 */

    /**
     * Method normalizes income string (removes spaces and tabs, converts to lower case, etc),
     * splits into tokens using tokenize method and then sends to reloaded eval method (see below)
     * @param expr string with expression
     * @return value of this expression
     * @throws IOException can be thrown by tokenize method
     */
	public double eval(String expr) throws IOException {
		expr = expr.toLowerCase().replace(" ", "").replace("\t", "").replace("-", "+-");
		if (expr.startsWith("+"))
		    expr = expr.substring(1);
		List<String> tokens = tokenize(expr);
		return eval(tokens);
		// some examples:
		// "1+2*3+4" returns 11.0
		// "(1+2)*(3+4)" returns 21.0
		// "sqrt(2)*sqrt(2) returns 2.0
		// "pi=3.14159265359" returns 3.14159265359
		//     and binds the identifier 'pi' to the same value
		// "cos(pi)" should then return -1
	}

   
    private double eval(List<String> tokens) {
        
        if (tokens.size() == 0)
            return -1;
       
        else if (tokens.size() == 1)
            return Double.parseDouble(tokens.get(0));
     
        else if (tokens.contains("=")) {
            String key = tokens.get(tokens.indexOf("=") - 1);
            double value = eval(tokens.subList(tokens.indexOf("=") + 1, tokens.size()));
            bindings.put(key, value);
            return value;
        }
        
       if (tokens.get(0).equals("-"))
            tokens.add(0, "0");
        
        for (int i = 1; i < tokens.size(); i++)
            if (tokens.get(i-1).equals("+") && tokens.get(i).equals("-"))
                tokens.remove(i-1);
       
        
        Pattern pattern = Pattern.compile(".*[a-zA-Z]+.*");
        Matcher matcher;
        for (String key : bindings.keySet()) {
            matcher = pattern.matcher(key);
            if (matcher.matches())
                for (int i = 0; i < tokens.size(); i++)
                    if (key.equals(tokens.get(i)))
                        tokens.set(i, String.valueOf(bindings.get(key)));
        }
        
        while (tokens.contains(")")) {
            int j = tokens.indexOf(")"); // index of closing bracket )
            int i = j;  // index of opening bracket (
            while (i >= 0 && !(tokens.get(i).equals("(") || tokens.get(i).equals(",")))
                i--;
            String key = "";
            for (int k = i+1; k < j; k++)
                key += tokens.get(k);
            double value = eval(tokens.subList(i+1, j));
            if (i > 0
                    && (tokens.get(i-1).equals("sqrt")
                        || tokens.get(i-1).equals("sin")
                        || tokens.get(i-1).equals("cos")
                        || tokens.get(i-1).equals("log"))) {
                i--;
                key = tokens.get(i) + "(" + String.valueOf(value) + ")";
                value = tokens.get(i).equals("sqrt") ? Math.sqrt(value)
                        : tokens.get(i).equals("sin") ? Math.sin(value)
                        : tokens.get(i).equals("cos") ? Math.cos(value)
                        : tokens.get(i).equals("log") ? Math.log(value) : -1;
            }
            if (j - i > 2)      
                bindings.put(key, value);
           
            
            ArrayList<String> newTokens = new ArrayList<>();
            newTokens.addAll(tokens.subList(0, i));
            newTokens.add(String.valueOf(value));
            newTokens.addAll(tokens.subList(j+1, tokens.size()));
            return eval(newTokens);
        }
        
        while (tokens.contains("*") || tokens.contains("/")) {
            int i = Math.max(tokens.indexOf("*"), tokens.indexOf("/"));
            String key = tokens.get(i-1) + tokens.get(i) + tokens.get(i+1);
            double value = tokens.get(i).equals("*")
                    ? Double.parseDouble(tokens.get(i-1)) * Double.parseDouble(tokens.get(i+1))
                    : Double.parseDouble(tokens.get(i-1)) / Double.parseDouble(tokens.get(i+1));
            bindings.put(key, value);
            ArrayList<String> newTokens = new ArrayList<>();
            newTokens.addAll(tokens.subList(0, i-1));
            newTokens.add(String.valueOf(value));
            newTokens.addAll(tokens.subList(i+2, tokens.size()));
            return eval(newTokens);
        }
      
        while (tokens.contains("+") || tokens.contains("-")) {
            int i = Math.max(tokens.indexOf("+"), tokens.indexOf("-"));
            String key = tokens.get(i-1) + tokens.get(i) + tokens.get(i+1);
            double value = tokens.get(i).equals("+")
                    ? Double.parseDouble(tokens.get(i-1)) + Double.parseDouble(tokens.get(i+1))
                    : Double.parseDouble(tokens.get(i-1)) - Double.parseDouble(tokens.get(i+1));
            bindings.put(key, value);
            ArrayList<String> newTokens = new ArrayList<>();
            newTokens.addAll(tokens.subList(0, i-1));
            newTokens.add(String.valueOf(value));
            newTokens.addAll(tokens.subList(i+2, tokens.size()));
            return eval(newTokens);
        }
       
        return 0;
    }

    /**
     * Method splits a string into tokens: double values or words or arithmetical signs
     * @param s initial string with the expression
     * @return ArrayList of tokens
     * @throws IOException can be thrown by StreamTokenizer
     */
    public List<String> tokenize(String s) throws IOException {
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));
		tokenizer.ordinaryChar('/');  // Don't treat slash as a comment start.
        ArrayList<String> tokBuf = new ArrayList<String>();
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			switch(tokenizer.ttype) {
				case StreamTokenizer.TT_NUMBER:
					tokBuf.add(String.valueOf(tokenizer.nval));
					break;
				case StreamTokenizer.TT_WORD:
					tokBuf.add(tokenizer.sval);
					break;
				default:  // operator
					tokBuf.add(String.valueOf((char) tokenizer.ttype));
			}
		}
		return tokBuf;
	}

    /**
     * Returns all sored bindings like "1.0+2.0 = 3.0"
     * @return TreeMap of bindings
     */
	public Map<String,Double> bindings() {
		return bindings;
	}

    /**
     * Contains bindings like "1.0+2.0 = 3.0". Key will be
     * the expression (1.0+2.0) value its calculated value (3.0)
     */
	private final Map<String,Double> bindings = new TreeMap<>();

    /**
     * Class entry point. Reads expressions or commands from
     * console and outputs expression value
     * @param args ignored
     * @throws IOException can be thrown by reader, writer or eval method
     */
	public static void main(String[] args) throws IOException {
		Calc calc = new Calc();
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				PrintWriter out = new PrintWriter(System.out, true)) {
			
			while (true) {
				
				String line = in.readLine();
				
				if (line == null) {
					break;
				}
				
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				
				try {
					if (!line.startsWith(":")) {
						// handle expression
						out.println(calc.eval(line));
					} else {
						// handle command
						String[] command = line.split("\\s+", 2);
						switch (command[0]) {
							case ":vars":
								calc.bindings().forEach((name, value) ->
										out.println(name + " = " + value));
								break;
								
							case ":clear":
								if (command.length == 1) {
									// clear all
									calc.bindings().clear();
								} else {
									// clear requested
									calc.bindings().keySet().removeAll(Arrays.asList(command[1].split("\\s+")));
								}
								break;
							case ":exit":
							case ":quit":
								System.exit(0);
								break;
							default:
								throw new RuntimeException("unrecognized command: " + line);
						}
					}
				} catch (Exception ex) {
					System.err.println("*** ERROR: " + ex.getMessage());
				}
			}
		}
	}
}
