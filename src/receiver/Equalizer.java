package receiver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.jscience.mathematics.vector.ComplexMatrix;

import weka.core.matrix.Matrix;
import weka.core.matrix.QRDecomposition;

public class Equalizer extends edu.mit.streamjit.api.Filter<Float, Float> {
	
	Matrix H;
		
	double y[][];
	Equalizer(){
		super(32,32);		
//		double s=System.nanoTime();
		setup();
//		double e=System.nanoTime();
//		System.out.println("setup timesss: "+(e-s));
	}
	
	@Override
	public void work() {
		y=new double[32][1];
		for (int i = 0; i <32; i++) {
			y[i][0]=pop();
		}
		Matrix Y = Matrix.constructWithCopy(y);
//		System.out.println("##############H MAtrix################");
//		System.out.println(H);
//		System.out.println("##############################");
//		System.out.println("##############Y MAtrix################");
//		System.out.println(Y);
//		System.out.println("##############################");
//		double[] y_out = GetLSD_Y(Y);
//		for (int i = 0; i < 32; i++) {
//			push((float)y_out[i]);
//		}
		double[] y_out = GetLSD_Y(Y,H);
		for (int i = 0; i <8; i++) {
			push((float)y_out[i]);
			push((float)y_out[i+16]);
		}
		
		for (int i = 0; i <8; i++) {
			push((float)y_out[i+8]);
			push((float)y_out[i+24]);
		}
				
	}
	
	void genH(ComplexMatrix H1){
		
		double h_temp[][]=new double[32][32];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				
				h_temp[i][j]=H1.get(i, j).getReal();
				h_temp[i+16][j+16]=h_temp[i][j];
				h_temp[i+16][j]=H1.get(i, j).getImaginary();
				h_temp[i][j+16]=(-1)*h_temp[i+16][j];
			}
		}
		
		H=Matrix.constructWithCopy(h_temp);	
	}
	
	void setup(){		
		double[][] Houtreal = null;
		double[][] Houtimg = null;
		try
		{
			FileInputStream fileIn = new FileInputStream("channel_matrices_Houtreal.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Houtreal = (double[][]) in.readObject();
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
			FileInputStream fileIn = new FileInputStream("channel_matrices_Houtimg.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Houtimg = (double[][]) in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException i)
		{
			i.printStackTrace();	         
		}catch(ClassNotFoundException c)
		{
			c.printStackTrace();	         
		}

		double h_temp[][]=new double[32][32];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				h_temp[i][j]=Houtreal[i][j];
				h_temp[i+16][j+16]=h_temp[i][j];
				h_temp[i+16][j]=Houtimg[i][j];
				h_temp[i][j+16]=(-1)*h_temp[i+16][j];

			}
		}
		H=Matrix.constructWithCopy(h_temp);
	}
	
	
	static double[] GetLSD_Y(Matrix Y, Matrix Hcopy){
		double y[] = new double[32];
//		double s=System.nanoTime();
		QRDecomposition QRD = new QRDecomposition(Hcopy);
//		double e=System.nanoTime();
//		System.out.println("setup timesss: "+(e-s));
	    
		Matrix Q=QRD.getQ();
		Matrix R=QRD.getR();
		Y=Q.transpose().times(Y);

		LSDTree tree=new LSDTree(1000000,32);
		tree.setRMatrix(R);
		tree.generateFirstlevel(Y.get(31, 0));

		for (int i = 0; i < 31; i++) {
			tree.generateNextlevel(Y.get(31-(i+1), 0),i+1);
		}
		
		Node minNode=tree.getMinnode();
		
		for (int i = 0; i < 32; i++) {		
			y[i]=minNode.getNode_S();
			minNode=minNode.getparent();
		}		
		
		return y;
	}
	
		
	
}
