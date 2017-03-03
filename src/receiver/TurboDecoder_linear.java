package receiver;

import edu.mit.streamjit.api.RoundrobinJoiner;
import edu.mit.streamjit.api.RoundrobinSplitter;
import edu.mit.streamjit.api.Splitjoin;

public class TurboDecoder_linear extends edu.mit.streamjit.api.Pipeline<Byte, Float> {

	public static int block_size = 40;
	public static int[] zero_transitions = { 0, 4, 5, 1, 2, 6, 7, 3 };
	public static int[] one_transitions = { 4, 0, 1, 5, 6, 2, 3, 7 };
	public static int[] zero_transitions_gammas = { 0, 1, 1, 0, 0, 1, 1, 0 };
	public static int[] one_transitions_gammas = { 3, 2, 2, 3, 3, 2, 2, 3 };
	public static int[] reverse_zero_transitions_gammas = { 0, 0, 1, 1, 1, 1, 0, 0 };
	static int[] reverse_one_transitions_gammas = { 3, 3, 2, 2, 2, 2, 3, 3 };
	public static int[] reverse_zero_transitions = { 0, 3, 4, 7, 1, 2, 5, 6 };
	public static int[] reverse_one_transitions = { 1, 2, 5, 6, 0, 3, 4, 7 };

	public static int[] pos_1 = { 0, 4 };
	public static int[] pos_1_zeros = { 0 };
	public static int[] pos_1_ones = { 4 };
	public static int[] pos_2 = { 0, 2, 4, 6 };
	public static int[] pos_2_zeros = { 0, 2 };
	public static int[] pos_2_ones = { 4, 6 };
	public static int[] pos_3 = { 0, 1, 2, 3, 4, 5, 6, 7 };
	public static int[] pos_3_zeros = { 0, 2, 5, 7 };
	public static int[] pos_3_ones = { 1, 3, 4, 6 };
	public static int[] pos_40_zeros = { 0, 3, 4, 7 };
	public static int[] pos_40_ones = { 1, 2, 5, 6 };
	public static int[] pos_41_zeros = { 0, 3 };
	public static int[] pos_41_ones = { 1, 2 };
	public static int[] pos_42_zeros = { 0 };
	public static int[] pos_42_ones = { 1 };
	public static int[] pos_43 = { 0 };

	public static int[] perms = new int[40];
	public static int[] reverse_perms = new int[40];
	public static int f1 = 3;
	public static int f2 = 10;

	public static float Lc = 5.0f;

	public TurboDecoder_linear() {
		super(new PolarEncoder(), new Divider(),
				// new BytePrinter(),
				new Splitjoin<Byte, Float>(new RoundrobinSplitter<Byte>(86), new RoundrobinJoiner<Float>(2),
						new Lprime(), new Lprime()),
				new Connector(),
				// new Printer(),
				new Decoder1(), new Decoder2(), new Decoder1(), new Decoder2(), new Decoder1(), new Decoder2(),
				new Decoder1(), new Decoder2(), new Decoder1(), new Decoder2(), new Terminator(), new Predictor());

		for (int i = 0; i < perms.length; i++) {
			int val = (f1 * i + f2 * i * i) % block_size;
			perms[i] = val;
			reverse_perms[val] = i;
		}

	}

	private static class PolarEncoder extends edu.mit.streamjit.api.Filter<Byte, Byte> {

		public PolarEncoder() {
			super(1, 1);
		}

		@Override
		public void work() {

			if (pop() == 0)
				push((byte) -1);
			else
				push((byte) 1);

		}
	}

	private static class L extends edu.mit.streamjit.api.Pipeline<Byte, Float> {

		public L() {
			this.add(
					new Preprocess(), new Splitjoin<Data, Data>(new RoundrobinSplitter<Data>(),
							new RoundrobinJoiner<Data>(), new Dummy(), new AlphaTraversal(), new BetaTraversal()),
					new Postprocess());
		}

	}

	private static class Divider extends edu.mit.streamjit.api.Filter<Byte, Byte> {
		byte[] data1 = new byte[block_size + 3];
		byte[] data2 = new byte[block_size + 3];
		byte[] parity1 = new byte[block_size + 3];
		byte[] parity2 = new byte[block_size + 3];

		public Divider() {
			super(132, 172);
		}

