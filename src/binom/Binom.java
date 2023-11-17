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
		return coeff(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
	}
	public static double cdf(final int n, final double p, final int k) {
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
	
	/** attempts to find n for P(X = k) <= P
	 * @param p probability of a single success
	 * @param k amount of successes expected
	 * @param P expected probability of event
	 * @return n amount of turns required
	 */
	public static int reversePDF_n(final double p, final int k, final double P) {
		println("started reversePDF_n with p=" + p + ", k=" + k + ", P=" + P);
		final int range = 10000;
		int min = k, max = k + range;
		for(double n = min + range / 2; n < k + range;) {
			int possibs = Math.abs(min - max);
			double prob = pdf((int) n, p, k);
			if(possibs <= 5)
				println("n=" + (int) n + ", pdf(n, p, k)=" + prob);
			
			boolean inc; // increase or decrease n
			if(prob <= P) {
				if(possibs <= 2)
					return (int) n;
				inc = n * p < k;
			} else
				inc = n * p > k;
			
			if(inc) {
				min = (int) Math.floor(n);
				n += (max - n) / 2;
			} else {
				max = (int) Math.ceil(n);
				n -= (n - min) / 2;
			}
		}
		return -1;
	}

	/**
	 * attempts to find p for P(X = k) <= P
	 * @param n number of trials
	 * @param p probability of a single success
	 * @param P max probability of event
	 */
	public static int[] reversePDF_k(final int n, final double p, final double P) {
		println("started reversePDF_k with n=" + n + ", p=" + p + ", P=" + P);
		if(n == 0) return new int[] {0};
		int left = reversePDF_k_oneside(n, p, P, true);
		int right = reversePDF_k_oneside(n, p, P, false);
		if(pdf(n, p, left) > pdf(n, p, right)) return new int[] {left};
		if(pdf(n, p, right) > pdf(n, p, left)) return new int[] {right};
		return new int[] {left, right};
	}
	private static int reversePDF_k_oneside(final int n, final double p, final double P, boolean leftside) {
		int expect = expect(n, p, Binom::pdf);
		
		int min = leftside ? 0 : expect;
		int max = leftside ? expect : n;
		int lastK = -1, lowestK = -1;
		for(double k = min + (max - min)/2;;) {
			if((int) k == lastK) return lowestK;
			lastK = (int) k;
			double prob = pdf(n, p, (int) k);
			int possibs = max - min;
			if(possibs <= 5)
				println("(" + (leftside ? "left" : "right") + ") k=" + (int) k + ", pdf(n, p, k)=" + prob);
			if(prob <= P) {
				if(possibs <= 2)
					return (int) k;
				lowestK = (int) k;
				min = (int) Math.floor(k);
				k += (max - k) / 2;
			} else {
				max = (int) Math.ceil(k);
				k -= (k - min) / 2;
			}
		}
	}
	
	/** attempts to find n for P(X <= k) <= P
	 * @param p probability of a single success
	 * @param k amount of successes expected
	 * @param P max probability of event
	 * @return n amount of turns required
	 */
	public static int reverseCDF_n(final double p, final int k, final double P) {
		println("started reverseCDF_n with p=" + p + ", k=" + k + ", P=" + P);
		final int range = 10000;
		int min = k, max = k + range, lastN = -1, lowestN = -1;
		for(double n = min + range / 2; n < k + range;) {
			if((int) n == lastN) return lowestN;
			int possibs = Math.abs(min - max);
			double prob = cdf((int) n, p, k);
			if(possibs <= 5)
				println("n=" + (int) n + ", cdf(n, p, k)=" + prob);
			lastN = (int) n;
			if(prob <= P) {
				if(possibs <= 2)
					return (int) n;
				lowestN = (int) n;
				max = (int) Math.ceil(n); 
				n -= (n - min) / 2;
			} else {
				min = (int) Math.floor(n);
				n += (max - n) / 2;
			}
		}
		return -1;
	}
	/**
	 * attempts to find p for P(X = k) <= P
	 * @param n number of trials
	 * @param p probability of a single success
	 * @param P max probability of event
	 */
	public static int reverseCDF_k(final int n, final double p, final double P) {
		println("started reverseCDF_k with n=" + n + ", p=" + p + ", P=" + P);
		if(n == 0) return 0;
		
		int min = 0, max = n,
			lastK = -1, lowestK = -1;
		for(double k = min + (max - min)/2;;) {
			if((int) k == lastK) return lowestK;
			lastK = (int) k;
			
			double prob = cdf(n, p, (int) k);
			int possibs = max - min;
			if(possibs <= 5)
				println("k=" + (int) k + ", cdf(n, p, k)=" + prob);
			if(prob <= P) {
                if(possibs <= 2)
					return (int) k;
				lowestK = (int) k;
				min = (int) Math.floor(k);
				k += (max - k) / 2;
			} else {
				max = (int) Math.ceil(k);
				k -= (k - min) / 2;
			}
		}
	}
	
	
	
	private static void println(String info) {
		if(quiet) return;
		System.out.println("[Binom] " + info);
	}
}
