package receiver;

import org.jtransforms.fft.DoubleFFT_1D;

public class DeMapper extends edu.mit.streamjit.api.Pipeline<Float, Float> {
	public DeMapper() {
		super(new DFT(12), new IFFTPrepare(), new ReOrder());
	}

	private static class DFT extends edu.mit.streamjit.api.Filter<Float, Float> {

		DoubleFFT_1D f;

		public DFT(int length) {
			super(24, 24);
			f = new DoubleFFT_1D(12);
		}

		@Override
		public void work() {
			double[] a = new double[24];
			for (int i = 0; i < 24; i++) {
				a[i] = pop();
				// System.out.println(a[i]);
			}
			f.complexForward(a);
			for (int i = 0; i < 24; i++) {
				push((float) a[i]);
			}

		}

	}

	private static class FFT extends edu.mit.streamjit.api.Filter<Float, Float> {

		DoubleFFT_1D f;

		public FFT(int length) {
			super(16, 16);
			f = new DoubleFFT_1D(8);
		}

		@Override
		public void work() {
			double[] a = new double[16];
			for (int i = 0; i < 16; i++) {
				a[i] = pop();
				// System.out.println(a[i]);
			}
			f.complexForward(a);
			for (int i = 0; i < 16; i++) {
				push((float) a[i]);
			}

		}

	}

	private static class IFFTPrepare extends edu.mit.streamjit.api.Filter<Float, Float> {

		public IFFTPrepare() {
			super(24, 16);

		}

		@Override
		public void work() {
			for (int i = 0; i < 2; i++) {
				pop();
				pop();

			}

			for (int i = 0; i < 8; i++) {
				push(pop());
				push(pop());

			}

			for (int i = 0; i < 2; i++) {
				pop();
				pop();

			}

		}
	}

	private static class ScaleConjugate extends edu.mit.streamjit.api.Filter<Float, Float> {

		public ScaleConjugate() {
			super(2, 2);
		}

		@Override
		public void work() {
			push(pop() / (8));
			push(-1 * pop() / (8));

		}
	}

	private static class ReOrder extends edu.mit.streamjit.api.Filter<Float, Float> {

		public ReOrder() {
			super(32, 32);
		}

		@Override
		public void work() {
			float[] a = new float[16];
			for (int i = 0; i < 16; i++) {
				push(pop());
				a[i] = pop();
			}

			for (int i = 0; i < 16; i++) {
				push(a[i]);
			}

		}
	}

	private static class Printer extends edu.mit.streamjit.api.Filter<Float, Float> {

		public Printer() {
			super(1, 1);
		}

		@Override
		public void work() {
			float a = pop();
			System.out.println(a);
			push(a);

		}
	}
}
