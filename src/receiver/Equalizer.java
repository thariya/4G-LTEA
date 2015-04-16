package receiver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.jscience.mathematics.vector.ComplexMatrix;

import weka.core.matrix.Matrix;
import weka.core.matrix.QRDecomposition;

public class Equalizer extends edu.mit.streamjit.api.Pipeline<Float, Float> {
	
	public Equalizer(){
		this.add(new EqualizerPreprocess());	
		for (int i = 0; i < 31; i++) {
			this.add(new TreeTraversal(i));
		}
		this.add(new EqualizerTerminal());
	}
	
//	@Override
//	public void work() {
//		double s=System.nanoTime();
//		double[][] y=new double[32][1];
//		for (int i = 0; i <32; i++) {
//			y[i][0]=pop();
//		}
//		Matrix Y = Matrix.constructWithCopy(y);
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
//		Matrix[] a=QRD(H);
//		
//		double temp[] = new double[32];
//		
//		Y=a[0].transpose().times(Y);
//
//		LSDTree tree=new LSDTree(1000000,32);
//		tree.setRMatrix(a[1]);
//		tree.generateFirstlevel(Y.get(31, 0));
//		double e=System.nanoTime();
//		System.out.println("Time: "+(e-s));
//		GetLSD_Y(tree,Y);
//		
//		Node minNode=tree.getMinnode();
//		
//		for (int i = 0; i < 32; i++) {		
//			temp[i]=minNode.getNode_S();
//			minNode=minNode.getparent();
//		}
//		
//		for (int i = 0; i <8; i++) {
//			push((float)temp[i]);
//			push((float)temp[i+16]);
//		}
//		
//		for (int i = 0; i <8; i++) {
//			push((float)temp[i+8]);
//			push((float)temp[i+24]);
//		}
//				
//	}
	
//	void genH(ComplexMatrix H1){
//		
//		double h_temp[][]=new double[32][32];
//		for (int i = 0; i < 16; i++) {
//			for (int j = 0; j < 16; j++) {
//				
//				h_temp[i][j]=H1.get(i, j).getReal();
//				h_temp[i+16][j+16]=h_temp[i][j];
//				h_temp[i+16][j]=H1.get(i, j).getImaginary();
//				h_temp[i][j+16]=(-1)*h_temp[i+16][j];
//			}
//		}
//		
//		H=Matrix.constructWithCopy(h_temp);	
//	}
	
	static Matrix setup(){		
		
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
		Matrix H=Matrix.constructWithCopy(h_temp);
		return H;
	}
	
	static Matrix[] QRD(Matrix Hcopy){
		
		QRDecomposition QRD = new QRDecomposition(Hcopy);
		Matrix Q=QRD.getQ();
		Matrix R=QRD.getR();
		Matrix[] a={Q,R};
		return a;
		
	}
	
	static void GetLSD_Y(LSDTree tree,Matrix Y){		

		for (int i = 0; i < 31; i++) {
			tree.generateNextlevel(Y.get(31-(i+1), 0),i+1);
		}	
	}
	
	
	private static class EqualizerPreprocess extends edu.mit.streamjit.api.Filter<Float,Level> {

		public EqualizerPreprocess() {
			super(32, 1);
		}

		@Override
		public void work() {
			Matrix H=setup();
			double[][] y=new double[32][1];
			for (int i = 0; i <32; i++) {
				y[i][0]=pop();
			}
			Matrix Y = Matrix.constructWithCopy(y);
//			System.out.println("##############H MAtrix################");
//			System.out.println(H);
//			System.out.println("##############################");
//			System.out.println("##############Y MAtrix################");
//			System.out.println(Y);
//			System.out.println("##############################");
//			double[] y_out = GetLSD_Y(Y);
//			for (int i = 0; i < 32; i++) {
//				push((float)y_out[i]);
//			}
			Matrix[] a=QRD(H);
			
			Y=a[0].transpose().times(Y);

			LSDTree tree=new LSDTree(1000000,32);
			tree.setRMatrix(a[1]);
			tree.generateFirstlevel(Y.get(31, 0));
			push(new Level(tree,Y));
		}
	}
	
	private static class TreeTraversal extends edu.mit.streamjit.api.Filter<Level,Level> {
		int i;
		public TreeTraversal(int x) {
			super(1, 1);
			i=x;
		}

		@Override
		public void work() {
			Level l=pop();
			l.t.generateNextlevel(l.Y.get(31-(i+1), 0),i+1);
			push(l);
		}
	}
	
	private static class EqualizerTerminal extends edu.mit.streamjit.api.Filter<Level,Float> {
		
		public EqualizerTerminal() {
			super(1, 32);			
		}

		@Override
		public void work() {
			Level l=pop();
			double temp[] = new double[32];
			Node minNode=l.t.getMinnode();
			
			for (int i = 0; i < 32; i++) {		
				temp[i]=minNode.getNode_S();
				minNode=minNode.getparent();
			}
			
			for (int i = 0; i <8; i++) {
				push((float)temp[i]);
				push((float)temp[i+16]);
			}
			
			for (int i = 0; i <8; i++) {
				push((float)temp[i+8]);
				push((float)temp[i+24]);
			}
		}
	}
	
	private static class Level {
		LSDTree t;
		Matrix Y;
		public Level(LSDTree tree, Matrix x) {
			t=tree;
			Y=x;
		}

		
	}
		
	
}
