import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Test {
	public static void main(String[] args){
		double[][] H=null;
		try
		{
			FileInputStream fileIn = new FileInputStream("EPA 5Hz-Low-Houtreal.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			H = (double[][]) in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException i)
		{

			i.printStackTrace();	         
		}catch(ClassNotFoundException c)
		{
			c.printStackTrace();	         
		}	
		System.out.print("{");
		for (int i = 0; i < 16; i++) {
			System.out.print("{");
			for (int j = 0; j < H[0].length; j++) {
				System.out.print(H[i][j]);
				if(j!=H[0].length-1)System.out.print(",");
				else;
			}
			System.out.print("}");
			if(i!=15)System.out.print(",");
		}
		System.out.print("}");
	}
	
}
