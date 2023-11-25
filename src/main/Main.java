package main;

import static java.math.BigDecimal.ZERO;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import binom.Binom;
import binom.CDF;
import binom.PDF;
import main.commands.Clear;
import main.commands.Command;
import main.commands.Exit;
import main.commands.Help;
import main.commands.OptionCmd;
import main.options.Option;
import main.options.OutputAccuracy;
import main.options.Scale;

public class Main {
	public static Map<String, Command> commands = Stream.of(new Object[][] {
		{ "exit", new Exit() },
		{ "help", new Help() },
		{ "option", new OptionCmd() },
		{ "clear", new Clear() }
	}).collect(Collectors.toMap(data -> (String) data[0], data -> (Command) data[1]));
	public static Option<?>[] options = {
		new OutputAccuracy(),
		new Scale()
	};
	private static boolean running = true;
	
	public static void main(String[] _a) {
		Scanner sc = new Scanner(System.in);

		System.out.println("custom functions:");
		String[] methodDefs = {
			"cdf(n, p, k)",
			"pdf(n, p, k)",
			"reversePDF_n(p, k, P[, min, max])",
			"reversePDF_k(n, p, P[, min, max])",
			"reversePDF_p(n, k, P, accuracy)",
			"reverseCDF_n(p, k, P[, min, max])",
			"reverseCDF_k(n, p, P[, min, max])",
			"reverseCDF_p(n, k, P, accuracy)"
		};
		
		for(String method : methodDefs) {
			System.out.println("\t" + method);
		}
		
        while(running) {
        	try {
	            System.out.print("~ ");
	            String op = sc.nextLine().trim();
	            if(op.isEmpty())
	            	continue;
	            
	            if(op.matches("\\/\\w+( \\\".*\\\"| [\\w\\d]*)*")) {
	            	int space = op.indexOf(' ');
	            	String 	command = op.substring(1, space < 0 ? op.length() : space),
	            			argString = (op.contains(" ") ? op.substring((space + 1) > 0 ? space + 1 : op.length()) : "");
	            	List<String> args = new ArrayList<>();
	            	if(!argString.isEmpty()) args.add("");
	            	boolean quoted = false;
	            	for(int i = 0, j = 0; i < argString.length(); i++) {
	            		char c = argString.charAt(i);
	            		if(c == '"')
	            			quoted = !quoted;
	            		else if(c == ' ' && !quoted) 
	            			args.add(++j, "");
	            		else args.set(j, args.get(j) + c);
	            	}
	            	
	            	if(!commands.containsKey(command)) {
	            		System.out.println("no such command: " + command);
	            		continue;
	            	}
	            	commands.get(command).exec(args.toArray(new String[0]));
	            	continue;
	            }
	            
	            Equation eq = new Equation(op, "");
	            Object out = eq.evaluate(ZERO);
	            if(out == null) continue;
	            CustomFields.ans = out;
	            if(out.getClass().isArray()) {
	            	int len = Array.getLength(out);
	            	for(int i = 0; i < len; i++) {
	            		System.out.print((i == 0 ? "{" : "") + Array.get(out, i) + (i < len - 1 ? ", " : "}\n"));
	            	}
	            } else if(out instanceof Double || out instanceof BigDecimal && Main.<Integer>getOption("output_accuracy") != -1) {
	            	System.out.println(String.format("%." + getOption("output_accuracy") + "f", out).replaceAll("0*$", "").replaceAll("\\.$", ""));
	            } else
	            	System.out.println(out);
        	} catch(NumberFormatException e) {
        		System.out.println("Not a Number Error: " + e.getMessage());
        	} catch(Exception e) {
        		System.out.println("Unknown Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());        		
    			e.printStackTrace();
        	}
        }
        sc.close();
	}
	public static void stop() {
		running = false;
	}
	
	public static Object setOption(String name, Object value) {
		for(int i = 0; i < options.length; i++)
			if(options[i].name().equalsIgnoreCase(name) && options[i].type().isAssignableFrom(value.getClass()))
				return options[i].set(value);
		return null;
	}
	@SuppressWarnings("unchecked")
	public static <T> T getOption(String name) {
		for(int i = 0; i < options.length; i++)
			if(options[i].name().equalsIgnoreCase(name))
				return (T) options[i].value();
		return null;
	}
	public static Class<?> getOptionType(String name) {
		for(int i = 0; i < options.length; i++)
			if(options[i].name().equalsIgnoreCase(name))
				return options[i].type();
		return null;
	}
	public static boolean matchesFuzzy(String a, String b) {
		String regex = "";
		for(int i = 0; i < b.length(); i++) {
			regex += b.charAt(i) + (i < b.length() - 1 ? ".*" : "");
		}
		return a.matches(regex);
	}
	public static Object invokeFunction(String fuzzyName, String[] params) throws InvocationTargetException, IllegalAccessException {
		Method[] methods = getAllFunctions();
        Method matching = null;
        for(Method method : methods) {
            if(matchesFuzzy(method.getName(), fuzzyName) && method.getParameterCount() == params.length) {
            	if(matching != null) {
            		System.out.println("ambiguous function name: matches " + matching.getName() + " and " + method.getName());
            		return null;
            	}
                matching = method;
            }
        }
        if(matching == null) {
        	System.out.println("no function found for name " + fuzzyName);
        	return null;
        } 
        Class<?>[] paramTypes = matching.getParameterTypes();
        Object[] paramObjects = new Object[paramTypes.length];

        if(paramTypes.length != params.length) {
            System.out.println("expected " + paramTypes.length + " arguments (got " + params.length + ")");
            return null;
        }
        
        for(int i = 0; i < paramTypes.length; i++) {
            paramObjects[i] = parseParam(params[i], paramTypes[i]);
            if(paramObjects[i] == null) {
                System.out.println("couldn't parse '" + params[i] + "' as " + paramTypes[i]);
                continue;
            }
        }

        Object out = matching.invoke(null, paramObjects);
        return out;
	}
	public static Object getField(String fuzzyName) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = getAllFields();
		
		Field matching = null;
		for(Field field : fields) {
            if(matchesFuzzy(field.getName(), fuzzyName)) {
            	if(matching != null) {
            		System.out.println("ambiguous function name: matches " + matching.getName() + " and " + field.getName());
            		return null;
            	}
                matching = field;
            }
        }
        if(matching == null) {
        	System.out.println("no function found for name " + fuzzyName);
        	return null;
        }
        
        return matching.get(null);
	}
	
