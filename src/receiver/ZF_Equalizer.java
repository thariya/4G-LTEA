package receiver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.jtransforms.fft.DoubleFFT_1D;

public class ZF_Equalizer extends edu.mit.streamjit.api.Pipeline<Float,Float> {
	public ZF_Equalizer() {
		this.add(new ZF_Init(),new ZF_Invert(), new ZF_Revert(),new ZF_Estimate());
	}

		
	private static class ZF_Init extends edu.mit.streamjit.api.Filter<Float, Float> {

		public ZF_Init() {
			super(48, 48);
		}

		@Override
		public void work() {
			float[][] tx1=new float[2][12];
			float[][] tx2=new float[2][12];
			
			Float[][] Tx_array=new Float[24][2];
			for (int j = 0; j < 12; j++) {
				tx1[0][j]=pop();
				tx1[1][j]=pop();
			}
			
			for (int j = 0; j < 12; j++) {
				tx2[0][j]=pop();
				tx2[1][j]=pop();
			}		
//			printArray(tx1);		
			
			tx1 = fft(tx1);
			tx2 = fft(tx2);

			for (int i = 0; i < 12; i++) {
				Tx_array[i][0] =tx1[0][i];
				Tx_array[i][1] =tx1[1][i];
				Tx_array[i+12][0] =tx2[0][i];
				Tx_array[i+12][1] =tx2[1][i];		

			}
			
			for (int i = 0; i < 24; i++) {
				push(Tx_array[i][0]); 	
				push(Tx_array[i][1]); 	
			}			
			
		}
	}
	
	private static class ZF_Invert extends edu.mit.streamjit.api.Filter<Float, Float> {

		public ZF_Invert() {
			super(48,32);
		}

		@Override
		public void work() {
			double H_array[][][]=setup();						
			Float[][] Tx_array=new Float[24][2];
			
			for (int i = 0; i < 24; i++) {
				Tx_array[i][0]=pop(); 	
				Tx_array[i][1]=pop(); 	
			}	
			
			for (int i = 2; i < 10; i++) {
				double real=0;
				double img=0;
				for (int j = 0; j <24; j++) {
					real+=(H_array[0][i][j]*Tx_array[j][0]);
				}
				for (int j = 0; j <24; j++) {
					real-=(H_array[1][i][j]*Tx_array[j][1]);
				}
								
				for (int j = 0; j <24; j++) {
					img+=(H_array[0][i][j]*Tx_array[j][1]);
				}
				for (int j = 0; j <24; j++) {
					img+=(H_array[1][i][j]*Tx_array[j][0]);
				}
				push((float)real);
				push((float)img);
	
			}
			
			for (int i = 14; i < 22; i++) {
				double real=0;
				double img=0;
				for (int j = 0; j <24; j++) {
					real+=(H_array[0][i][j]*Tx_array[j][0]);
				}
				for (int j = 0; j <24; j++) {
					real-=(H_array[1][i][j]*Tx_array[j][1]);
				}
								
				for (int j = 0; j <24; j++) {
					img+=(H_array[0][i][j]*Tx_array[j][1]);
				}
				for (int j = 0; j <24; j++) {
					img+=(H_array[1][i][j]*Tx_array[j][0]);
				}
								
				push((float)real);
				push((float)img);
			}
			
			
		}
	}
	
	private static class ZF_Revert extends edu.mit.streamjit.api.Filter<Float, Float> {

		public ZF_Revert() {
			super(32, 32);
		}

		@Override
		public void work() {
			Float[][] R=new Float[2][16];			
			for (int i = 0; i < 16; i++) {
				R[0][i]=pop();
				R[1][i]=pop();
			}
						
//			printArray(R);
			R=ifft(R);		
			
			for (int i = 0; i <16; i++) {
				push(R[0][i]);
				push(R[1][i]);
			}
		}
	}
	
	private static class ZF_Estimate extends edu.mit.streamjit.api.Filter<Float, Float> {

		public ZF_Estimate() {
			super(1, 1);
		}

		@Override
		public void work() {
			
			float a=pop();
//			System.out.println(a);
			float l=(float)Math.floor(a);
			float h=(float)Math.ceil(a);
			if(l<=-7)	push(-7.0f);
			else if(h>=7)	push(7.0f);
			else{
				if(l%2==1)	push(l);
				else push(h);				
			}
			
		}
	}
	
	
	private static float[][] fft(float[][] tx) {
		double in[] = new double[24];
		DoubleFFT_1D fftDo = new DoubleFFT_1D(12);
		for (int i = 0; i < tx[0].length; ++i) {
			in[2 * i] = tx[0][i];
			in[2 * i + 1] = tx[1][i];

		}

		

		fftDo.complexForward(in);
		int count = 0;
		for (int j = 0; j < 12; ++j) {
			tx[0][count] = (float)in[2 * j];
			tx[1][count] = (float)in[2 * j + 1];
			++count;
		}

		return tx;
	}

	private static Float[][] ifft(Float[][] rx) {
		double in1[] = new double[16];
		double in2[] = new double[16];
		for (int i = 0; i <8; ++i) {
			in1[2 * i] = rx[0][i];
			in1[2 * i + 1] = rx[1][i];
			
			in2[2 * i] = rx[0][i + 8];
			in2[2 * i + 1] = rx[1][i + 8];
		}

		DoubleFFT_1D fftDo = new DoubleFFT_1D(8);

		fftDo.complexInverse(in1, true);
		fftDo.complexInverse(in2, true);
		int count = 0;
		for (int j = 0; j <8; ++j) {
			rx[0][count] = (float)in1[2 * j];
			rx[1][count] = (float)in1[2 * j + 1];
			rx[0][count + 8] = (float)in2[2 * j];
			rx[1][count + 8] = (float)in2[2 * j + 1];
			++count;
		}
		
		return rx;
	}
	
	
	private static double[][][] setup(){
		
		double[][][] H_array=new double[2][24][24];
		double[][] Hreal = null;
		double[][] Himg = null;
		try
	      {
	         FileInputStream fileIn = new FileInputStream("params/channel matrices/EPA 5Hz-Low-HLMMSEreal.ser");
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
	         FileInputStream fileIn = new FileInputStream("params/channel matrices/EPA 5Hz-Low-HLMMSEimg.ser");
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
			
//			for (int i = 0; i < 24; i++) {
//				for (int j = 0; j < 24; j++) {
//					System.out.print(Hreal[i][j]+" ");
//				}
//				System.out.println();
//			}
//			System.out.println("######################");
//			for (int i = 0; i <24; i++) {
//				for (int j = 0; j < 24; j++) {
//					System.out.print(Himg[i][j]+" ");
//				}
//				System.out.println();
//			}
//			System.out.println("######################");
			
			return H_array;
	}
	
	private static void printArray(Float[][] d){
		for (int i = 0; i < d[0].length; i++) {
			for (int j = 0; j < 2; j++) {
				System.out.print(d[j][i]+" ");
			}
			System.out.println();
		}
		System.out.println("######################");
	}
	
	private static void printArray(float[][] d){
		for (int i = 0; i < d[0].length; i++) {
			for (int j = 0; j < 2; j++) {
				System.out.print(d[j][i]+" ");
			}
			System.out.println();
		}
		System.out.println("######################");
	}
}
