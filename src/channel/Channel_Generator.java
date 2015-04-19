package channel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;
import org.jtransforms.fft.DoubleFFT_1D;

import weka.core.matrix.Matrix;

public class Channel_Generator {

	public static void main(String[] args) {
		String channel_type="EPA 5Hz";
		String corr_type="Low";
		int l=12;
		double fcarry=2;
		double Eb_No = 10;
		double lc = (20.0 / 33.0) * Math.pow(10, 0.1 * Eb_No);
		double sigma = Math.sqrt(3.5 / Math.pow(10, 0.1 * Eb_No));
		ComplexMatrix H_out;
		ComplexMatrix H;
		ComplexMatrix Hinv;
		ComplexMatrix Hhat;
		
		int length=16;
		Matrix sqrt_corr_matrix;
		double no_taps = 0;
		int path_delays[] = { 0, 30, 70, 90, 110, 190, 410, 0, 0 };
		double path_gains[] = { 0, -1, -2, -3, -8, -17.2, -20.8, 0, 0 };
		
		String[] corrs={"Low","Medium","High"};
		String[] types={"EPA 5Hz","EVA 5Hz","EVA 70Hz","ETU 70Hz","ETU 300Hz"};
		
		for (int p = 0; p < types.length; p++) {
			for (int q = 0; q < corrs.length; q++) {
				channel_type=types[p];
				corr_type=corrs[q];
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
				
				H = ComplexMatrix.valueOf(H_array);
//				System.out.println(H);
				

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
				
				Hinv=H.inverse();
				
				Complex[][] H_temp=new Complex[24][24];
				for (int j = 0; j < 24; j++) {
					for (int k = 0; k < 24; k++) {
						H_temp[j][k]= Complex.valueOf(H.get(j, k).getReal(), (-1)*H.get(j, k).getImaginary());  //(i, j)=Complex ;// H.get(i, j).getImaginary();
					}
				}
				Hhat=ComplexMatrix.valueOf(H_temp);
				Hhat=Hhat.transpose();
				
				///////////////////////////////////////////////////////////
				Complex Pd_array[][] = new Complex[24][24];

				for (int i = 0; i < 24; i++) {

					for (int j = 0; j < 24; j++) {
						if(i==j){
							Pd_array[i][i] = Complex.valueOf(42.0, 0.0);
						if (i == 0 || i == 1 || i == 10 || i == 11 || i == 12
								|| i == 13 || i == 22 || i == 23) {
							Pd_array[i][j] = Complex.valueOf(0.0, 0.0);
						} 
						}else{
							Pd_array[i][j] = Complex.valueOf(0.0, 0.0);
							
						}
					}
				}
				ComplexMatrix Pd = ComplexMatrix.valueOf(Pd_array);
				
				
				Complex sigma_mat_array[][] = new Complex[24][24];
				for (int i = 0; i < 24; i++) {

					for (int j = 0; j < 24; j++) {
						if(i==j){
							sigma_mat_array[i][i] = Complex.valueOf(sigma * sigma, 0.0);
						
						}else{
							sigma_mat_array[i][j] = Complex.valueOf(0.0, 0.0);
							
						}
					}
				}
				ComplexMatrix sigma_mat=ComplexMatrix.valueOf(sigma_mat_array);
				//////////////////////////////////////////////////////////////
				Complex C_her_array[][] = new Complex[24][24];
				ComplexMatrix C;
				ComplexMatrix C_her;
				C=Pd.times(H).times(Hhat).plus(sigma_mat).inverse().times(Pd).times(Hhat);
//				System.out.println(C);
				for (int j = 0; j < 24; j++) {
					for (int k = 0; k < 24; k++) {
						C_her_array[j][k]= Complex.valueOf(C.get(j, k).getReal(), (-1)*C.get(j, k).getImaginary());  //(i, j)=Complex ;// H.get(i, j).getImaginary();
					}
				}
				C_her=ComplexMatrix.valueOf(C_her_array);
				C_her=C_her.transpose();
				
//				Complex C_her_array1[][] = new Complex[24][24];
//				ComplexMatrix C1;
//				ComplexMatrix C_her1;
//				C1=Pd.times(H).times(Hhat).inverse().times(Pd).times(Hhat);
////				System.out.println(C1);
//				for (int j = 0; j < 24; j++) {
//					for (int k = 0; k < 24; k++) {
//						C_her_array1[j][k]= Complex.valueOf(C1.get(j, k).getReal(), (-1)*C1.get(j, k).getImaginary());  //(i, j)=Complex ;// H.get(i, j).getImaginary();
////						System.out.println(C_her_array1[j][k]);
//					}
//				}
//				C_her1=ComplexMatrix.valueOf(C_her_array1);
//				C_her1=C_her1.transpose();
//				System.out.println(C_her1);
				
				
				save(H,"params/channel matrices/"+channel_type+"-"+corr_type+"-H");
				save(H_out,"params/channel matrices/"+channel_type+"-"+corr_type+"-Hout");
				save(Hinv,"params/channel matrices/"+channel_type+"-"+corr_type+"-Hinv");
				save(C_her,"params/channel matrices/"+channel_type+"-"+corr_type+"-HLMMSE");
//				save(C_her1,"params/channel matrices/"+channel_type+"-"+corr_type+"-HLMMSEzero");
			}
		}
			
			
			
			
	}
	
