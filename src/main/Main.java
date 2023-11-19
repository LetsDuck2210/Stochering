package main;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import binom.Binom;

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
			"reversePDF_n(p, k, P)",
			"reversePDF_k(n, p, P)",
			"reversePDF_p(n, k, P, accuracy)",
			"reverseCDF_n(p, k, P)",
			"reverseCDF_k(n, p, P)",
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
            
            Method[] methods = Binom.class.getMethods();
            Method matching = null;
            for(Method method : methods) {
                if(matchesFuzzy(method.getName(), func)) {
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
	
	public static void randPDF_n() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int k = new Random().nextInt(10);
		int result = Binom.reversePDF_n("" + p, "" + k, "" + P);
		System.out.println((Binom.pdf(result, p, k) > P ? "\033[0;31m" : "") + "n = " + result + "\033[0m");
	}
	public static void randPDF_k() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int n = new Random().nextInt(20);
		int[] result = Binom.reversePDF_k("" + n, "" + p, "" + P);
		System.out.println("k = {" + result[0] + (result.length == 1 ? "" : ", " + result[1]) + "}");
	}
	public static void randCDF_n() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int k = new Random().nextInt(10);
		int result = Binom.reverseCDF_n("" + p, "" + k, "" + P);
		System.out.println((Binom.cdf(result, p, k) > P ? "\033[0;31m" : "") + "n = " + result + "\033[0m");
	}
	public static void randCDF_k() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int n = new Random().nextInt(20);
		int result = Binom.reverseCDF_k("" + n, "" + p, "" + P);
		System.out.println((Binom.cdf(n, p, result) > P ? "\033[0;31m" : "") + "k = " + result + "\033[0m");
	}
}