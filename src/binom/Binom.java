package binom;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Binom {
	public static boolean quiet = false;
	
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
        
        BigDecimal bk = new BigDecimal(k);
        BigDecimal b = ONE;
        for (BigDecimal i=ONE, m=new BigDecimal(n); i.compareTo(bk) <= 0; i=i.add(ONE), m=m.subtract(ONE))
            b=b.multiply(m).divide(i);
        return b;
    }
	public static BigDecimal pdf(final BigInteger n, final BigDecimal p, final BigInteger k) {
		if(p.compareTo(ZERO) == 0) return (k.compareTo(BigInteger.ZERO) == 0 ? ONE : ZERO);
		final BigDecimal coeff = coeff(n, k);
		BigDecimal bres = coeff.multiply(p.pow(k.intValueExact())).multiply(ONE.subtract(p).pow(n.subtract(k).intValueExact()));
		return bres;
	}
	public static BigDecimal cdf(final BigInteger n, final BigDecimal p, final BigInteger k) {
		if(p.doubleValue() == 0) return (Binom.floor(new BigDecimal(k)).intValueExact() == 0 ? ONE : ZERO);
		if(k.intValueExact() < 0) return ZERO;
		if(n.intValueExact() <= 0) return ZERO;
		
		BigDecimal total = ZERO;
		for(BigInteger i = BigInteger.ZERO; i.compareTo(k) <= 0; i=i.add(BigInteger.ONE)) {
			total = total.add(pdf(n, p, i));
		}
		return total;
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
