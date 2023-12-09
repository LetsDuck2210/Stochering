package binom;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import util.Gamma;

public class Binom {
	public static boolean quiet = false;
	
	public static final BigDecimal DTWO = new BigDecimal("2"),
									DNEG_ONE = new BigDecimal("-1");
	public static final BigInteger ITWO = new BigInteger("2"),
									INEG_ONE = new BigInteger("-1");
	
	interface Event {
		public BigDecimal prob(BigInteger n, BigDecimal p, BigInteger k);
	}
	
	public static BigDecimal floor(BigDecimal x) {
        return new BigDecimal(x.setScale(0, RoundingMode.FLOOR).unscaledValue());
    }
	public static BigDecimal ceil(BigDecimal x) {
        return new BigDecimal(x.setScale(0, RoundingMode.CEILING).unscaledValue());
    }
	public static BigDecimal round(BigDecimal x) {
		return new BigDecimal(x.setScale(0, RoundingMode.HALF_UP).unscaledValue());
	}
	
	public static BigDecimal coeff(final BigInteger n, BigInteger k) {
        if (k.compareTo(n.subtract(k)) > 0)
            k=n.subtract(k);
        
        BigInteger b = BigInteger.ONE;
        for (BigInteger i=BigInteger.ONE, m=new BigInteger("" + n); i.compareTo(k) <= 0; i=i.add(BigInteger.ONE), m=m.subtract(BigInteger.ONE))
            b=b.multiply(m).divide(i);
        return new BigDecimal(b);
    }
	public static BigDecimal pdf(final BigInteger n, final BigDecimal p, final BigInteger k) {
		if(p.compareTo(ZERO) == 0) return (k.compareTo(BigInteger.ZERO) == 0 ? ONE : ZERO);
		final BigDecimal coeff = coeff(n, k);
		BigDecimal bres = coeff.multiply(p.pow(k.intValueExact())).multiply(ONE.subtract(p).pow(n.subtract(k).intValueExact()));
		return bres;
	}
	public static BigDecimal cdf(final BigInteger n, final BigDecimal p, final BigInteger k) {
		int ni = n.intValue();
		double pd = p.doubleValue();
		int ki = k.intValue();
		double da, db, dp;
        //        int ia, ib;

        if (ki < 0) {
            dp = 0.0;
        } else if (ki >= ni) {
            dp = 1.0;
        } else if (pd == 0.0) {
            dp = (ki < 0) ? 0.0 : 1.0;
        } else if (pd == 1.0) {
            dp = (ki < ni) ? 0.0 : 1.0;
        } else {
            da = (double) ki + 1.0;
            db = (double) (ni - ki);
            dp = 1.0 - Gamma.betaCdf(pd, da, db);
        }

        return new BigDecimal(dp);
	}
	public static BigInteger expect(final BigInteger n, final BigDecimal p, Event prob) {
		BigDecimal e = new BigDecimal(n).multiply(p);
		if(floor(e).compareTo(ceil(e)) != 0) {
			BigDecimal floor = prob.prob(n, p, floor(e).toBigInteger());
			BigDecimal ceil = prob.prob(n, p, ceil(e).toBigInteger());
			if(floor.compareTo(ceil) > 0) return e.toBigInteger();
			else return ceil(e).toBigInteger();
		}
		return e.toBigInteger();
	}
	
	static void println(String info) {
		if(quiet) return;
		System.out.println("[Binom] " + info);
	}
}
