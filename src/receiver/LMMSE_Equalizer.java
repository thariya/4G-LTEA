package receiver;

import org.jtransforms.fft.DoubleFFT_1D;

public class LMMSE_Equalizer extends edu.mit.streamjit.api.Pipeline<Float, Float> {
	public LMMSE_Equalizer() {
		this.add(new LMMSE_Init(), new LMMSE_Invert(), new LMMSE_Revert(), new LMMSE_Estimate());
	}

	private static class LMMSE_Init extends edu.mit.streamjit.api.Filter<Float, Float> {

		public LMMSE_Init() {
			super(48, 48);
		}

		@Override
		public void work() {
			float[][] tx1 = new float[2][12];
			float[][] tx2 = new float[2][12];

			Float[][] Tx_array = new Float[24][2];
			for (int j = 0; j < 12; j++) {
				tx1[0][j] = pop();
				tx1[1][j] = pop();
			}

			for (int j = 0; j < 12; j++) {
				tx2[0][j] = pop();
				tx2[1][j] = pop();
			}
			// printArray(tx1);

			tx1 = fft(tx1);
			tx2 = fft(tx2);

			for (int i = 0; i < 12; i++) {
				Tx_array[i][0] = tx1[0][i];
				Tx_array[i][1] = tx1[1][i];
				Tx_array[i + 12][0] = tx2[0][i];
				Tx_array[i + 12][1] = tx2[1][i];

			}

			for (int i = 0; i < 24; i++) {
				push(Tx_array[i][0]);
				push(Tx_array[i][1]);
			}

		}
	}

	private static class LMMSE_Invert extends edu.mit.streamjit.api.Filter<Float, Float> {

		public LMMSE_Invert() {
			super(48, 32);
		}

		@Override
		public void work() {
			double H_array[][][] = setup();
			Float[][] Tx_array = new Float[24][2];

			for (int i = 0; i < 24; i++) {
				Tx_array[i][0] = pop();
				Tx_array[i][1] = pop();
			}

			for (int i = 2; i < 10; i++) {
				double real = 0;
				double img = 0;
				for (int j = 0; j < 24; j++) {
					// System.out.println(H_array[0][i][j]);
					real += (H_array[0][i][j] * Tx_array[j][0]);
				}
				for (int j = 0; j < 24; j++) {
					real -= (H_array[1][i][j] * Tx_array[j][1]);
				}

				for (int j = 0; j < 24; j++) {
					img += (H_array[0][i][j] * Tx_array[j][1]);
				}
				for (int j = 0; j < 24; j++) {
					img += (H_array[1][i][j] * Tx_array[j][0]);
				}
				push((float) real);
				push((float) img);

			}

			for (int i = 14; i < 22; i++) {
				double real = 0;
				double img = 0;
				for (int j = 0; j < 24; j++) {
					// System.out.println(H_array[0][i][j]);
					real += (H_array[0][i][j] * Tx_array[j][0]);
				}
				for (int j = 0; j < 24; j++) {
					real -= (H_array[1][i][j] * Tx_array[j][1]);
				}

				for (int j = 0; j < 24; j++) {
					img += (H_array[0][i][j] * Tx_array[j][1]);
				}
				for (int j = 0; j < 24; j++) {
					img += (H_array[1][i][j] * Tx_array[j][0]);
				}

				push((float) real);
				push((float) img);
			}

		}
	}

	private static class LMMSE_Revert extends edu.mit.streamjit.api.Filter<Float, Float> {

		public LMMSE_Revert() {
			super(32, 32);
		}

		@Override
		public void work() {
			Float[][] R = new Float[2][16];
			for (int i = 0; i < 16; i++) {
				R[0][i] = pop();
				R[1][i] = pop();
			}

			// printArray(R);
			R = ifft(R);
			// printArray(R);
			for (int i = 0; i < 16; i++) {
				push(R[0][i]);
				push(R[1][i]);
			}
		}
	}

	private static class LMMSE_Estimate extends edu.mit.streamjit.api.Filter<Float, Float> {

		public LMMSE_Estimate() {
			super(1, 1);
		}

		@Override
		public void work() {

			float a = pop();
			// System.out.println(a);
			// System.out.println(a);

			int l = (int) Math.floor(a);
			int h = (int) Math.ceil(a);
			// System.out.println(l+" "+h);
			if (l <= -7)
				push(-7.0f);
			else if (h >= 7)
				push(7.0f);
			else {
				if (Math.abs(l) % 2 == 1)
					push((float) l);
				else
					push((float) h);
			}

		}
	}

	public static float[][] fft(float[][] tx) {
		double in[] = new double[24];
		DoubleFFT_1D fftDo = new DoubleFFT_1D(12);
		for (int i = 0; i < tx[0].length; ++i) {
			in[2 * i] = tx[0][i];
			in[2 * i + 1] = tx[1][i];

		}

		fftDo.complexForward(in);
		int count = 0;
		for (int j = 0; j < 12; ++j) {
			tx[0][count] = (float) in[2 * j];
			tx[1][count] = (float) in[2 * j + 1];
			++count;
		}

		return tx;
	}

