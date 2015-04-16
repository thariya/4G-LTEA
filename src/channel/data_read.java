package channel;


import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class data_read {
	
	
	public static void main(String args[]){
		int data_length=528;
	
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream("src/edu/mit/streamjit/channel/transmitter_out.out"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < data_length; i++) {	
			float f = 0;
			try {
				f = in.readFloat();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(f);
		}
	}
	
	
}
