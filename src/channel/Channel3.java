package channel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

import org.jtransforms.fft.DoubleFFT_1D;

public class Channel3 extends
edu.mit.streamjit.api.Filter<Float, Float>{
	
	public Channel3() {
		super(48,48);		
		
	}

	@Override
	public void work() {
		double H_array[][][]=new double[2][24][24];
		double[][] Hreal = null;
		double[][] Himg = null;
		try
	      {
	         FileInputStream fileIn = new FileInputStream("channel_matrices_Hreal.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         Hreal = (double[][]) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();	         
	      }catch(ClassNotFoundException c)
	      {
	         c.printStackTrace();	         
	      }
		
		try
	      {
	         FileInputStream fileIn = new FileInputStream("channel_matrices_Himg.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         Himg = (double[][]) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();	         
	      }catch(ClassNotFoundException c)
	      {
	         c.printStackTrace();	         
	      }
		
		H_array[0]=Hreal;
		H_array[1]=Himg;	
		double[][] tx1=new double[2][12];
		double[][] tx2=new double[2][12];
		double[][] noise_array= new double[24][2];
		double[][] Tx_array=new double[24][2];
		for (int j = 0; j < 12; j++) {
			tx1[0][j]=pop();
			tx1[1][j]=pop();
		}
		
		for (int j = 0; j < 12; j++) {
			tx2[0][j]=pop();
			tx2[1][j]=pop();
		}		
		
		Random ran = new Random();
		double sigma=4.0;	
		
		for (int j = 0; j < 24; j++) {
			noise_array[j][0] = sigma*sigma*ran.nextGaussian();
			noise_array[j][1] = sigma*sigma*ran.nextGaussian();
		}
		
		tx1 = fft(tx1);
		tx2 = fft(tx2);

		for (int i = 0; i < 12; i++) {
			Tx_array[i][0] =tx1[0][i];
			Tx_array[i][1] =tx1[1][i];
			Tx_array[i+12][0] =tx2[0][i];
			Tx_array[i+12][1] =tx2[1][i];		

		}
		double[][] R=new double[2][24];
		
		for (int i = 0; i < 24; i++) {
			double real=0;
			double img=0;
			for (int j = 0; j <24; j++) {
				real+=(H_array[0][i][j]*Tx_array[j][0]);
			}
			for (int j = 0; j <24; j++) {
				real-=(H_array[1][i][j]*Tx_array[j][1]);
			}
			real+=noise_array[i][0];
			
			for (int j = 0; j <24; j++) {
				img+=(H_array[0][i][j]*Tx_array[j][1]);
			}
			for (int j = 0; j <24; j++) {
				img+=(H_array[1][i][j]*Tx_array[j][0]);
			}
			img+=noise_array[i][1];
			
			R[0][i]=real;
			R[1][i]=img;
		}		
		
		R = ifft(R);
		
		for (int i = 0; i < R[0].length; i++) {
			push((float)R[0][i]);
			push((float)R[1][i]);
		}
		
		
	}
	
	private static double[][][] setup(double[][][] H_array){
//		double H_array[][][]=new double[2][24][24];
		double[][] Hreal = null;
		double[][] Himg = null;
		try
	      {
	         FileInputStream fileIn = new FileInputStream("channel_matrices_Hreal.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         Hreal = (double[][]) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();	         
	      }catch(ClassNotFoundException c)
	      {
	         c.printStackTrace();	         
	      }
		
		try
	      {
	         FileInputStream fileIn = new FileInputStream("channel_matrices_Himg.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         Himg = (double[][]) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();	         
	      }catch(ClassNotFoundException c)
	      {
	         c.printStackTrace();	         
	      }
		
		H_array[0]=Hreal;
		H_array[1]=Himg;	
		return H_array;
	}

	private static double[][] fft(double[][] tx) {
		double in[] = new double[24];
		DoubleFFT_1D fftDo = new DoubleFFT_1D(12);
		for (int i = 0; i < tx[0].length; ++i) {
			in[2 * i] = tx[0][i];
			in[2 * i + 1] = tx[1][i];

		}

		

		fftDo.complexForward(in);
		int count = 0;
		for (int j = 0; j < 12; ++j) {
			tx[0][count] = in[2 * j];
			tx[1][count] = in[2 * j + 1];
			++count;
		}

		return tx;
	}

	private static double[][] ifft(double[][] rx) {
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