	public static Float[][] ifft(Float[][] rx) {
		double in1[] = new double[16];
		double in2[] = new double[16];
		for (int i = 0; i < 8; ++i) {
			in1[2 * i] = rx[0][i];
			in1[2 * i + 1] = rx[1][i];

			in2[2 * i] = rx[0][i + 8];
			in2[2 * i + 1] = rx[1][i + 8];
		}

		DoubleFFT_1D fftDo = new DoubleFFT_1D(8);

		fftDo.complexInverse(in1, true);
		fftDo.complexInverse(in2, true);
		int count = 0;
		for (int j = 0; j < 8; ++j) {
			rx[0][count] = (float) in1[2 * j];
			rx[1][count] = (float) in1[2 * j + 1];
			rx[0][count + 8] = (float) in2[2 * j];
			rx[1][count + 8] = (float) in2[2 * j + 1];
			++count;
		}

		return rx;
	}

	public static double[][][] setup() {

		double[][][] H_array = new double[2][24][24];
		double[][] Hreal = {
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, -0.09820046866503074, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						-0.03414369290686697, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, -0.02632013125903408, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						-0.028437611940622942, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.03820135277975062, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.03176036400994188, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, -0.09415942094476423, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.10609646449253432, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.02265820924874495, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.04789224706542082, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.03361377308859323, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 6.153735503120136E-4, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.035311718578859466, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.03134390110523879, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.11462025448109645, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0, -0.013187379023728588, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, -0.03007777010575756, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						-0.05336566892951522, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.03928540585456253, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.01896425614572562, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, -0.06695338394839778, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.035371770287818, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.15792331035008467, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.036758545485157675, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.056691874998857944, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.02584601949360954, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.09158488751579183, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, -0.052424460054023556, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05274623944185003, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, -0.029451995504087853, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05212774730779931, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0, 0.11779633486068042, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						0.0, 0.0, 0.0, 0.0 } };
		double[][] Himg = {
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.010687289846020069, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						0.033970460651640395, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.004669423201142837, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, 0.037338717088816746, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.012777844012157397, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.019247092204946026, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.12161842935577173, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.023416719151025542, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, 0.0019769297981446082, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, 0.030679395407931655, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, 0.05091809390091878, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.04311180600477309, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.012417020818793593, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, 0.023914005706428695, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0195121153695388, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.056943801371114766, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, 0.06281428979476415, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						0.00605238700401152, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, 0.003788917200676147, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, 0.06328062654825732, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.05745994433915273, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.053692117175817275, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.07729060292563447, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, 0.012927868062324904, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0016493485450327673, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.062120871296507715, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.07063247620550091, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, 0.05540093141996923, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.04042455685799972, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.008092568732910723, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.08367172180590862, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0020481284715710216, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
				{ -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0,
						-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 } };
		// try
		// {
		// FileInputStream fileIn = new FileInputStream("params/channel
		// matrices/EPA 5Hz-Low-HLMMSEreal.ser");
		// ObjectInputStream in = new ObjectInputStream(fileIn);
		// Hreal = (double[][]) in.readObject();
		//
		// in.close();
		// fileIn.close();
		// }catch(IOException i)
		// {
		// i.printStackTrace();
		// }catch(ClassNotFoundException c)
		// {
		// c.printStackTrace();
		// }
		//
		// try
		// {
		// FileInputStream fileIn = new FileInputStream("params/channel
		// matrices/EPA 5Hz-Low-HLMMSEimg.ser");
		// ObjectInputStream in = new ObjectInputStream(fileIn);
		// Himg = (double[][]) in.readObject();
		// in.close();
		// fileIn.close();
		// }catch(IOException i)
		// {
		// i.printStackTrace();
		// }catch(ClassNotFoundException c)
		// {
		// c.printStackTrace();
		// }

		H_array[0] = Hreal;
		H_array[1] = Himg;

		// for (int i = 0; i < 24; i++) {
		// for (int j = 0; j < 24; j++) {
		// System.out.print(Hreal[i][j]+" ");
		// }
		// System.out.println();
		// }
		// System.out.println("######################");
		// for (int i = 0; i <24; i++) {
		// for (int j = 0; j < 24; j++) {
		// System.out.print(Himg[i][j]+" ");
		// }
		// System.out.println();
		// }
		// System.out.println("######################");

		return H_array;
	}

	private static void printArray(Float[][] d) {
		for (int i = 0; i < d[0].length; i++) {
			for (int j = 0; j < 2; j++) {
				System.out.print(d[j][i] + " ");
			}
			System.out.println();
		}
		System.out.println("######################");
	}

	private static void printArray(float[][] d) {
		for (int i = 0; i < d[0].length; i++) {
			for (int j = 0; j < 2; j++) {
				System.out.print(d[j][i] + " ");
			}
			System.out.println();
		}
		System.out.println("######################");
	}
}
