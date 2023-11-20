package main;

import java.lang.reflect.Array;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Scanner;

import binom.Binom;
import binom.CDF;
import binom.PDF;

import static java.math.BigDecimal.ZERO;

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
		
        while(true) {
            System.out.print("~ ");
            String op = sc.nextLine().trim();
            if(op.equalsIgnoreCase("exit"))
            	break;
            if(op.trim().equals(""))
            	continue;
            
            Equation eq = new Equation(op, "");
            Object out = eq.evaluate(ZERO);
            if(out.getClass().isArray()) {
            	int len = Array.getLength(out);
            	for(int i = 0; i < len; i++) {
            		System.out.print((i == 0 ? "{" : "") + Array.get(out, i) + (i < len - 1 ? ", " : "}\n"));
            	}
            } else {
            	System.out.println(out);
            }
        }
        sc.close();
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
	private static Method[] getAllFunctions() {
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