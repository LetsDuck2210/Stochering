package binom;

import main.Equation;
import static binom.Binom.*;

public class CDF {
	
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
		println("started reverseCDF_k with n=" + n.getTerm() + ", p=" + p.getTerm() + ", P=" + P.getTerm());
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
	
}
