package channel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class T {

	public static void main(String[] args) {
		M mat = null;
		try {
			FileInputStream fileIn = new FileInputStream("src/edu/mit/streamjit/channel/channel_matrices.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			mat = (M) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
