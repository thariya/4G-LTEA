package receiver;

import receiver.LSDTree.Node;
import weka.core.matrix.Matrix;
import weka.core.matrix.QRDecomposition;

public class LSD_Equalizer_linear extends edu.mit.streamjit.api.Pipeline<Float, Float> {

	public LSD_Equalizer_linear() {
		this.add(new EqualizerPreprocess());
		this.add(new TotalTraversal());
		this.add(new EqualizerTerminal());
	}

	// @Override
	// public void work() {
	// double s=System.nanoTime();
	// double[][] y=new double[32][1];
	// for (int i = 0; i <32; i++) {
	// y[i][0]=pop();
	// }
	// Matrix Y = Matrix.constructWithCopy(y);
	// System.out.println("##############H MAtrix################");
	// System.out.println(H);
	// System.out.println("##############################");
	// System.out.println("##############Y MAtrix################");
	// System.out.println(Y);
	// System.out.println("##############################");
	// double[] y_out = GetLSD_Y(Y);
	// for (int i = 0; i < 32; i++) {
	// push((float)y_out[i]);
	// }
	// Matrix[] a=QRD(H);
	//
	// double temp[] = new double[32];
	//
	// Y=a[0].transpose().times(Y);
	//
	// LSDTree tree=new LSDTree(1000000,32);
	// tree.setRMatrix(a[1]);
	// tree.generateFirstlevel(Y.get(31, 0));
	// double e=System.nanoTime();
	// System.out.println("Time: "+(e-s));
	// GetLSD_Y(tree,Y);
	//
	// Node minNode=tree.getMinnode();
	//
	// for (int i = 0; i < 32; i++) {
	// temp[i]=minNode.getNode_S();
	// minNode=minNode.getparent();
	// }
	//
	// for (int i = 0; i <8; i++) {
	// push((float)temp[i]);
	// push((float)temp[i+16]);
	// }
	//
	// for (int i = 0; i <8; i++) {
	// push((float)temp[i+8]);
	// push((float)temp[i+24]);
	// }
	//
	// }

	// void genH(ComplexMatrix H1){
	//
	// double h_temp[][]=new double[32][32];
	// for (int i = 0; i < 16; i++) {
	// for (int j = 0; j < 16; j++) {
	//
	// h_temp[i][j]=H1.get(i, j).getReal();
	// h_temp[i+16][j+16]=h_temp[i][j];
	// h_temp[i+16][j]=H1.get(i, j).getImaginary();
	// h_temp[i][j+16]=(-1)*h_temp[i+16][j];
	// }
	// }
	//
	// H=Matrix.constructWithCopy(h_temp);
	// }

