package channel;


import java.util.Random;

import weka.core.matrix.*;

import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;
import org.jtransforms.fft.DoubleFFT_1D;

public class newChannel extends
edu.mit.streamjit.api.Filter<Float, Float>{
	
	static String channel_type="EPA 5Hz";
	static String corr_type="Low";
	static double fcarry=2;
	static double sigma=0;
	static ComplexMatrix H_out;
	static int length=16;
	static Matrix sqrt_corr_matrix;
	double no_taps = 0;
	static int path_delays[] = { 0, 30, 70, 90, 110, 190, 410, 0, 0 };
	static double path_gains[] = { 0, -1, -2, -3, -8, -17.2, -20.8, 0, 0 };
	
	{   
		double tx_corr_coeff = 0.0;
		double rx_corr_coeff = 0.0;

		switch (corr_type) {
		case "Low":
			tx_corr_coeff = 0;
			rx_corr_coeff = 0;
			break;

		case "Medium":
			tx_corr_coeff = 0.3;
			rx_corr_coeff = 0.9;
			break;

		default:
			tx_corr_coeff = 0.9;
			rx_corr_coeff = 0.9;

		}
		double dopp_freq = 6;
		
		
		/*
		 * int[] path_delays; double[] path_gains;
		 */
		if (channel_type.equals("EPA 5Hz")) {

			dopp_freq = dopp_freq - 1;// 5
			no_taps = 7;
		} else if (channel_type.equals("EVA 5Hz")) {
			path_delays[2] = 150;
			path_delays[3] = 310;
			path_delays[4] = 370;
			path_delays[5] = 710;
			path_delays[6] = 1090;
			path_delays[7] = 1730;
			path_delays[8] = 2510;

			path_gains[1] = -1.5;
			path_gains[2] = -1.4;
			path_gains[3] = -3.6;
			path_gains[4] = -0.6;
			path_gains[5] = -9.1;
			path_gains[6] = -7;
			path_gains[7] = -12;
			path_gains[8] = -16.9;
			dopp_freq = 5;
			no_taps = 9;
		} else if (channel_type.equals("EVA 70Hz")) {
			path_delays[2] = 150;
			path_delays[3] = 310;
			path_delays[4] = 370;
			path_delays[5] = 710;
			path_delays[6] = 1090;
			path_delays[7] = 1730;
			path_delays[8] = 2510;

			path_gains[1] = -1.5;
			path_gains[2] = -1.4;
			path_gains[3] = -3.6;
			path_gains[4] = -0.6;
			path_gains[5] = -9.1;
			path_gains[6] = -7;
			path_gains[7] = -12;
			path_gains[8] = -16.9;

			dopp_freq = 70;
			no_taps = 9;
		} else if (channel_type.equals("ETU 70Hz")) {
			path_delays[1] = 50;
			path_delays[2] = 120;
			path_delays[3] = 200;
			path_delays[4] = 230;
			path_delays[5] = 500;
			path_delays[6] = 1600;
			path_delays[7] = 2300;
			path_delays[8] = 5000;

			path_gains[0] = -1;
			path_gains[1] = -1;
			path_gains[2] = -1;
			path_gains[3] = 0;
			path_gains[4] = 0;
			path_gains[5] = 0;
			path_gains[6] = -3;
			path_gains[7] = -5;
			path_gains[8] = -7;
			dopp_freq = 70;
			no_taps = 9;
		} else if(channel_type.equals("ETU 300Hz")){// ETU 300Hz	
			path_delays[1] = 50;
			path_delays[2] = 120;
			path_delays[3] = 200;
			path_delays[4] = 230;
			path_delays[5] = 500;
			path_delays[6] = 1600;
			path_delays[7] = 2300;
			path_delays[8] = 5000;

			path_gains[0] = -1;
			path_gains[1] = -1;
			path_gains[2] = -1;
			path_gains[3] = 0;
			path_gains[4] = 0;
			path_gains[5] = 0;
			path_gains[6] = -3;
			path_gains[7] = -5;
			path_gains[8] = -7;
			dopp_freq = 300;
			no_taps = 9;
		}else{							
			path_delays[1] = 0;
			path_delays[2] = 0;
			path_delays[3] = 0;
			path_delays[4] = 0;
			path_delays[5] = 0;
			path_delays[6] = 0;
			path_delays[7] = 0;
			path_delays[8] = 0;

			path_gains[0] = 1;
			path_gains[1] = 0;
			path_gains[2] = 0;
			path_gains[3] = 0;
			path_gains[4] = 0;
			path_gains[5] = 0;
			path_gains[6] = 0;
			path_gains[7] = 0;
			path_gains[8] = 0;
			dopp_freq = 0;
			no_taps = 1;
		}
		// ////////////////////////////////////////

		double[][] valstx = { { 1., tx_corr_coeff }, { tx_corr_coeff, 1. } };
		double[][] valsrx = { { 1., rx_corr_coeff }, { rx_corr_coeff, 1. } };
		/*
		 * Matrix tx_corr_matrix = new Matrix(valstx); Matrix rx_corr_matrix =
		 * new Matrix(valsrx);
		 */
		double[][] corr_matrix_val = {
				{ valstx[0][0] * valsrx[0][0], valstx[0][0] * valsrx[0][1],
						valstx[0][1] * valsrx[0][0],
						valstx[0][1] * valsrx[0][1] },

				{ valstx[0][0] * valsrx[1][0], valstx[0][0] * valsrx[1][1],
						valstx[0][1] * valsrx[1][0],
						valstx[0][1] * valsrx[1][1] },

				{ valstx[1][0] * valsrx[0][0], valstx[1][0] * valsrx[0][1],
						valstx[1][1] * valsrx[0][0],
						valstx[1][1] * valsrx[0][1] },

				{ valstx[1][0] * valsrx[1][0], valstx[1][0] * valsrx[1][1],
						valstx[1][1] * valsrx[1][0],
						valstx[1][1] * valsrx[1][1] } };

		Matrix corr_matrix = new Matrix(corr_matrix_val);

		sqrt_corr_matrix = corr_matrix.sqrt();  
    	    
	}
    
	public newChannel(int length) {
		super(length*2,length*2);
		
	}

	@Override
	public void work() {
					
		double[][] tx1=new double[2][12];
		double[][] tx2=new double[2][12];
		
		for (int j = 0; j < 12; j++) {
			tx1[0][j]=pop();
			tx1[1][j]=pop();
		}
		
		for (int j = 0; j < 12; j++) {
			tx2[0][j]=pop();
			tx2[1][j]=pop();
		}
		
		
		int l = tx1[0].length;// number of sub carriers 12
		
		double[] f = new double[l * 2];
		for (int k = 0; k < l; ++k) {

			f[k] = fcarry - 59 * 15 * 0.000001 + 15 * 0.000001 * k;
			f[l + k] = fcarry - 59 * 15 * 0.000001 + 15 * 0.000001 * k;

		}
		
		Complex H_array[][] = new Complex[2 * l][2 * l];
		for (int i = 0; i < 2 * l; i++) {
			for (int j = 0; j < 2 * l; j++) {
				H_array[i][j] = Complex.valueOf(0.0, 0.0);
			}

		}

		Complex tr_1_coeff = Complex.valueOf(0.0, 0.0);
		Complex tr_2_coeff = Complex.valueOf(0.0, 0.0);
		Complex tr_1_calc = Complex.valueOf(0.0, 0.0);
		Complex tr_2_calc = Complex.valueOf(0.0, 0.0);
		Complex A_array[] = new Complex[4];
		Random ran = new Random();
		for (int i = 0; i < 4; i++) {
			A_array[i] = Complex.valueOf(ran.nextGaussian(), ran.nextGaussian());

		}
		
		Complex B_array[] = A_array;

		for (int i = 0; i < 4; ++i) {
			B_array[i] = (A_array[0].times(sqrt_corr_matrix.get(0, i)))
					.plus((A_array[1].times(sqrt_corr_matrix.get(1, i)))
							.plus((A_array[2].times(sqrt_corr_matrix.get(2, i)))
									.plus((A_array[3].times(sqrt_corr_matrix
											.get(3, i))))));
		}
		
		for (int k = 0; k < l; ++k) {

			
			// generate random numbers for A
			for (int i = 0; i < 4; i++) {
				A_array[i] = Complex.valueOf(ran.nextGaussian(),
						ran.nextGaussian());

			}
			
			for (int i = 0; i < 4; ++i) {
				B_array[i] = (A_array[0].times(sqrt_corr_matrix.get(0, i)))
						.plus(A_array[1].times(sqrt_corr_matrix.get(1, i)))
						.plus(A_array[2].times(sqrt_corr_matrix.get(2, i)))
						.plus(A_array[3].times(sqrt_corr_matrix.get(3, i)));
				// B_array[i]=A_array[0].times(sqrt_corr_matrix.get(0,
				// i)).plus(that)

			}
			// B = ComplexMatrix.valueOf(B_array);
			/*
			 * System.out.println("B_array : " + B_array[0].toString() + " , " +
			 * B_array[1].toString());
			 */
			tr_1_coeff = Complex.valueOf(1.0, 0.0);
			tr_2_coeff = Complex.valueOf(1.0, 0.0);
			// cout<<"EXP : "<<tr_1_coeff(0, 0)<<" : "<<exp(tr_1_coeff(0,
			// 0))<<endl;
			// for m =1:no_taps
			for (int m = 0; m < no_taps; ++m) {
				tr_1_calc = Complex.valueOf(0.0, 2.0 * Math.PI * f[k]
						* path_delays[m]);
				tr_2_calc = Complex.valueOf(0.0, 2.0 * Math.PI * f[k + 8]
						* path_delays[m]);

				tr_1_coeff = Complex.valueOf(10.0, 0).pow(path_gains[m]).sqrt()
						.plus(tr_1_calc.exp()).plus(tr_1_coeff);
				tr_2_coeff = Complex.valueOf(10.0, 0).pow(path_gains[m]).sqrt()
						.plus(tr_2_calc.exp()).plus(tr_2_coeff);
			}

			// 2 by 2 MIMO --> 4Paths
			// System.out.println("tr1 : " + tr_1_coeff.toString());

			H_array[k][k] = B_array[0].times(tr_1_coeff);
			H_array[k][k + l] = B_array[1].times(tr_2_coeff);
			H_array[k + l][k] = B_array[2].times(tr_1_coeff);
			H_array[k + l][k + l] = B_array[3].times(tr_2_coeff);

		}


		Complex noise_array[][] = new Complex[1][24];
		for (int j = 0; j < noise_array.length; j++) {
			noise_array[0][j] = Complex.valueOf(ran.nextGaussian(),
					ran.nextGaussian());// make it random
		}

		
		Complex Tx_array[][] = new Complex[24][1];

		tx1 = fft(tx1);
		tx2 = fft(tx2);

		for (int i = 0; i < 12; i++) {

			Tx_array[i][0] = Complex.valueOf(tx1[0][i], tx1[1][i]);
			Tx_array[i + 12][0] = Complex.valueOf(tx2[0][i], tx2[1][i]);

		}

		ComplexMatrix H = ComplexMatrix.valueOf(H_array);
//		System.out.println(H);
		ComplexMatrix Tx = ComplexMatrix.valueOf(Tx_array);
		// ComplexMatrix noise = ComplexMatrix.valueOf(noise_array);
		ComplexMatrix R = H.times(Tx); // yet to add noise for the data

	//	System.out.println(R);

		double RX[][] = new double[2][24];

		for (int i = 0; i < RX[0].length; i++) {
			RX[0][i] = R.get(i, 0).getReal();
			RX[1][i] = R.get(i, 0).getImaginary();
		}
		RX = ifft(RX);

		Complex H1_array[][] = new Complex[16][16];

		for (int y = 0; y < 8; ++y) {
			for (int z = 0; z < 8; ++z) {
				H1_array[y][z] = H.get(y + 2, z + 2);
			}
		}

		for (int y = 0; y < 8; ++y) {
			for (int z = 8; z < 16; ++z) {
				H1_array[y][z] = H.get(y + 2, z + 6);
			}
		}

		for (int y = 8; y < 16; ++y) {
			for (int z = 0; z < 8; ++z) {
				H1_array[y][z] = H.get(y + 6, z + 2);
			}
		}

		for (int y = 8; y < 16; ++y) {
			for (int z = 8; z < 16; ++z) {
				H1_array[y][z] = H.get(y + 6, z + 6);
			}
		}

		ComplexMatrix H1_mat = ComplexMatrix.valueOf(H1_array);
		DFTARRAY dft = new DFTARRAY();
		Complex[][] dftArray = dft.getDFT();
		ComplexMatrix DF = ComplexMatrix.valueOf(dftArray);
		H_out = H1_mat.times(DF);
		// System.out.println(H0);
		
		for (int i = 0; i < RX[0].length; i++) {
			push((float)RX[0][i]);
			push((float)RX[1][i]);
		}
		
		
	}
	
	public ComplexMatrix getHout() {
		return H_out;
	}

	private double[][] fft(double[][] tx) {
		double in[] = new double[24];

		for (int i = 0; i < tx[0].length; ++i) {
			in[2 * i] = tx[0][i];
			in[2 * i + 1] = tx[1][i];

		}

		DoubleFFT_1D fftDo = new DoubleFFT_1D(12);

		fftDo.complexForward(in);
		int count = 0;
		for (int j = 0; j < 12; ++j) {
			tx[0][count] = in[2 * j];
			tx[1][count] = in[2 * j + 1];
			++count;
		}

		return tx;
	}

	private double[][] ifft(double[][] rx) {
		double in1[] = new double[24];
		double in2[] = new double[24];
		for (int i = 0; i < rx[0].length / 2; ++i) {
			in1[2 * i] = rx[0][i];
			in1[2 * i + 1] = rx[1][i];
			
			in2[2 * i] = rx[0][i + 12];
			in2[2 * i + 1] = rx[1][i + 12];
		}

		DoubleFFT_1D fftDo = new DoubleFFT_1D(12);

		fftDo.complexInverse(in1, true);
		fftDo.complexInverse(in2, true);
		int count = 0;
		for (int j = 0; j < 12; ++j) {
			rx[0][count] = in1[2 * j];
			rx[1][count] = in1[2 * j + 1];
			rx[0][count + 12] = in2[2 * j];
			rx[1][count + 12] = in2[2 * j + 1];
			++count;
		}

		return rx;
	}
}

