package binom;

import main.Equation;
import static binom.Binom.*;

public class PDF {

	
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
		
		println("started reversePDF_n with p=" + p.getTerm() + ", k=" + k.getTerm() + ", P=" + P.getTerm());
		
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
		
		println("started reversePDF_k with n=" + n.getTerm() + ", p=" + p.getTerm() + ", P=" + P.getTerm());
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
	
}