	public static Matrix setup() {

		// double[][] Houtreal = null;
		// double[][] Houtimg = null;
		// try
		// {
		// FileInputStream fileIn = new FileInputStream("EPA
		// 5Hz-Low-Houtreal.ser");
		// ObjectInputStream in = new ObjectInputStream(fileIn);
		// Houtreal = (double[][]) in.readObject();
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
		// FileInputStream fileIn = new FileInputStream("EPA
		// 5Hz-Low-Houtimg.ser");
		// ObjectInputStream in = new ObjectInputStream(fileIn);
		// Houtimg = (double[][]) in.readObject();
		// in.close();
		// fileIn.close();
		// }catch(IOException i)
		// {
		// i.printStackTrace();
		// }catch(ClassNotFoundException c)
		// {
		// c.printStackTrace();
		// }
		double[][] Houtimg = {
				{ 4.049888583322141, 4.049888583322141, 4.049888583322141, 4.049888583322141, 4.049888583322141,
						4.049888583322141, 4.049888583322141, 4.049888583322141, -6.37558114024194, -6.37558114024194,
						-6.37558114024194, -6.37558114024194, -6.37558114024194, -6.37558114024194, -6.37558114024194,
						-6.37558114024194 },
				{ 11.642600298517829, 19.815502481778907, 16.380752056451627, 3.350379238325951, -11.642600298517829,
						-19.815502481778907, -16.380752056451627, -3.3503792383259547, 4.5478807518084805,
						-6.354436589254176, -13.534411157571569, -12.786111228517278, -4.5478807518084805,
						6.354436589254175, 13.534411157571569, 12.78611122851728 },
				{ -7.329831686317631, -12.773719252152635, 7.329831686317631, 12.773719252152635, -7.329831686317631,
						-12.773719252152635, 7.329831686317631, 12.773719252152635, 0.6580378745005215,
						8.480480553760296, -0.6580378745005215, -8.480480553760296, 0.6580378745005215,
						8.480480553760296, -0.6580378745005215, -8.480480553760296 },
				{ -1.3860360410096226, 2.1622323890388904, -1.671822328571554, 0.20208142190516998, 1.3860360410096226,
						-2.1622323890388904, 1.671822328571554, -0.20208142190517087, 0.8215549826970237,
						-4.822029177889897, 5.997824078833655, -3.660174979124572, -0.8215549826970237,
						4.822029177889902, -5.997824078833655, 3.660174979124574 },
				{ 19.66459452677597, -19.66459452677597, 19.66459452677597, -19.66459452677597, 19.66459452677597,
						-19.66459452677597, 19.66459452677597, -19.66459452677597, 8.542818438087917,
						-8.542818438087917, 8.542818438087917, -8.542818438087917, 8.542818438087917,
						-8.542818438087917, 8.542818438087917, -8.542818438087917 },
				{ -4.3674811926796, -2.3027931952211484, 7.624122560701824, -8.479344331318064, 4.3674811926796,
						2.302793195221151, -7.624122560701824, 8.479344331318066, 1.099302511206559, -4.201889265864615,
						4.843066276169108, -2.647240745365504, -1.099302511206559, 4.201889265864617,
						-4.843066276169108, 2.6472407453655067 },
				{ 2.329046525544578, 7.778660642027814, -2.329046525544578, -7.778660642027814, 2.329046525544578,
						7.778660642027814, -2.329046525544578, -7.778660642027814, 6.545383825116527, 8.185599361632505,
						-6.545383825116527, -8.185599361632505, 6.545383825116527, 8.185599361632505,
						-6.545383825116527, -8.185599361632505 },
				{ 0.6103932792513049, -9.853358568167996, -14.545146601279622, -10.71658502206658, -0.6103932792513049,
						9.853358568168016, 14.545146601279622, 10.716585022066536, -6.9953921775635655,
						-6.2209867801844885, -1.8024116983170724, 3.6719917114445613, 6.9953921775635655,
						6.220986780184483, 1.8024116983170724, -3.671991711444589 },
				{ -9.752417847702194, -9.752417847702194, -9.752417847702194, -9.752417847702194, -9.752417847702194,
						-9.752417847702194, -9.752417847702194, -9.752417847702194, 4.708103749765038,
						4.708103749765038, 4.708103749765038, 4.708103749765038, 4.708103749765038, 4.708103749765038,
						4.708103749765038, 4.708103749765038 },
				{ -11.688661410436467, -6.324942977242104, 2.7438412707842224, 10.205320515384177, 11.688661410436467,
						6.324942977242106, -2.7438412707842224, -10.205320515384177, -8.038822037091233,
						-4.818356140995244, 1.2246374341520712, 6.55025500936289, 8.038822037091233, 4.8183561409952445,
						-1.2246374341520712, -6.550255009362889 },
				{ 18.659294548258618, -7.761998919437512, -18.659294548258618, 7.761998919437512, 18.659294548258618,
						-7.761998919437512, -18.659294548258618, 7.761998919437512, 1.3286368871870646,
						-9.130321966789772, -1.3286368871870646, 9.130321966789772, 1.3286368871870646,
						-9.130321966789772, -1.3286368871870646, 9.130321966789772 },
				{ -0.995153453163327, -6.188723528403512, 9.747330200809047, -7.596083038509506, 0.995153453163327,
						6.188723528403521, -9.747330200809047, 7.5960830385095095, 8.175456086132591,
						-7.603121835428181, 2.576981929904961, 3.9587190401661942, -8.175456086132591,
						7.603121835428175, -2.576981929904961, -3.9587190401661907 },
				{ -1.6010637251533018, 1.6010637251533018, -1.6010637251533018, 1.6010637251533018, -1.6010637251533018,
						1.6010637251533018, -1.6010637251533018, 1.6010637251533018, -1.0470890273695534,
						1.0470890273695534, -1.0470890273695534, 1.0470890273695534, -1.0470890273695534,
						1.0470890273695534, -1.0470890273695534, 1.0470890273695534 },
				{ 12.169124692921628, -12.441386778030713, 5.425653223298708, 4.768354404908363, -12.169124692921628,
						12.441386778030711, -5.425653223298708, -4.768354404908356, -2.382780303717827,
						-2.9905517774448303, 6.612059186359272, -6.360311999118068, 2.382780303717827,
						2.9905517774448325, -6.612059186359272, 6.36031199911807 },
				{ -10.424847269106788, 14.263630346784373, 10.424847269106788, -14.263630346784373, -10.424847269106788,
						14.263630346784373, 10.424847269106788, -14.263630346784373, 3.0864833150831386,
						-9.46027640650577, -3.0864833150831386, 9.46027640650577, 3.0864833150831386, -9.46027640650577,
						-3.0864833150831386, 9.46027640650577 },
				{ -10.49532591289673, -3.0472202448503083, 6.185905715091557, 11.795412002694029, 10.49532591289673,
						3.047220244850288, -6.185905715091557, -11.795412002694043, 2.062575477624647,
						11.512241859198483, 14.218193092973099, 8.595319645323542, -2.062575477624647,
						-11.512241859198497, -14.218193092973099, -8.595319645323492 } };
		double[][] Houtreal = {
				{ -6.337029507853405, -6.337029507853405, -6.337029507853405, -6.337029507853405, -6.337029507853405,
						-6.337029507853405, -6.337029507853405, -6.337029507853405, 2.2003277030587425,
						2.2003277030587425, 2.2003277030587425, 2.2003277030587425, 2.2003277030587425,
						2.2003277030587425, 2.2003277030587425, 2.2003277030587425 },
				{ -16.380752056451627, -3.350379238325951, 11.642600298517829, 19.815502481778907, 16.380752056451627,
						3.350379238325953, -11.642600298517829, -19.815502481778907, 13.534411157571569,
						12.786111228517278, 4.5478807518084805, -6.354436589254176, -13.534411157571569,
						-12.78611122851728, -4.5478807518084805, 6.354436589254173 },
				{ 12.773719252152635, -7.329831686317631, -12.773719252152635, 7.329831686317631, 12.773719252152635,
						-7.329831686317631, -12.773719252152635, 7.329831686317631, -8.480480553760296,
						0.6580378745005215, 8.480480553760296, -0.6580378745005215, -8.480480553760296,
						0.6580378745005215, 8.480480553760296, -0.6580378745005215 },
				{ -1.671822328571554, 0.2020814219051703, 1.3860360410096226, -2.16223238903889, 1.671822328571554,
						-0.20208142190516754, -1.3860360410096226, 2.16223238903889, 5.997824078833655,
						-3.660174979124573, -0.8215549826970237, 4.822029177889898, -5.997824078833655,
						3.660174979124567, 0.8215549826970237, -4.822029177889896 },
				{ -6.777190862208266, 6.777190862208266, -6.777190862208266, 6.777190862208266, -6.777190862208266,
						6.777190862208266, -6.777190862208266, 6.777190862208266, 15.367198742633487,
						-15.367198742633487, 15.367198742633487, -15.367198742633487, 15.367198742633487,
						-15.367198742633487, 15.367198742633487, -15.367198742633487 },
				{ -7.624122560701824, 8.479344331318062, -4.3674811926796, -2.3027931952211387, 7.624122560701824,
						-8.47934433131806, 4.3674811926796, 2.3027931952211333, -4.843066276169108, 2.6472407453654996,
						1.099302511206559, -4.2018892658646125, 4.843066276169108, -2.647240745365498,
						-1.099302511206559, 4.201889265864611 },
				{ 7.778660642027814, -2.329046525544578, -7.778660642027814, 2.329046525544578, 7.778660642027814,
						-2.329046525544578, -7.778660642027814, 2.329046525544578, 8.185599361632505,
						-6.545383825116527, -8.185599361632505, 6.545383825116527, 8.185599361632505,
						-6.545383825116527, -8.185599361632505, 6.545383825116527 },
				{ -14.545146601279622, -10.716585022066575, -0.6103932792513049, 9.853358568167991, 14.545146601279622,
						10.716585022066557, 0.6103932792513049, -9.853358568168039, -1.8024116983170724,
						3.671991711444564, 6.9953921775635655, 6.220986780184491, 1.8024116983170724,
						-3.6719917114445746, -6.9953921775635655, -6.220986780184474 },
				{ -0.08855559281520886, -0.08855559281520886, -0.08855559281520886, -0.08855559281520886,
						-0.08855559281520886, -0.08855559281520886, -0.08855559281520886, -0.08855559281520886,
						-13.007129214016196, -13.007129214016196, -13.007129214016196, -13.007129214016196,
						-13.007129214016196, -13.007129214016196, -13.007129214016196, -13.007129214016196 },
				{ -2.7438412707842224, -10.205320515384177, -11.688661410436467, -6.324942977242104, 2.7438412707842224,
						10.205320515384177, 11.688661410436467, 6.324942977242106, -1.2246374341520712,
						-6.55025500936289, -8.038822037091233, -4.818356140995244, 1.2246374341520712, 6.55025500936289,
						8.038822037091233, 4.8183561409952445 },
				{ 7.761998919437512, 18.659294548258618, -7.761998919437512, -18.659294548258618, 7.761998919437512,
						18.659294548258618, -7.761998919437512, -18.659294548258618, 9.130321966789772,
						1.3286368871870646, -9.130321966789772, -1.3286368871870646, 9.130321966789772,
						1.3286368871870646, -9.130321966789772, -1.3286368871870646 },
				{ 9.747330200809047, -7.596083038509507, 0.995153453163327, 6.188723528403513, -9.747330200809047,
						7.596083038509499, -0.995153453163327, -6.18872352840351, 2.576981929904961, 3.958719040166193,
						-8.175456086132591, 7.60312183542818, -2.576981929904961, -3.9587190401662022,
						8.175456086132591, -7.6031218354281815 },
				{ 17.459309758649002, -17.459309758649002, 17.459309758649002, -17.459309758649002, 17.459309758649002,
						-17.459309758649002, 17.459309758649002, -17.459309758649002, 6.952565791748154,
						-6.952565791748154, 6.952565791748154, -6.952565791748154, 6.952565791748154,
						-6.952565791748154, 6.952565791748154, -6.952565791748154 },
				{ -5.425653223298708, -4.768354404908379, 12.169124692921628, -12.441386778030719, 5.425653223298708,
						4.768354404908381, -12.169124692921628, 12.441386778030722, -6.612059186359272,
						6.360311999118066, -2.382780303717827, -2.9905517774448223, 6.612059186359272,
						-6.360311999118064, 2.382780303717827, 2.9905517774448187 },
				{ 14.263630346784373, 10.424847269106788, -14.263630346784373, -10.424847269106788, 14.263630346784373,
						10.424847269106788, -14.263630346784373, -10.424847269106788, -9.46027640650577,
						-3.0864833150831386, 9.46027640650577, 3.0864833150831386, -9.46027640650577,
						-3.0864833150831386, 9.46027640650577, 3.0864833150831386 },
				{ 6.185905715091557, 11.79541200269403, 10.49532591289673, 3.0472202448503136, -6.185905715091557,
						-11.795412002694036, -10.49532591289673, -3.047220244850262, 14.218193092973099,
						8.595319645323539, -2.062575477624647, -11.512241859198479, -14.218193092973099,
						-8.595319645323517, 2.062575477624647, 11.512241859198515 } };
		double h_temp[][] = new double[32][32];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				h_temp[i][j] = Houtreal[i][j];
				h_temp[i + 16][j + 16] = h_temp[i][j];
				h_temp[i + 16][j] = Houtimg[i][j];
				h_temp[i][j + 16] = (-1) * h_temp[i + 16][j];

			}
		}
		Matrix H = Matrix.constructWithCopy(h_temp);
		return H;
	}

	public static Matrix[] QRD(Matrix Hcopy) {

		QRDecomposition QRD = new QRDecomposition(Hcopy);
		Matrix Q = QRD.getQ();
		Matrix R = QRD.getR();
		Matrix[] a = { Q, R };
		return a;

	}

	public static void GetLSD_Y(LSDTree tree, Matrix Y) {

		for (int i = 0; i < 31; i++) {
			tree.generateNextlevel(Y.get(31 - (i + 1), 0), i + 1);
		}
	}

	private static class EqualizerPreprocess extends edu.mit.streamjit.api.Filter<Float, Level> {

		public EqualizerPreprocess() {
			super(32, 1);
		}

		@Override
		public void work() {
			Matrix H = setup();
			double[][] y = new double[32][1];
			for (int i = 0; i < 32; i++) {
				y[i][0] = pop();
			}
			Matrix Y = Matrix.constructWithCopy(y);
			// System.out.println("##############H MAtrix################");
			// System.out.println(H);
			// System.out.println("##############################");
			// System.out.println("##############Y MAtrix################");
			// System.out.println(Y);
			// System.out.println("##############################");
			// double[] y_out = GetLSD_Y(Y);
			// for (int i = 0; i < 32; i++) {
			// push((float)y_out[i]);
			// }
			Matrix[] a = QRD(H);

			Y = a[0].transpose().times(Y);

			LSDTree tree = new LSDTree(1000000, 32);
			tree.setRMatrix(a[1]);
			tree.generateFirstlevel(Y.get(31, 0));
			push(new Level(tree, Y));
		}
	}

	private static class TreeTraversal extends edu.mit.streamjit.api.Filter<Level, Level> {
		int i;

		public TreeTraversal(int x) {
			super(1, 1);
			i = x;
		}

		@Override
		public void work() {
			Level l = pop();
			l.t.generateNextlevel(l.Y.get(31 - (i + 1), 0), i + 1);
			push(l);
		}
	}

	private static class TotalTraversal extends edu.mit.streamjit.api.Filter<Level, Level> {

		public TotalTraversal() {
			super(1, 1);

		}

		@Override
		public void work() {
			Level l = pop();
			for (int j = 0; j < 31; j++) {
				l.t.generateNextlevel(l.Y.get(31 - (j + 1), 0), j + 1);
			}
			push(l);
		}
	}

	private static class EqualizerTerminal extends edu.mit.streamjit.api.Filter<Level, Float> {

		public EqualizerTerminal() {
			super(1, 32);
		}

		@Override
		public void work() {
			Level l = pop();
			double temp[] = new double[32];
			Node minNode = l.t.getMinnode();

			for (int i = 0; i < 32; i++) {
				temp[i] = minNode.getNode_S();
				minNode = minNode.getparent();
			}

			for (int i = 0; i < 8; i++) {
				push((float) temp[i]);
				push((float) temp[i + 16]);
			}

			for (int i = 0; i < 8; i++) {
				push((float) temp[i + 8]);
				push((float) temp[i + 24]);
			}
		}
	}

	public static class Level {
		public LSDTree t;
		public Matrix Y;

		public Level(LSDTree tree, Matrix x) {
			t = tree;
			Y = x;
		}

	}

}