		@Override
		public void work() {
			for (int i = 0; i < 40; i++) {
				data1[i] = pop();
				data2[perms[i]] = data1[i];
				parity1[i] = pop();
				parity2[i] = pop();
			}

			for (int i = 40; i < 43; i++) {
				data1[i] = pop();
				parity1[i] = pop();
			}
			for (int i = 40; i < 43; i++) {
				data2[i] = pop();
				parity2[i] = pop();
			}

			for (int i = 0; i < 43; i++) {
				push(data1[i]);
				push(parity1[i]);
			}

			for (int i = 0; i < 43; i++) {
				push(data2[i]);
				push(parity2[i]);
			}
		}
	}

	private static class Lprime extends edu.mit.streamjit.api.Filter<Byte, Float> {

		public Lprime() {
			super(86, 80);
		}

		@Override
		public void work() {
			Byte[] y = new Byte[43];
			Byte[] parity = new Byte[43];

			for (int i = 0; i < y.length; i++) {
				y[i] = pop();
				parity[i] = pop();
				// System.out.println(y[i]);
				// System.out.println(parity[i]);

			}

			float[][] alpha = new float[8][44];
			float[][] beta = new float[8][44];
			float[][] gamma = new float[4][44];

			// gamma calculation

			for (int i = 1; i < 44; i++) {
				gamma[0][i] = Lc / 2 * (-parity[i - 1] - y[i - 1]);
				gamma[1][i] = Lc / 2 * (parity[i - 1] - y[i - 1]);
				gamma[2][i] = Lc / 2 * (-parity[i - 1] + y[i - 1]);
				gamma[3][i] = Lc / 2 * (parity[i - 1] + y[i - 1]);
			}

			// alpha update

			alpha[0][0] = 0;

			for (int i = 0; i < pos_1_zeros.length; i++) {
				int pos = 1;
				int current = pos_1_zeros[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities
						.max_e(alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos], 0f);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos);
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+" ");
			}
			for (int i = 0; i < pos_1_ones.length; i++) {
				int pos = 1;
				int current = pos_1_ones[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities.max_e(0f,
						alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos);
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+" ");
			}

