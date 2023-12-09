package util;

public class Gamma {
	private static double vm_epsilon = 1.0;
    private static final double[] cof = { 76.18009172947146,
            -86.50532032941677, 24.01409824083091, -1.231739572450155,
            0.1208650973866179e-2, -0.5395239384953e-5 };


    /**
     * Incomplete Beta function. </p> Translated from FORTRAN july 1977 edition.
     * w. fullerton, c3, los alamos scientific lab. based on bosten and
     * battiste, remark on algorithm 179, comm. acm, v 17, p 153, (1974).
     *
     * @param x   upper limit of integration.  x must be in (0,1) inclusive.
     * @param pin first beta distribution parameter.  p must be gt 0.0.
     * @param qin second beta distribution parameter.  q must be gt 0.0.
     * @return result.
     */
    public static double betaCdf(double x, double pin, double qin) {
        // betai  the incomplete beta function ratio is the probability that a
        // random variable from a beta distribution having parameters
        // p and q will be less than or equal to x.
    	
        double c, finsum, p, ps, q, term, xb, xi, y, dbetai, p1;
        int i, n, ib;
        double eps, alneps, sml, alnsml;

        if (x <= 0.0) {
            return 0.0;

            // I'm not sure these tolerances are optimal.
        }

        eps = macheps();
        alneps = Math.log(eps);
        sml = eps;
        alnsml = alneps;
        y = x;
        p = pin;
        q = qin;

        if ((q > p) || (x >= 0.8)) {
            if (x >= 0.2) {
                y = 1.0 - y;
                p = qin;
                q = pin;
            }
        }

        if ((p + q) * y / (p + 1.0) < eps) {
            dbetai = 0.0;
            xb = p * Math.log(Math.max(y, sml)) - Math.log(p)
                    - logbeta(p, q);

            if ((xb > alnsml) && (y != 0.0)) {
                dbetai = Math.exp(xb);
            }

            if ((y != x) || (p != pin)) {
                dbetai = 1.0 - dbetai;
            }
        } else {

            // evaluate the infinite sum first.  term will equal
            // y**/pbeta(ps,p) * (1.-ps)-sub-i * y**i / fac(i) .
            ps = q - Math.floor(q);

            if (ps == 0.0) {
                ps = 1.0;
            }

            xb = p * Math.log(y) - logbeta(ps, p) - Math.log(p);
            dbetai = 0.0;

            if (xb >= alnsml) {
                dbetai = Math.exp(xb);
                term = dbetai * p;

                if (ps != 1.0) {
                    n = (int) Math.max(alneps / Math.log(y), 4.0);

                    for (i = 1; i <= n; i++) {
                        xi = i;
                        term = term * (xi - ps) * y / xi;
                        dbetai = dbetai + term / (p + xi);
                    }
                }
            }

            // now evaluate the finite sum, maybe
            if (q > 1.0) {
                xb = p * Math.log(y) + q * Math.log(1.0 - y)
                        - logbeta(p, q) - Math.log(q);
                ib = (int) Math.max(xb / alnsml, 0.0);
                term = Math.exp(xb - ((double) ib) * alnsml);
                c = 1.0 / (1.0 - y);
                p1 = q * c / (p + q - 1.0);
                finsum = 0.0;
                n = (int) q;

                if (q == (double) n) {
                    n--;
                }

                for (i = 1; i <= n; i++) {
                    if ((p1 <= 1.0) && (term / eps <= finsum)) {
                        break;
                    }

                    xi = i;
                    term = (q - xi + 1.0) * c * term / (p + q - xi);

                    if (term > 1.0) {
                        ib = ib - 1;
                    }

                    if (term > 1.0) {
                        term = term * sml;
                    }

                    if (ib == 0) {
                        finsum += term;
                    }
                }

                dbetai += finsum;
            }

            if ((y != x) || (p != pin)) {
                dbetai = 1.0 - dbetai;
            }

            dbetai = Math.max(Math.min(dbetai, 1.0), 0.0);
        }

        return dbetai;
    }

    /**
     * Method declaration
     *
     * @return result.
     */
    private static double macheps() {

        if (vm_epsilon >= 1.0) {
            while (1.0 + vm_epsilon / 2.0 != 1.0) {
                vm_epsilon /= 2.0;
            }
        }

        return vm_epsilon;
    }

    /**
     * Calculates the log beta function of p and q.
     */
    public static double logbeta(double p, double q) {
        return (lngamma(p) + lngamma(q) - lngamma(p + q));
    }

    /**
     * This is a more literal (that is, exact) copy of the log gamma method from
     * Numerical Recipes than the following one.  It was created by cutting and
     * pasting from the PDF version of the book and then converting C syntax to
     * Java. </p> The static double array above goes with this. </p> Converted
     * to Java by Frank Wimberly
     *
     * @return the value ln[?(xx)] for xx > 0
     */
    public static double lngamma(double xx) {
        //Returns the value ln[?(xx)] for xx > 0.

        if (xx <= 0)
            return Double.NaN;

        //Internal arithmetic will be done in double precision, a nicety that you can omit if ?ve-?gure
        //accuracy is good enough.
        double x, y, tmp, ser;

        int j;
        y = x = xx;
        tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        ser = 1.000000000190015;
        for (j = 0; j <= 5; j++) {
            ser += cof[j] / ++y;
        }
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }
}
