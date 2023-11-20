package binom;

import static binom.Binom.INEG_ONE;
import static binom.Binom.ITWO;
import static binom.Binom.cdf;
import static binom.Binom.println;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.BigInteger;

import main.Equation;

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
	public static BigInteger reverseCDF_n(final String eq_p, final String eq_k, final String eq_P) {
		final BigInteger range = new BigInteger("10000");
		BigInteger k0 = new Equation(eq_k, "n").evaluateInt(ZERO).max(BigInteger.ZERO);
		BigInteger min = k0, max = k0.add(range);
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
	public static BigInteger reverseCDF_n(final String eq_p, final String eq_k, final String eq_P, BigInteger min, BigInteger max) {
		final String paramName = "n";
		final Equation 	p = new Equation(eq_p, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		println("started reverseCDF_n with p=" + p.getTerm() + ", k=" + k.getTerm() + ", P=" + P.getTerm());
		BigInteger lastN = INEG_ONE, nearestN = INEG_ONE;
		for(BigDecimal n = new BigDecimal(min.add(max).divide(ITWO));;) {
			BigDecimal pEval = p.evaluateDouble(n);
			BigInteger kEval = k.evaluateInt(n).max(BigInteger.ZERO);
			BigDecimal PEval = P.evaluateDouble(n);
			
			if(n.compareTo(new BigDecimal(lastN)) == 0) return nearestN;
			BigInteger possibs = min.subtract(max).abs();
			BigDecimal prob = cdf(Binom.floor(n).toBigIntegerExact(), pEval, kEval);
			if(possibs.compareTo(new BigInteger("5")) <= 0)
				println("cdf(n=" + Binom.floor(n).toBigIntegerExact() + ", p=" + pEval + ", k=" + kEval + ")=" + prob);
			lastN = Binom.floor(n).toBigIntegerExact();
			if(prob.compareTo(PEval) <= 0) {
				if(possibs.compareTo(ITWO) <= 0)
					return Binom.floor(n).toBigIntegerExact();
				nearestN = Binom.floor(n).toBigIntegerExact();
				max = Binom.ceil(n).toBigIntegerExact(); 
				n = n.subtract((n.subtract(new BigDecimal(min))).divide(new BigDecimal(2)));
			} else {
				min = Binom.floor(n).toBigIntegerExact();
				n = n.add((new BigDecimal(max).subtract(n)).divide(new BigDecimal(2)));
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
	public static BigInteger reverseCDF_k(final String eq_n, final String eq_p, final String eq_P) {
		BigInteger min = BigInteger.ZERO, max = new Equation(eq_n, "k").evaluateInt(ZERO);
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
	public static BigInteger reverseCDF_k(final String eq_n, final String eq_p, final String eq_P, BigInteger min, BigInteger max) {
		final String paramName = "k";
		final Equation 	n = new Equation(eq_n, paramName),
						p = new Equation(eq_p, paramName),
						P = new Equation(eq_P, paramName);
		
		BigInteger n0 = n.evaluateInt(ZERO);
		println("started reverseCDF_k with n=" + n.getTerm() + ", p=" + p.getTerm() + ", P=" + P.getTerm());
		if(n0.intValueExact() == 0) return BigInteger.ZERO;
		
		BigInteger lastK = INEG_ONE, nearestK = INEG_ONE;
		for(BigDecimal k = new BigDecimal(min.add((max.subtract(min)).divide(ITWO)));;) {
			BigInteger nEval = n.evaluateInt(k);
			BigDecimal pEval = p.evaluateDouble(k);
			BigDecimal PEval = P.evaluateDouble(k);
			
			if(Binom.floor(k).toBigIntegerExact().compareTo(lastK) == 0) return nearestK;
			lastK = Binom.floor(k).toBigIntegerExact();
			
			BigDecimal prob = cdf(nEval, pEval, Binom.floor(k).toBigIntegerExact());
			BigInteger possibs = max.subtract(min);
			if(possibs.compareTo(new BigInteger("5")) <= 0)
				println("cdf(n=" + nEval + ", p=" + pEval + ", k=" + Binom.floor(k).toBigIntegerExact() + ")=" + prob);
			if(prob.compareTo(PEval) <= 0) {
                if(possibs.compareTo(ITWO) <= 0)
					return Binom.floor(k).toBigIntegerExact();
				nearestK = Binom.floor(k).toBigIntegerExact();
				min = Binom.floor(k).toBigIntegerExact();
				k = k.add((new BigDecimal(max).subtract(k)).divide(new BigDecimal(2)));
			} else {
				max = Binom.ceil(k).toBigIntegerExact();
				k = k.subtract(k.subtract(new BigDecimal(min)).divide(new BigDecimal(2)));
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
	public static BigDecimal reverseCDF_p(final String eq_n, final String eq_k, final String eq_P, final BigInteger accuracy) {
		BigDecimal 	min = ZERO, max = ONE,
					lastP = new BigDecimal(-1), nearestP = new BigDecimal(-1);
		
		final String paramName = "p";
		final Equation 	n = new Equation(eq_n, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		for(BigDecimal p = new BigDecimal("0.5");;) {
			BigInteger nEval = n.evaluateInt(p);
			BigInteger kEval = k.evaluateInt(p);
			BigDecimal PEval = P.evaluateDouble(p);
			
			BigDecimal 	rounded = Binom.round(p.multiply(new BigDecimal(BigInteger.TEN.pow(accuracy.intValueExact()))).divide(BigDecimal.TEN.pow(accuracy.intValueExact()))),
						floored = Binom.floor(p.multiply(new BigDecimal(BigInteger.TEN.pow(accuracy.intValueExact()))).divide(BigDecimal.TEN.pow(accuracy.intValueExact()))),
						ceiled = Binom.ceil(p.multiply(new BigDecimal(BigInteger.TEN.pow(accuracy.intValueExact()))).divide(BigDecimal.TEN.pow(accuracy.intValueExact())));
			if(p.compareTo(lastP) == 0) return nearestP;
			lastP = rounded;
			BigDecimal prob = cdf(nEval, p, kEval);
			if(prob.compareTo(PEval) >= 0) {
				if(rounded == nearestP) return rounded;
				nearestP = rounded;
				min = floored;
				p = p.add(max.subtract(p).divide(new BigDecimal(2)));
			} else {
				max = ceiled;
				p = p.subtract(p.subtract(min).divide(new BigDecimal(2)));
			}
		}
	}
	
}