			for (int i = 0; i < pos_2_zeros.length; i++) {
				int pos = 2;
				int current = pos_2_zeros[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities
						.max_e(alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos], 0f);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}
			for (int i = 0; i < pos_2_ones.length; i++) {
				int pos = 2;
				int current = pos_2_ones[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities.max_e(0f,
						alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}

			for (int i = 0; i < pos_3_zeros.length; i++) {
				int pos = 3;
				int current = pos_3_zeros[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities
						.max_e(alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos], 0f);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}
			for (int i = 0; i < pos_3_ones.length; i++) {
				int pos = 3;
				int current = pos_3_ones[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities.max_e(0f,
						alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}

			for (int i = 4; i < 41; i++) {
				int pos = i;
				for (int j = 0; j < 8; j++) {
					int current = j;

					int zero_pos = reverse_zero_transitions[current];
					int one_pos = reverse_one_transitions[current];

					alpha[current][pos] = Utilities.max_e(
							alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos],
							alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				}
			}

			// beta update

			beta[0][43] = 0;

			for (int i = 0; i < pos_42_zeros.length; i++) {
				int pos = 42;
				int current = pos_42_zeros[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities
						.max_e(beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1], 0f);

			}
			for (int i = 0; i < pos_42_ones.length; i++) {
				int pos = 42;
				int current = pos_42_ones[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(0f,
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);

			}

			for (int i = 0; i < pos_41_zeros.length; i++) {
				int pos = 41;
				int current = pos_41_zeros[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities
						.max_e(beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1], 0f);
			}
			for (int i = 0; i < pos_41_ones.length; i++) {
				int pos = 41;
				int current = pos_41_ones[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(0f,
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 0; i < pos_40_zeros.length; i++) {
				int pos = 40;
				int current = pos_40_zeros[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities
						.max_e(beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1], 0f);
			}
			for (int i = 0; i < pos_40_ones.length; i++) {
				int pos = 40;
				int current = pos_40_ones[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(0f,
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 39; i > 2; i--) {
				int pos = i;
				for (int j = 0; j < 8; j++) {
					int current = j;

					int zero_pos = zero_transitions[current];
					int one_pos = one_transitions[current];

					beta[current][pos] = Utilities.max_e(
							beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
							beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
				}
			}

			for (int i = 0; i < pos_2.length; i++) {
				int pos = 2;
				int current = pos_2[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(
						beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 0; i < pos_1.length; i++) {
				int pos = 1;
				int current = pos_1[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(
						beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 0; i < 1; i++) {
				int pos = 0;
				int current = 0;
				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];
				beta[current][pos] = Utilities.max_e(
						beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			// LLR calculation

			push((alpha[0][0] + gamma[one_transitions_gammas[4]][1] + beta[4][1])
					- (alpha[0][0] + gamma[zero_transitions_gammas[0]][1] + beta[0][1]));
			push((float) y[0]);

			float[] ones = new float[2];
			float[] zeros = new float[2];
			for (int j = 0; j < pos_1.length; j++) {
				int pos = 1;
				int current = pos_1[j];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				zeros[j] = alpha[current][pos] + gamma[zero_transitions_gammas[zero_pos]][pos + 1]
						+ beta[zero_pos][pos + 1];
				ones[j] = alpha[current][pos] + gamma[one_transitions_gammas[one_pos]][pos + 1]
						+ beta[one_pos][pos + 1];
			}

			push(Utilities.max_e(ones) - Utilities.max_e(zeros));
			push((float) y[1]);

			ones = new float[4];
			zeros = new float[4];
			for (int j = 0; j < pos_2.length; j++) {
				int pos = 2;
				int current = pos_2[j];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				zeros[j] = alpha[current][pos] + gamma[zero_transitions_gammas[zero_pos]][pos + 1]
						+ beta[zero_pos][pos + 1];
				ones[j] = alpha[current][pos] + gamma[one_transitions_gammas[one_pos]][pos + 1]
						+ beta[one_pos][pos + 1];
			}
			//
			push(Utilities.max_e(ones) - Utilities.max_e(zeros));
			push((float) y[2]);

			for (int i = 3; i < 40; i++) {
				int pos = i;
				ones = new float[8];
				zeros = new float[8];

				for (int j = 0; j < 8; j++) {
					int current = j;

					int zero_pos = zero_transitions[current];
					int one_pos = one_transitions[current];

					zeros[j] = alpha[current][pos] + gamma[zero_transitions_gammas[zero_pos]][pos + 1]
							+ beta[zero_pos][pos + 1];
					ones[j] = alpha[current][pos] + gamma[one_transitions_gammas[one_pos]][pos + 1]
							+ beta[one_pos][pos + 1];
				}
				float temp = Utilities.max_e(ones) - Utilities.max_e(zeros);
				// System.out.println(temp);
				push(temp);
				push((float) y[i]);
			}

			// System.out.println("@@@@@@@@@@@@@@@@@@@@");
			// System.out.println("GAMMA");
			// for (int i = 0; i < 4; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(gamma[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println("ALPHA");
			// for (int i = 0; i < 8; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(alpha[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println("BETA");
			// for (int i = 0; i < 8; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(beta[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println();
			// System.out.println();
		}
	}

	// private static class Lprime2 extends
	// edu.mit.streamjit.api.Filter<Byte, Float> {
	//
	//
	// public Lprime2() {
	// super(86, 80);
	// }
	//
	// @Override
	// public void work() {
	// Byte[] y=new Byte[43];
	// Byte[] parity=new Byte[43];
	//
	// for (int i = 0; i < y.length; i++) {
	// y[i]=pop();
	// parity[i]=pop();
	//// System.out.println(y[i]);
	//// System.out.println(parity[i]);
	//
	// }
	//
	// float[][] alpha=new float[8][44];
	// float[][] beta=new float[8][44];
	// float[][] gamma=new float[4][44];
	//
	// //gamma calculation
	//
	// for (int i = 1; i < 44; i++) {
	// gamma[0][i]=Lc/2*(-parity[i-1]-y[i-1]);
	// gamma[1][i]=Lc/2*(parity[i-1]-y[i-1]);
	// gamma[2][i]=Lc/2*(-parity[i-1]+y[i-1]);
	// gamma[3][i]=Lc/2*(parity[i-1]+y[i-1]);
	// }
	//
	//
	// //alpha update
	//
	// alpha[0][0]=0;
	//
	// for (int i = 0; i < pos_1_zeros.length; i++) {
	// int pos=1;
	// int current=pos_1_zeros[i];
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(alpha[zero_pos][pos-1]+gamma[zero_transitions_gammas[current]][pos],0f);
	//// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
	// "+zero_transitions_gammas[current]+" "+pos);
	//// System.out.println(alpha[zero_pos][pos-1]+"
	// "+gamma[zero_transitions_gammas[current]][pos]+" ");
	// }
	// for (int i = 0; i < pos_1_ones.length; i++) {
	// int pos=1;
	// int current=pos_1_ones[i];
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(0f,alpha[one_pos][pos-1]+gamma[one_transitions_gammas[current]][pos]);
	//// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
	// "+zero_transitions_gammas[current]+" "+pos);
	//// System.out.println(alpha[zero_pos][pos-1]+"
	// "+gamma[zero_transitions_gammas[current]][pos]+" ");
	// }
	//
	// for (int i = 0; i < pos_2_zeros.length; i++) {
	// int pos=2;
	// int current=pos_2_zeros[i];
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(alpha[zero_pos][pos-1]+gamma[zero_transitions_gammas[current]][pos],0f);
	//// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
	// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+" "+(pos-1)+"
	// "+one_transitions_gammas[current]+" "+(pos)+" " );
	//// System.out.println(alpha[zero_pos][pos-1]+"
	// "+gamma[zero_transitions_gammas[current]][pos]+"
	// "+alpha[one_pos][pos-1]+" "+gamma[one_transitions_gammas[current]][pos]);
	// }
	// for (int i = 0; i < pos_2_ones.length; i++) {
	// int pos=2;
	// int current=pos_2_ones[i];
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(0f,alpha[one_pos][pos-1]+gamma[one_transitions_gammas[current]][pos]);
	//// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
	// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+" "+(pos-1)+"
	// "+one_transitions_gammas[current]+" "+(pos)+" " );
	//// System.out.println(alpha[zero_pos][pos-1]+"
	// "+gamma[zero_transitions_gammas[current]][pos]+"
	// "+alpha[one_pos][pos-1]+" "+gamma[one_transitions_gammas[current]][pos]);
	// }
	//
	// for (int i = 0; i < pos_3_zeros.length; i++) {
	// int pos=3;
	// int current=pos_3_zeros[i];
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(alpha[zero_pos][pos-1]+gamma[zero_transitions_gammas[current]][pos],0f);
	//// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
	// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+" "+(pos-1)+"
	// "+one_transitions_gammas[current]+" "+(pos)+" " );
	//// System.out.println(alpha[zero_pos][pos-1]+"
	// "+gamma[zero_transitions_gammas[current]][pos]+"
	// "+alpha[one_pos][pos-1]+" "+gamma[one_transitions_gammas[current]][pos]);
	// }
	// for (int i = 0; i < pos_3_ones.length; i++) {
	// int pos=3;
	// int current=pos_3_ones[i];
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(0f,alpha[one_pos][pos-1]+gamma[one_transitions_gammas[current]][pos]);
	//// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
	// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+" "+(pos-1)+"
	// "+one_transitions_gammas[current]+" "+(pos)+" " );
	//// System.out.println(alpha[zero_pos][pos-1]+"
	// "+gamma[zero_transitions_gammas[current]][pos]+"
	// "+alpha[one_pos][pos-1]+" "+gamma[one_transitions_gammas[current]][pos]);
	// }
	//
	// for (int i = 4; i < 41; i++) {
	// int pos=i;
	// for (int j = 0; j <8; j++) {
	// int current=j;
	//
	// int zero_pos=reverse_zero_transitions[current];
	// int one_pos=reverse_one_transitions[current];
	//
	// alpha[current][pos]=Utilities.max_e(alpha[zero_pos][pos-1]+gamma[zero_transitions_gammas[current]][pos],alpha[one_pos][pos-1]+gamma[one_transitions_gammas[current]][pos]);
	// }
	// }
	//
	//
	// //beta update
	//
	// beta[0][43]=0;
	//
	// for (int i = 0; i < pos_42_zeros.length; i++) {
	// int pos=42;
	// int current=pos_42_zeros[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],0f);
	//
	// }
	// for (int i = 0; i < pos_42_ones.length; i++) {
	// int pos=42;
	// int current=pos_42_ones[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(0f,beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	//
	// }
	//
	// for (int i = 0; i < pos_41_zeros.length; i++) {
	// int pos=41;
	// int current=pos_41_zeros[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],0f);
	// }
	// for (int i = 0; i < pos_41_ones.length; i++) {
	// int pos=41;
	// int current=pos_41_ones[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(0f,beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	// }
	//
	// for (int i = 0; i < pos_40_zeros.length; i++) {
	// int pos=40;
	// int current=pos_40_zeros[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],0f);
	// }
	// for (int i = 0; i < pos_40_ones.length; i++) {
	// int pos=40;
	// int current=pos_40_ones[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(0f,beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	// }
	//
	// for (int i = 39; i >2; i--) {
	// int pos=i;
	// for (int j = 0; j <8; j++) {
	// int current=j;
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	// }
	// }
	//
	// for (int i = 0; i < pos_2.length; i++) {
	// int pos=2;
	// int current=pos_2[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	// }
	//
	// for (int i = 0; i < pos_1.length; i++) {
	// int pos=1;
	// int current=pos_1[i];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	// }
	//
	// for (int i = 0; i <1; i++) {
	// int pos=0;
	// int current=0;
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	// beta[current][pos]=Utilities.max_e(beta[zero_pos][pos+1]+gamma[zero_transitions_gammas[zero_pos]][pos+1],beta[one_pos][pos+1]+gamma[one_transitions_gammas[one_pos]][pos+1]);
	// }
	//
	//
	// //LLR calculation
	//
	// push((alpha[0][0]+gamma[one_transitions_gammas[4]][1]+beta[4][1])-(alpha[0][0]+gamma[zero_transitions_gammas[0]][1]+beta[0][1]));
	// push((float)y[0]);
	//
	// float[] ones=new float[2];
	// float[] zeros=new float[2];
	// for (int j = 0; j <pos_1.length; j++) {
	// int pos=1;
	// int current=pos_1[j];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// zeros[j]=alpha[current][pos]+gamma[zero_transitions_gammas[zero_pos]][pos+1]+beta[zero_pos][pos+1];
	// ones[j]=alpha[current][pos]+gamma[one_transitions_gammas[one_pos]][pos+1]+beta[one_pos][pos+1];
	// }
	//
	// push(Utilities.max_e(ones)-Utilities.max_e(zeros));
	// push((float)y[1]);
	//
	// ones=new float[4];
	// zeros=new float[4];
	// for (int j = 0; j <pos_2.length; j++) {
	// int pos=2;
	// int current=pos_2[j];
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// zeros[j]=alpha[current][pos]+gamma[zero_transitions_gammas[zero_pos]][pos+1]+beta[zero_pos][pos+1];
	// ones[j]=alpha[current][pos]+gamma[one_transitions_gammas[one_pos]][pos+1]+beta[one_pos][pos+1];
	// }
	////
	// push(Utilities.max_e(ones)-Utilities.max_e(zeros));
	// push((float)y[2]);
	//
	//
	// for (int i = 3; i <40; i++) {
	// int pos=i;
	// ones=new float[8];
	// zeros=new float[8];
	//
	// for (int j = 0; j <8; j++) {
	// int current=j;
	//
	// int zero_pos=zero_transitions[current];
	// int one_pos=one_transitions[current];
	//
	// zeros[j]=alpha[current][pos]+gamma[zero_transitions_gammas[zero_pos]][pos+1]+beta[zero_pos][pos+1];
	// ones[j]=alpha[current][pos]+gamma[one_transitions_gammas[one_pos]][pos+1]+beta[one_pos][pos+1];
	// }
	// float temp=Utilities.max_e(ones)-Utilities.max_e(zeros);
	//// System.out.println(temp);
	// push(temp);
	// push((float)y[i]);
	// }
	//
	//// System.out.println("@@@@@@@@@@@@@@@@@@@@");
	//// System.out.println("GAMMA");
	//// for (int i = 0; i < 4; i++) {
	//// for (int j = 0; j < 44; j++) {
	//// System.out.print(gamma[i][j]+" ");
	//// }
	//// System.out.println();
	//// }
	//// System.out.println("ALPHA");
	//// for (int i = 0; i < 8; i++) {
	//// for (int j = 0; j < 44; j++) {
	//// System.out.print(alpha[i][j]+" ");
	//// }
	//// System.out.println();
	//// }
	//// System.out.println("BETA");
	//// for (int i = 0; i < 8; i++) {
	//// for (int j = 0; j < 44; j++) {
	//// System.out.print(beta[i][j]+" ");
	//// }
	//// System.out.println();
	//// }
	//// System.out.println();
	//// System.out.println();
	// }
	// }

	private static class Preprocess extends edu.mit.streamjit.api.Filter<Byte, Data> {

		public Preprocess() {
			super(86, 3);

		}

		@Override
		public void work() {

			byte[] y = new byte[43];
			byte[] parity = new byte[43];

			for (int i = 0; i < 43; i++) {
				y[i] = pop();
				parity[i] = pop();
				// System.out.println(y[i]);
				// System.out.println(parity[i]);
			}

			float[][] gamma = new float[4][44];
			float[][] gamma1 = new float[4][44];
			float[][] gamma2 = new float[4][44];

			// gamma calculation

			for (int i = 1; i < 44; i++) {
				gamma[0][i] = Lc / 2 * (-parity[i - 1] - y[i - 1]);
				gamma1[0][i] = gamma[0][i];
				gamma2[0][i] = gamma[0][i];
				gamma[1][i] = Lc / 2 * (parity[i - 1] - y[i - 1]);
				gamma1[1][i] = gamma[1][i];
				gamma2[1][i] = gamma[1][i];
				gamma[2][i] = Lc / 2 * (-parity[i - 1] + y[i - 1]);
				gamma1[2][i] = gamma[2][i];
				gamma2[2][i] = gamma[2][i];
				gamma[3][i] = Lc / 2 * (parity[i - 1] + y[i - 1]);
				gamma1[3][i] = gamma[3][i];
				gamma2[3][i] = gamma[3][i];
			}

			push(new Data(y, gamma));
			push(new Data(y, gamma1));
			push(new Data(y, gamma2));

		}
	}

	private static class AlphaTraversal extends edu.mit.streamjit.api.Filter<Data, Data> {

		public AlphaTraversal() {
			super(1, 1);

		}

		@Override
		public void work() {
			float[][] gamma;
			Data d = pop();
			gamma = d.a;
			float[][] alpha = new float[8][44];

			alpha[0][0] = 0;

			for (int i = 0; i < pos_1_zeros.length; i++) {
				int pos = 1;
				int current = pos_1_zeros[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities
						.max_e(alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos], 0f);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos);
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+" ");
			}
			for (int i = 0; i < pos_1_ones.length; i++) {
				int pos = 1;
				int current = pos_1_ones[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities.max_e(0f,
						alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos);
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+" ");
			}

			for (int i = 0; i < pos_2_zeros.length; i++) {
				int pos = 2;
				int current = pos_2_zeros[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities
						.max_e(alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos], 0f);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}
			for (int i = 0; i < pos_2_ones.length; i++) {
				int pos = 2;
				int current = pos_2_ones[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities.max_e(0f,
						alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}

			for (int i = 0; i < pos_3_zeros.length; i++) {
				int pos = 3;
				int current = pos_3_zeros[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities
						.max_e(alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos], 0f);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}
			for (int i = 0; i < pos_3_ones.length; i++) {
				int pos = 3;
				int current = pos_3_ones[i];

				int zero_pos = reverse_zero_transitions[current];
				int one_pos = reverse_one_transitions[current];

				alpha[current][pos] = Utilities.max_e(0f,
						alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				// System.out.println(current+" "+pos+" "+zero_pos+" "+(pos-1)+"
				// "+zero_transitions_gammas[current]+" "+pos+" "+one_pos+"
				// "+(pos-1)+" "+one_transitions_gammas[current]+" "+(pos)+" "
				// );
				// System.out.println(alpha[zero_pos][pos-1]+"
				// "+gamma[zero_transitions_gammas[current]][pos]+"
				// "+alpha[one_pos][pos-1]+"
				// "+gamma[one_transitions_gammas[current]][pos]);
			}

			for (int i = 4; i < 41; i++) {
				int pos = i;
				for (int j = 0; j < 8; j++) {
					int current = j;

					int zero_pos = reverse_zero_transitions[current];
					int one_pos = reverse_one_transitions[current];

					alpha[current][pos] = Utilities.max_e(
							alpha[zero_pos][pos - 1] + gamma[zero_transitions_gammas[current]][pos],
							alpha[one_pos][pos - 1] + gamma[one_transitions_gammas[current]][pos]);
				}
			}
			d.seta(alpha);
			// System.out.println(d);
			push(d);

		}
	}

	private static class BetaTraversal extends edu.mit.streamjit.api.Filter<Data, Data> {

		public BetaTraversal() {
			super(1, 1);

		}

		@Override
		public void work() {
			float[][] gamma;
			Data d = pop();
			gamma = d.a;
			float[][] beta = new float[8][44];
			beta[0][43] = 0;

			for (int i = 0; i < pos_42_zeros.length; i++) {
				int pos = 42;
				int current = pos_42_zeros[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities
						.max_e(beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1], 0f);

			}
			for (int i = 0; i < pos_42_ones.length; i++) {
				int pos = 42;
				int current = pos_42_ones[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(0f,
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);

			}

			for (int i = 0; i < pos_41_zeros.length; i++) {
				int pos = 41;
				int current = pos_41_zeros[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities
						.max_e(beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1], 0f);
			}
			for (int i = 0; i < pos_41_ones.length; i++) {
				int pos = 41;
				int current = pos_41_ones[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(0f,
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 0; i < pos_40_zeros.length; i++) {
				int pos = 40;
				int current = pos_40_zeros[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities
						.max_e(beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1], 0f);
			}
			for (int i = 0; i < pos_40_ones.length; i++) {
				int pos = 40;
				int current = pos_40_ones[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(0f,
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 39; i > 2; i--) {
				int pos = i;
				for (int j = 0; j < 8; j++) {
					int current = j;

					int zero_pos = zero_transitions[current];
					int one_pos = one_transitions[current];

					beta[current][pos] = Utilities.max_e(
							beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
							beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
				}
			}

			for (int i = 0; i < pos_2.length; i++) {
				int pos = 2;
				int current = pos_2[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(
						beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 0; i < pos_1.length; i++) {
				int pos = 1;
				int current = pos_1[i];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				beta[current][pos] = Utilities.max_e(
						beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			for (int i = 0; i < 1; i++) {
				int pos = 0;
				int current = 0;
				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];
				beta[current][pos] = Utilities.max_e(
						beta[zero_pos][pos + 1] + gamma[zero_transitions_gammas[zero_pos]][pos + 1],
						beta[one_pos][pos + 1] + gamma[one_transitions_gammas[one_pos]][pos + 1]);
			}

			d.seta(beta);
			// System.out.println(d);
			push(d);
		}
	}

	private static class Postprocess extends edu.mit.streamjit.api.Filter<Data, Float> {

		public Postprocess() {
			super(3, 80);

		}

		@Override
		public void work() {

			float[][] alpha = null;
			float[][] beta = null;
			float[][] gamma = null;
			byte[] y;
			Data d;

			d = pop();
			gamma = d.a;
			y = d.y;
			// System.out.println(d);
			// System.out.println("@@@@@@@@@@@@@@@@@@@@");
			// System.out.println("GAMMA");
			// for (int i = 0; i < 4; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(gamma[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println("ALPHA");
			// for (int i = 0; i < 8; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(alpha[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println("BETA");
			// for (int i = 0; i < 8; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(beta[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println();
			// System.out.println();

			d = pop();
			alpha = d.a;
			// System.out.println(d);
			d = pop();
			beta = d.a;
			// System.out.println(d);
			// System.out.println("@@@@@@@@@@@@@@@@@@@@");
			// System.out.println("GAMMA");
			// for (int i = 0; i < 4; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(gamma[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println("ALPHA");
			// for (int i = 0; i < 8; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(alpha[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println("BETA");
			// for (int i = 0; i < 8; i++) {
			// for (int j = 0; j < 44; j++) {
			// System.out.print(beta[i][j]+" ");
			// }
			// System.out.println();
			// }
			// System.out.println();
			// System.out.println();

			push((alpha[0][0] + gamma[one_transitions_gammas[4]][1] + beta[4][1])
					- (alpha[0][0] + gamma[zero_transitions_gammas[0]][1] + beta[0][1]));
			push((float) y[0]);

			float[] ones = new float[2];
			float[] zeros = new float[2];
			for (int j = 0; j < pos_1.length; j++) {
				int pos = 1;
				int current = pos_1[j];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				zeros[j] = alpha[current][pos] + gamma[zero_transitions_gammas[zero_pos]][pos + 1]
						+ beta[zero_pos][pos + 1];
				ones[j] = alpha[current][pos] + gamma[one_transitions_gammas[one_pos]][pos + 1]
						+ beta[one_pos][pos + 1];
			}

			push(Utilities.max_e(ones) - Utilities.max_e(zeros));
			push((float) y[1]);

			ones = new float[4];
			zeros = new float[4];
			for (int j = 0; j < pos_2.length; j++) {
				int pos = 2;
				int current = pos_2[j];

				int zero_pos = zero_transitions[current];
				int one_pos = one_transitions[current];

				zeros[j] = alpha[current][pos] + gamma[zero_transitions_gammas[zero_pos]][pos + 1]
						+ beta[zero_pos][pos + 1];
				ones[j] = alpha[current][pos] + gamma[one_transitions_gammas[one_pos]][pos + 1]
						+ beta[one_pos][pos + 1];
			}
			//
			push(Utilities.max_e(ones) - Utilities.max_e(zeros));
			push((float) y[2]);

			for (int i = 3; i < 40; i++) {
				int pos = i;
				ones = new float[8];
				zeros = new float[8];

				for (int j = 0; j < 8; j++) {
					int current = j;

					int zero_pos = zero_transitions[current];
					int one_pos = one_transitions[current];

					zeros[j] = alpha[current][pos] + gamma[zero_transitions_gammas[zero_pos]][pos + 1]
							+ beta[zero_pos][pos + 1];
					ones[j] = alpha[current][pos] + gamma[one_transitions_gammas[one_pos]][pos + 1]
							+ beta[one_pos][pos + 1];
				}
				float temp = Utilities.max_e(ones) - Utilities.max_e(zeros);
				// System.out.println(temp);
				push(temp);
				push((float) y[i]);
			}

		}
	}

	private static class Dummy extends edu.mit.streamjit.api.Filter<Data, Data> {

		public Dummy() {
			super(1, 1);

		}

		@Override
		public void work() {
			push(pop());
		}
	}

	private static class Connector extends edu.mit.streamjit.api.Filter<Float, Float> {

		public Connector() {
			super(4, 5);
		}

		@Override
		public void work() {
			push(pop());
			push(pop());
			push(0f);
			push(pop());
			push(pop());

		}
	}

	private static class Decoder1 extends edu.mit.streamjit.api.Filter<Float, Float> {

		float[] L = new float[40];
		float[] LLR = new float[40];
		float[] y = new float[40];

		public Decoder1() {
			super(200, 200);

		}

		@Override
		public void work() {

			for (int i = 0; i < 40; i++) {
				LLR[i] = pop();
				y[i] = pop();
				float prev = pop();
				L[perms[i]] = LLR[i] - y[i] * Lc - prev;
				push(pop());
				push(pop());
				push(L[i]);
				push(LLR[i]);
				push(y[i]);
			}

		}
	}

	private static class Decoder2 extends edu.mit.streamjit.api.Filter<Float, Float> {

		float[] L = new float[40];
		float[] LLR = new float[40];
		float[] y = new float[40];

		public Decoder2() {
			super(200, 200);

		}

		@Override
		public void work() {

			for (int i = 0; i < 40; i++) {
				LLR[i] = pop();
				y[i] = pop();
				float prev = pop();
				L[reverse_perms[i]] = LLR[i] - y[i] * Lc - prev;
				push(pop());
				push(pop());
				push(L[i]);
				push(LLR[i]);
				push(y[i]);
			}

		}
	}

	private static class Terminator extends edu.mit.streamjit.api.Filter<Float, Float> {

		public Terminator() {
			super(5, 1);

		}

		@Override
		public void work() {

			float LLR = pop();
			pop();
			float prev = pop();
			push(LLR + prev);
			pop();
			pop();

		}
	}

	private static class Predictor extends edu.mit.streamjit.api.Filter<Float, Byte> {

		public Predictor() {
			super(1, 1);

		}

		@Override
		public void work() {

			if (pop() > 0)
				push((byte) 1);
			else
				push((byte) 0);

		}
	}

	private static class BytePrinter extends edu.mit.streamjit.api.Filter<Byte, Byte> {

		public BytePrinter() {
			super(1, 1);
		}

		@Override
		public void work() {
			byte a = pop();
			System.out.println(a);
			push(a);

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