	private static void save(ComplexMatrix A,String s){
		double[][] rel=new double[ A.getNumberOfRows()][A.getNumberOfColumns()];
		double[][] img=new double[ A.getNumberOfRows()][A.getNumberOfColumns()]; 
		for (int i = 0; i < A.getNumberOfRows(); i++) {
			for (int j = 0; j < A.getNumberOfColumns(); j++) {
				rel[i][j]=A.get(i, j).getReal();
				img[i][j]=A.get(i, j).getImaginary();
			}
		}
		
		try
	      {
		     FileOutputStream fileOut1 = new FileOutputStream(s+"real.ser");
		     FileOutputStream fileOut2 = new FileOutputStream(s+"img.ser");
		     ObjectOutputStream out1 = new ObjectOutputStream(fileOut1);
		     out1.writeObject(rel);
		     ObjectOutputStream out2 = new ObjectOutputStream(fileOut2);
		     out2.writeObject(img);
		     out1.close();
		     out2.close();
		     fileOut1.close();
		     fileOut2.close();
		  }catch(IOException i){
		      i.printStackTrace();
		  }

		
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

class DFTARRAY {

	Complex[][] DF;

	DFTARRAY() {
		DF = new Complex[16][16];
		Complex val = Complex.valueOf(0, 0);
		for (int i = 0; i < DF.length / 2; i++) {
			for (int j = 0; j < DF.length / 2; j++) {
				val = Complex.valueOf(0, -1 * (2 * Math.PI * (i) * (j) / 8))
						.exp();
				if (Math.abs(val.getReal()) < Math.pow(10, -10)) {
					val = Complex.valueOf(0, val.getImaginary());
				}
				if (Math.abs(val.getImaginary()) < Math.pow(10, -10)) {
					val = Complex.valueOf(val.getReal(), 0);
				}

				DF[i][j] = val;

				DF[i + 8][j + 8] = DF[i][j];
				DF[i][j + 8] = Complex.valueOf(0, 0);
				DF[i + 8][j] = Complex.valueOf(0, 0);
			}
		}

	}

	public Complex[][] getDFT() {
		return DF;
	}
	
	public void printDFT() {
		System.out.println("DFT Matrix :");
		for (int i = 0; i < DF.length; i++) {
			for (int j = 0; j < DF.length; j++) {
				System.out.print(DF[i][j] + " \t ");
			}
			System.out.println();
		}

	}
	
}

