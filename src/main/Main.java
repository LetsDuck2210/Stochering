package main;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Scanner;

import binom.Binom;
import binom.CDF;
import binom.PDF;

public class Main {

	public static long measure(Runnable r) {
		long start = System.nanoTime();
		r.run();
		return System.nanoTime() - start;
	}
	
	public static boolean matchesFuzzy(String a, String b) {
		String regex = "";
		for(int i = 0; i < b.length(); i++) {
			regex += b.charAt(i) + (i < b.length() - 1 ? ".*" : "");
		}
		return a.matches(regex);
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.println("functions:");
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
		
        outer: while(true) {
            System.out.print("~ ");
            String op = sc.nextLine().trim();
            if(op.equalsIgnoreCase("exit"))
            	break;
            if(op.isBlank())
            	continue;
            
            int paramOpen = op.indexOf('('), paramClose = op.indexOf(')');
            
            String func = op.substring(0, paramOpen);
            String[] params = op.substring(paramOpen + 1, paramClose < 0 ? op.length() : paramClose).split(",");
            for(int i = 0; i < params.length; i++)
                params[i] = params[i].trim();
            
            Method[] methods = getAllMethods();
            Method matching = null;
            for(Method method : methods) {
                if(matchesFuzzy(method.getName(), func) && method.getParameterCount() == params.length) {
                	if(matching != null) {
                		System.out.println("ambiguous function name: matches " + matching.getName() + " and " + method.getName());
                		continue outer;
                	}
                    matching = method;
                }
            }
            final String matchingName = matching.getName();
            if(matching == null 
            		|| !List.of(methodDefs).stream().anyMatch(m -> m.startsWith(matchingName))) { // don't want equals(..) notify(..) etc.
            	System.out.println("no function found for name " + func);
            	continue;
            }
            Class<?>[] paramTypes = matching.getParameterTypes();
            Object[] paramObjects = new Object[paramTypes.length];

            if(paramTypes.length != params.length) {
                System.out.println("expected " + paramTypes.length + " arguments (got " + params.length + ")");
                continue;
            }
            
            for(int i = 0; i < paramTypes.length; i++) {
                paramObjects[i] = parseParam(params[i], paramTypes[i]);
                if(paramObjects[i] == null) {
                    System.out.println("couldn't parse '" + params[i] + "' as " + paramTypes[i]);
                    continue;
                }
            }

            try {
                Object out = matching.invoke(null, paramObjects);
                if(out.getClass().isArray()) {
                	final int len = Array.getLength(out);
                	System.out.print("{");
                	for(int i = 0; i < len; i++)
                		System.out.print(Array.get(out, i) + (i < len - 1 ? ", " : ""));
                	System.out.println("}");
                } else
                	System.out.println(out);
            } catch(Exception e) {
                System.out.println("couldn't invoke method: " + e.getMessage());
            }
            continue;
        }
        sc.close();
	}
	
	private static Method[] getAllMethods() {
		Method[][] all = {
			Binom.class.getMethods(),
			CDF.class.getMethods(),
			PDF.class.getMethods()
		};
		
		int len = 0;
		for(int i = 0; i < all.length; i++) len += all[i].length;
		
		Method[] onedim = new Method[len];
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
                return new Equation(givenParam, "").evaluate(0);
            if(int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz))
                return (int) new Equation(givenParam, "").evaluate(0);
        } catch(Exception e) {
            return null;
        }
        return null;
    }
	
}