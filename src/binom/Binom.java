package binom;

public class Binom {
	public static boolean quiet = false;
	
	interface Event {
		public double prob(int n, double p, int k);
	}
	
	private static long coeff(final int n, int k) {
        if (k>n-k)
            k=n-k;

        long b=1;
        for (int i=1, m=n; i<=k; i++, m--)
            b=b*m/i;
        return b;
    }
	public static double pdf(final int n, final double p, final int k) {
		if(p == 0) return (k == 0 ? 1 : 0);
		return coeff(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
	}
	public static double cdf(final int n, final double p, final int k) {
		if(p == 0) return (k == 0 ? 1 : 0);
		if(k < 0) return 0;
		if(n <= 0) return 0;
		
		double total = 0;
		for(int i = 0; i <= k; i++) {
			total += pdf(n, p, i);
		}
		return total;
	}
	public static int expect(final int n, final double p, Event prob) {
		double e = n * p;
		if(Math.floor(e) != Math.ceil(e)) {
			double floor = prob.prob(n, p, (int) e);
			double ceil = prob.prob(n, p, (int) Math.ceil(e));
			if(floor > ceil) return (int) e;
			else return (int) Math.ceil(e);
		}
		return (int) e;
	}
	


	
	static void println(String info) {
		if(quiet) return;
		System.out.println("[Binom] " + info);
	}
}
