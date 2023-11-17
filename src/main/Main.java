import java.util.Random;
import java.util.Scanner;
import java.lang.reflect.Method;

public class Main {

	public static long measure(Runnable r) {
		long start = System.nanoTime();
		r.run();
		return System.nanoTime() - start;
	}
	
	public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Functions: \n\tpdf(n, p, k) \n\tcdf(n, p, k) \n\treversePDF_n(p, k, P) \n\treverseCDF_n(p, k, P) \n\treversePDF_k(n, p, P) \n\treverseCDF_k(n, p, P)");

        while(true) {
            System.out.print("~ ");
            String op = sc.nextLine().trim();
            int paramOpen = op.indexOf('('), paramClose = op.indexOf(')');
            
            String func = op.substring(0, paramOpen);
            String[] params = op.substring(paramOpen + 1, paramClose < 0 ? op.length() - 1 : paramClose).split(",");
            for(int i = 0; i < params.length; i++)
                params[i] = params[i].trim();
            
            Method[] methods = Binom.class.getMethods();
            for(Method method : methods) {
                if(method.getName().equals(func)) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Object[] paramObjects = new Object[paramTypes.length];
    
                    if(paramTypes.length != params.length) {
                        System.out.println("expected " + paramTypes.length + " arguments (got " + params.length + ")");
                        return;
                    }
                    
                    for(int i = 0; i < paramTypes.length; i++) {
                        paramObjects[i] = parseParam(params[i], paramTypes[i]);
                        if(paramObjects[i] == null) {
                            System.out.println("couldn't parse '" + params[i] + "' as " + paramTypes[i]);
                            return;
                        }
                    }
    
                    try {
                        System.out.println(method.invoke(null, paramObjects));
                    } catch(Exception e) {
                        System.out.println("couldn't invoke method: " + e.getMessage());
                    }
                }
            }
        }
	}

    public static Object parseParam(String givenParam, Class<?> clazz) {
        try {
            if(double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz))
                return Double.parseDouble(givenParam);
            if(int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz))
                return Integer.parseInt(givenParam);
        } catch(Exception e) {
            return null;
        }
        return null;
    }
	
	public static void randPDF_n() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int k = new Random().nextInt(10);
		int result = Binom.reversePDF_n(p, k, P);
		System.out.println((Binom.pdf(result, p, k) > P ? "\033[0;31m" : "") + "n = " + result + "\033[0m");
	}
	public static void randPDF_k() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int n = new Random().nextInt(20);
		int[] result = Binom.reversePDF_k(n, p, P);
		System.out.println("k = {" + result[0] + (result.length == 1 ? "" : ", " + result[1]) + "}");
	}
	public static void randCDF_n() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int k = new Random().nextInt(10);
		int result = Binom.reverseCDF_n(p, k, P);
		System.out.println((Binom.cdf(result, p, k) > P ? "\033[0;31m" : "") + "n = " + result + "\033[0m");
	}
	public static void randCDF_k() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int n = new Random().nextInt(20);
		int result = Binom.reverseCDF_k(n, p, P);
		System.out.println((Binom.cdf(n, p, result) > P ? "\033[0;31m" : "") + "k = " + result + "\033[0m");
	}
}