	private static Method[] getAllFunctions() {
		Method[][] all = {
			Binom.class.getMethods(),
			CDF.class.getMethods(),
			PDF.class.getMethods(),
			Math.class.getMethods()
		};
		
		int len = 0;
		for(int i = 0; i < all.length; i++) len += all[i].length;
		
		Method[] onedim = new Method[len];
		for(int i = 0, f = 0; i < all.length; i++)
			for(int j = 0; j < all[i].length; j++, f++)
				onedim[f] = all[i][j];
		return onedim;
	}
	private static Field[] getAllFields() {
		Field[][] all = {
			Binom.class.getFields(),
			CDF.class.getFields(),
			PDF.class.getFields(),
			Math.class.getFields(),
			CustomFields.class.getFields()
		};

		int len = 0;
		for(int i = 0; i < all.length; i++) len += all[i].length;
		
		Field[] onedim = new Field[len];
		for(int i = 0, f = 0; i < all.length; i++)
			for(int j = 0; j < all[i].length; j++, f++)
				onedim[f] = all[i][j];
		return onedim;
	}

    public static Object parseParam(String givenParam, Class<?> clazz) {
        try {
        	if(String.class.isAssignableFrom(clazz))
        		return givenParam;
            if(double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz))
                return new Equation(givenParam, "").evaluateDouble(ZERO).doubleValue();
            if(int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz))
                return new Equation(givenParam, "").evaluateInt(ZERO).intValue();
            if(BigInteger.class.isAssignableFrom(clazz))
            	return new Equation(givenParam, "").evaluateInt(ZERO);
            if(BigDecimal.class.isAssignableFrom(clazz))
            	return new Equation(givenParam, "").evaluateDouble(ZERO);
        } catch(Exception e) {
            return null;
        }
        return null;
    }
	
}