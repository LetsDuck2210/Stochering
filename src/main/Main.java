package main;

import java.util.Random;

import binom.Binom;

public class Main {

	public static long measure(Runnable r) {
		long start = System.nanoTime();
		r.run();
		return System.nanoTime() - start;
	}
	
	public static void main(String[] args) {
		System.out.println("PDF_n took " + (measure(Main::randPDF_n) / 1_000_000.0) + "ms");
		System.out.println("PDF_k took " + (measure(Main::randPDF_k) / 1_000_000.0) + "ms");
		System.out.println("CDF_n took " + (measure(Main::randCDF_n) / 1_000_000.0) + "ms");
		System.out.println("CDF_k took " + (measure(Main::randCDF_k) / 1_000_000.0) + "ms");
	}
	
	public static void randPDF_n() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int k = new Random().nextInt(10);
		int result = Binom.reversePDF_n(p, k, P);
		System.out.println((Binom.pdf(result, p, k) > P ? "\033[0;31m" : "") + "n = " + result + "\033[0m");
	}
	public static void randPDF_k() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int n = new Random().nextInt(20);
		int[] result = Binom.reversePDF_k(n, p, P);
		System.out.println("k = {" + result[0] + (result.length == 1 ? "" : ", " + result[1]) + "}");
	}
	public static void randCDF_n() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int k = new Random().nextInt(10);
		int result = Binom.reverseCDF_n(p, k, P);
		System.out.println((Binom.cdf(result, p, k) > P ? "\033[0;31m" : "") + "n = " + result + "\033[0m");
	}
	public static void randCDF_k() {
		final double p = (int)(Math.random() * 100) / 100.0, P = (int)(Math.random() * 100) / 100.0;
		final int n = new Random().nextInt(20);
		int result = Binom.reverseCDF_k(n, p, P);
		System.out.println((Binom.cdf(n, p, result) > P ? "\033[0;31m" : "") + "k = " + result + "\033[0m");
	}
}
