package binom;

import static binom.Binom.expect;
import static binom.Binom.pdf;
import static binom.Binom.println;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;

import main.Equation;

public class PDF {

	
	/** attempts to find n for P(X = k) <= P <br/>
	 *  takes equations as strings
	 * @param eq_p probability of a single success
	 * @param eq_k amount of successes expected
	 * @param eq_P expected probability of event
	 * @return n: amount of turns required
	 */
	public static BigInteger reversePDF_n(final String eq_p, final String eq_k, final String eq_P) {
		BigInteger k0 = new Equation(eq_k, "n").evaluateInt(ZERO);
		final BigInteger range = new BigInteger("10000");
		BigInteger min = k0, max = k0.add(range);
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
	public static BigInteger reversePDF_n(final String eq_p, final String eq_k, final String eq_P, BigInteger min, BigInteger max) {
		final String paramName = "n";
		final Equation 	p = new Equation(eq_p, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		println("started reversePDF_n with p=" + p.getTerm() + ", k=" + k.getTerm() + ", P=" + P.getTerm());
		
		for(BigDecimal n = new BigDecimal((min.add(max)).divide(new BigInteger("2")));;) {
			BigDecimal pEval = p.evaluateDouble(new BigDecimal(n.intValueExact()));
			BigInteger kEval = k.evaluateInt(new BigDecimal(n.intValueExact()));
			BigDecimal PEval = P.evaluateDouble(new BigDecimal(n.intValueExact()));
			
			int possibs = Math.abs(min.subtract(max).intValueExact());
			BigDecimal prob = pdf(n.toBigInteger(), pEval, kEval);
			if(possibs <= 5)
				println("n=" + n.toBigInteger() + ", pdf(n, p, k)=" + prob);
			
			boolean inc; // increase or decrease n
			if(prob.compareTo(PEval) <= 0) {
				if(possibs <= 2)
					return n.toBigInteger();
				inc = n.multiply(pEval).compareTo(new BigDecimal(kEval)) < 0;
			} else
				inc = n.multiply(pEval).compareTo(new BigDecimal(kEval)) > 0;
			
			if(inc) {
				min = n.toBigInteger();
				n = n.add(new BigDecimal(max.subtract(n.toBigInteger()).divide(new BigInteger("2"))));
			} else {
				max = n.round(MathContext.UNLIMITED).toBigInteger();
				n = n.subtract((n.subtract(new BigDecimal(min))).divide(new BigDecimal(2)));
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
	public static BigInteger[] reversePDF_k(final String eq_n, final String eq_p, final String eq_P) {
		final Equation 	n = new Equation(eq_n, "k");
		
		return reversePDF_k(eq_n, eq_p, eq_P, BigInteger.ZERO, n.evaluateInt(ZERO));
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
	public static BigInteger[] reversePDF_k(final String eq_n, final String eq_p, final String eq_P, final BigInteger min, final BigInteger max) {
		final String paramName = "k";
		final Equation 	n = new Equation(eq_n, paramName),
						p = new Equation(eq_p, paramName),
						P = new Equation(eq_P, paramName);
		
		println("started reversePDF_k with n=" + n.getTerm() + ", p=" + p.getTerm() + ", P=" + P.getTerm());
		if(n.evaluateInt(ZERO).intValueExact() == 0) return new BigInteger[] {BigInteger.ZERO};
		BigInteger left = reversePDF_k_oneside(n, p, P, true, min, max);
		BigInteger right = reversePDF_k_oneside(n, p, P, false, min, max);
		final BigInteger 	nLeft = n.evaluateInt(new BigDecimal(left)),
							nRight = n.evaluateInt(new BigDecimal(right));
		final BigDecimal 	pLeft = p.evaluateDouble(new BigDecimal(left)),
							pRight = p.evaluateDouble(new BigDecimal(right));
		if(pdf(nLeft, pLeft, left).compareTo(pdf(nRight, pRight, right)) > 0) return new BigInteger[] {left};
		if(pdf(nRight, pRight, right).compareTo(pdf(nLeft, pLeft, left)) > 0) return new BigInteger[] {right};
		return new BigInteger[] {left, right};
	}
	private static BigInteger reversePDF_k_oneside(final Equation n, final Equation p, final Equation P, boolean leftside, final BigInteger min0, final BigInteger max0) {
		BigInteger expect = expect(n.evaluateInt(ZERO), p.evaluateDouble(ZERO), Binom::pdf);
		BigInteger 	min = leftside ? min0 : expect,
					max = leftside ? expect : max0;
		BigInteger lastK = new BigInteger("-1"), nearestK = new BigInteger("-1");
		for(BigDecimal k = new BigDecimal(min.add(max.subtract(min)).divide(new BigInteger("2")));;) {
			BigInteger nEval = n.evaluateInt(k);
			BigDecimal pEval = p.evaluateDouble(k);
			BigDecimal PEval = P.evaluateDouble(k);
			
			if(Binom.floor(k).toBigInteger().compareTo(lastK) == 0) return nearestK;
			lastK = Binom.floor(k).toBigInteger();
			BigDecimal prob = pdf(nEval, pEval, Binom.floor(k).toBigInteger());
			BigInteger possibs = max.subtract(min);
			if(possibs.compareTo(new BigInteger("5")) <= 0)
				println("(" + (leftside ? "left" : "right") + ") k=" + Binom.floor(k).toBigInteger() + ", pdf(n, p, k)=" + prob);
			
			// increase on left side but decrease on right side if prob <= PEval
			boolean inc = leftside;
			if(prob.compareTo(PEval) <= 0) {
				if(possibs.compareTo(new BigInteger("2")) <= 0)
					return Binom.floor(k).toBigInteger();
			} else
				inc = !inc;
			if(inc) {
				nearestK = Binom.floor(k).toBigInteger();
				min = Binom.floor(k).toBigInteger();
				k = k.add((new BigDecimal(max).subtract(k)).divide(new BigDecimal(2)));
			} else {
				max = Binom.ceil(k).toBigInteger();
				k = k.subtract((k.subtract(new BigDecimal(min))).divide(new BigDecimal(2)));
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
	public static BigDecimal reversePDF_p(final String eq_n, final String eq_k, final String eq_P, final BigInteger accuracy) {
		final String paramName = "p";
		final Equation 	n = new Equation(eq_n, paramName),
						k = new Equation(eq_k, paramName),
						P = new Equation(eq_P, paramName);
		
		BigDecimal 	min = ZERO, max = ONE,
					lastP = new BigDecimal(-1), nearestP = new BigDecimal(-1);
		
		for(BigDecimal p = new BigDecimal("0.5");;) {
			BigDecimal 	rounded = Binom.round(p.multiply(new BigDecimal(BigInteger.TEN.pow(accuracy.intValueExact()).divide(BigInteger.TEN.pow(accuracy.intValueExact()))))),
						floored = Binom.floor(p.multiply(new BigDecimal(BigInteger.TEN.pow(accuracy.intValueExact()).divide(BigInteger.TEN.pow(accuracy.intValueExact()))))),
						ceiled = Binom.ceil(p.multiply(new BigDecimal(BigInteger.TEN.pow(accuracy.intValueExact()).divide(BigInteger.TEN.pow(accuracy.intValueExact())))));
			
			BigInteger nEval = n.evaluateInt(p);
			BigInteger kEval = k.evaluateInt(p);
			BigDecimal PEval = P.evaluateDouble(p);
			
			if(p.compareTo(lastP) == 0) return nearestP;
			lastP = rounded;
			BigDecimal prob = pdf(nEval, p, kEval);
			if(prob.compareTo(PEval) >= 0) {
				if(rounded == nearestP) return rounded;
				nearestP = rounded;
				min = floored;
				p = p.add((max.subtract(p)).divide(new BigDecimal(2)));
			} else {
				max = ceiled;
				p = p.subtract((p.subtract(min)).divide(new BigDecimal(2)));
			}
		}
	}
	
}
