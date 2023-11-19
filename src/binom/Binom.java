package binom;

import main.Equation;

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
	
	
	/** attempts to find n for P(X = k) <= P <br/>
	 *  takes equations as strings
	 * @param eq_p probability of a single success
	 * @param eq_k amount of successes expected
	 * @param eq_P expected probability of event
	 * @return n: amount of turns required
	 */
	public static int reversePDF_n(final String eq_p, final String eq_k, final String eq_P) {
		int k0 = (int) new Equation(eq_k, "n").evaluate(0);
		final int range = 10000;
		int min = (int) k0, max = (int) k0 + range;
		return reversePDF_n(eq_p, eq_k, eq_P, min, max);
	}
	/** attempts to find n for P(X = k) <= P <br/>
	 *  takes equations as strings
	 * @param eq_p probability of a single success
	 * @param eq_k amount of successes expected
	 * @param eq_P expected probability of event
	 * @param min smallest possible n
	 * @param max biggest possible n
	 * @return n: amount of turns required
	 */
	public static int reversePDF_n(final String eq_p, final String eq_k, final String eq_P, int min, int max) {
		final String paramName = "n";
		final Equation 	p = new Equation(eq_p, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		println("started reversePDF_n with p=" + p.evaluate(0) + ", k=" + (int) k.evaluate(0) + ", P=" + P);
		
		for(double n = (min + max) / 2;;) {
			double pEval = p.evaluate((int) n);
			int kEval = (int) k.evaluate((int) n);
			double PEval = P.evaluate((int) n);
			
			int possibs = Math.abs(min - max);
			double prob = pdf((int) n, pEval, kEval);
			if(possibs <= 5)
				println("n=" + (int) n + ", pdf(n, p, k)=" + prob);
			
			boolean inc; // increase or decrease n
			if(prob <= PEval) {
				if(possibs <= 2)
					return (int) n;
				inc = n * pEval < kEval;
			} else
				inc = n * pEval > kEval;
			
			if(inc) {
				min = (int) Math.floor(n);
				n += (max - n) / 2;
			} else {
				max = (int) Math.ceil(n);
				n -= (n - min) / 2;
			}
		}
	}
	
	/** attempts to find k for P(X = k) <= P<br/>
	 *  takes equations as strings
	 * @param eq_n number of trials
	 * @param eq_P max probability of event
	 * @param eq_P max probability of event
	 * @return k: list of minimum required successes 
	 */
	public static int[] reversePDF_k(final String eq_n, final String eq_p, final String eq_P) {
		final Equation 	n = new Equation(eq_n, "k");
		
		return reversePDF_k(eq_n, eq_p, eq_P, 0, (int) n.evaluate(0));
	}
	/** attempts to find k for P(X = k) <= P<br/>
	 *  takes equations as strings
	 * @param eq_n number of trials
	 * @param eq_P max probability of event
	 * @param eq_P max probability of event
	 * @param min smallest possible k
	 * @param max biggest possible k
	 * @return k: list of minimum required successes 
	 */
	public static int[] reversePDF_k(final String eq_n, final String eq_p, final String eq_P, final int min, final int max) {
		final String paramName = "k";
		final Equation 	n = new Equation(eq_n, paramName),
						p = new Equation(eq_p, paramName),
						P = new Equation(eq_P, paramName);
		
		println("started reversePDF_k with n=" + (int) n.evaluate(0) + ", p=" + p.evaluate(0) + ", P=" + P.evaluate(0));
		if(n.evaluate(0) == 0) return new int[] {0};
		int left = reversePDF_k_oneside(n, p, P, true, min, max);
		int right = reversePDF_k_oneside(n, p, P, false, min, max);
		final int 	nLeft = (int) n.evaluate(left),
					nRight = (int) n.evaluate(right);
		final double 	pLeft = p.evaluate(left),
						pRight = p.evaluate(right);
		if(pdf(nLeft, pLeft, left) > pdf(nRight, pRight, right)) return new int[] {left};
		if(pdf(nRight, pRight, right) > pdf(nLeft, pLeft, left)) return new int[] {right};
		return new int[] {left, right};
	}
	private static int reversePDF_k_oneside(final Equation n, final Equation p, final Equation P, boolean leftside, final int min0, final int max0) {
		int expect = expect((int) n.evaluate(0), p.evaluate(0), Binom::pdf);
		int min = leftside ? min0 : expect,
			max = leftside ? expect : max0;
		int lastK = -1, nearestK = -1;
		for(double k = min + (max - min)/2;;) {
			int nEval = (int) n.evaluate((int) k);
			double pEval = p.evaluate((int) k);
			double PEval = P.evaluate((int) k);
			
			if((int) k == lastK) return nearestK;
			lastK = (int) k;
			double prob = pdf(nEval, pEval, (int) k);
			int possibs = max - min;
			if(possibs <= 5)
				println("(" + (leftside ? "left" : "right") + ") k=" + (int) k + ", pdf(n, p, k)=" + prob);
			
			// increase on left side but decrease on right side if prob <= PEval
			boolean inc = leftside;
			if(prob <= PEval) {
				if(possibs <= 2)
					return (int) k;
			} else
				inc = !inc;
			if(inc) {
				nearestK = (int) k;
				min = (int) Math.floor(k);
				k += (max - k) / 2;
			} else {
				max = (int) Math.ceil(k);
				k -= (k - min) / 2;
			}
		}
	}
	
	/** attempts to find p for P(X = k) <= P<br/>
	 *  takes equations as strings
	 * 
	 * @param eq_n number of trials
	 * @param eq_k amount of successes expected
	 * @param eq_P max probability of event
	 * @param accuracy number of digits 
	 * @return p: approximate probability required per event
	 */
	public static double reversePDF_p(final String eq_n, final String eq_k, final String eq_P, final int accuracy) {
		final String paramName = "p";
		final Equation 	n = new Equation(eq_n, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		double 	min = 0, max = 1,
				lastP = -1, nearestP = -1;
		
		for(double p = 0.5;;) {
			double 	rounded = Math.round(p * Math.pow(10, accuracy)) / Math.pow(10, accuracy),
					floored = (int)(p * Math.pow(10, accuracy)) / Math.pow(10, accuracy),
					ceiled = Math.ceil(p * Math.pow(10, accuracy)) / Math.pow(10, accuracy);
			
			int nEval = (int) n.evaluate(p);
			int kEval = (int) k.evaluate(p);
			double PEval = P.evaluate(p);
			
			if(p == lastP) return nearestP;
			lastP = rounded;
			double prob = pdf(nEval, p, kEval);
			if(prob >= PEval) {
				if(rounded == nearestP) return rounded;
				nearestP = rounded;
				min = floored;
				p += (max - p) / 2;
			} else {
				max = ceiled;
				p -= (p - min) / 2;
			}
		}
	}
	
	
	/** attempts to find n for P(X <= k) <= P<br/>
	 *  takes equations as strings
	 * @param eq_P max probability of event
	 * @param eq_k amount of successes expected
	 * @param eq_P max probability of event
	 * @param min smallest possible n
	 * @param max biggest possible n
	 * @return n: number of trials required
	 */
	public static int reverseCDF_n(final String eq_p, final String eq_k, final String eq_P) {
		final int range = 10000;
		int k0 = Math.max((int) new Equation(eq_k, "n").evaluate(0), 0);
		int min = k0, max = k0 + range;
		return reverseCDF_n(eq_p, eq_k, eq_P, min, max);
	}
	/** attempts to find n for P(X <= k) <= P<br/>
	 *  takes equations as strings
	 * @param eq_P max probability of event
	 * @param eq_k amount of successes expected
	 * @param eq_P max probability of event
	 * @param min smallest possible n
	 * @param max biggest possible n
	 * @return n: number of trials required
	 */
	public static int reverseCDF_n(final String eq_p, final String eq_k, final String eq_P, int min, int max) {
		final String paramName = "n";
		final Equation 	p = new Equation(eq_p, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		println("started reverseCDF_n with p=" + p.getTerm() + ", k=" + k.getTerm() + ", P=" + P.getTerm());
		int lastN = -1, nearestN = -1;
		for(double n = (min + max) / 2;;) {
			double pEval = p.evaluate((int) n);
			int kEval = Math.max((int) k.evaluate((int) n), 0);
			double PEval = P.evaluate((int) n);
			
			if((int) n == lastN) return nearestN;
			int possibs = Math.abs(min - max);
			double prob = cdf((int) n, pEval, kEval);
			if(possibs <= 5)
				println("n=" + (int) n + ", k=" + kEval + ", cdf(n, p, k)=" + prob);
			lastN = (int) n;
			if(prob <= PEval) {
				if(possibs <= 2)
					return (int) n;
				nearestN = (int) n;
				max = (int) Math.ceil(n); 
				n -= (n - min) / 2;
			} else {
				min = (int) Math.floor(n);
				n += (max - n) / 2;
			}
		}
	}
	
	/** attempts to find k for P(X <= k) <= P<br/>
	 *  takes equations as strings
	 * @param eq_n number of trials
	 * @param eq_P max probability of event
	 * @param eq_P max probability of event
	 * @return k: minimum amount of successes
	 */
	public static int reverseCDF_k(final String eq_n, final String eq_p, final String eq_P) {
		int min = 0, max = (int) new Equation(eq_n, "k").evaluate(0);
		return reverseCDF_k(eq_n, eq_p, eq_P, min, max);
	}
	/** attempts to find k for P(X <= k) <= P<br/>
	 *  takes equations as strings
	 * @param eq_n number of trials
	 * @param eq_P max probability of event
	 * @param eq_P max probability of event
	 * @param min smallest possible k
	 * @param max biggest possible k
	 * @return k: minimum amount of successes
	 */
	public static int reverseCDF_k(final String eq_n, final String eq_p, final String eq_P, int min, int max) {
		final String paramName = "k";
		final Equation 	n = new Equation(eq_n, paramName),
						p = new Equation(eq_p, paramName),
						P = new Equation(eq_P, paramName);
		
		int n0 = (int) n.evaluate(0);
		double p0 = p.evaluate(0), P0 = P.evaluate(0);
		println("started reverseCDF_k with n=" + n0 + ", p=" + p0 + ", P=" + P0);
		if(n0 == 0) return 0;
		
		int lastK = -1, nearestK = -1;
		for(double k = min + (max - min)/2;;) {
			int nEval = (int) n.evaluate((int) k);
			double pEval = p.evaluate((int) k);
			double PEval = P.evaluate((int) k);
			
			if((int) k == lastK) return nearestK;
			lastK = (int) k;
			
			double prob = cdf(nEval, pEval, (int) k);
			int possibs = max - min;
			if(possibs <= 5)
				println("k=" + (int) k + ", cdf(n, p, k)=" + prob);
			if(prob <= PEval) {
                if(possibs <= 2)
					return (int) k;
				nearestK = (int) k;
				min = (int) Math.floor(k);
				k += (max - k) / 2;
			} else {
				max = (int) Math.ceil(k);
				k -= (k - min) / 2;
			}
		}
	}
	
	/** attempts to find p for P(X <= k) <= P<br/>
	 *  takes equations as strings
	 * 
	 * @param eq_n number of trials
	 * @param eq_k amount of successes expected
	 * @param eq_P max probability of event
	 * @param accuracy number of digits 
	 * @return p: approximate probability required per event
	 */
	public static double reverseCDF_p(final String eq_n, final String eq_k, final String eq_P, final int accuracy) {
		double 	min = 0, max = 1,
				lastP = -1, nearestP = -1;
		
		final String paramName = "p";
		final Equation 	n = new Equation(eq_n, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		for(double p = 0.5;;) {
			int nEval = (int) n.evaluate(p);
			int kEval = (int) k.evaluate(p);
			double PEval = P.evaluate(p);
			
			double 	rounded = Math.round(p * Math.pow(10, accuracy)) / Math.pow(10, accuracy),
					floored = (int)(p * Math.pow(10, accuracy)) / Math.pow(10, accuracy),
					ceiled = Math.ceil(p * Math.pow(10, accuracy)) / Math.pow(10, accuracy);
			if(p == lastP) return nearestP;
			lastP = rounded;
			double prob = cdf(nEval, p, kEval);
			if(prob >= PEval) {
				if(rounded == nearestP) return rounded;
				nearestP = rounded;
				min = floored;
				p += (max - p) / 2;
			} else {
				max = ceiled;
				p -= (p - min) / 2;
			}
		}
	}
	
	
	private static void println(String info) {
		if(quiet) return;
		System.out.println("[Binom] " + info);
	}
}
