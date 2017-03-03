package transmitter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class data_generator {

	public static void main(String[] args) {
		int data_length = 320;

		// DataOutputStream out=null;
		DataInputStream in = null;
		try {
			// out = new DataOutputStream(new
			// FileOutputStream("src/transmitter/data.in"));
			in = new DataInputStream(new FileInputStream("src/transmitter/data.in"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Random r = new Random();

		for (int i = 0; i < data_length; i++) {
			int l;
			double temp = r.nextDouble();
			if (temp > 0.5)
				l = 1;
			else
				l = 0;

			try {
				// out.writeByte(l);;
				// System.out.print(l+" ");
				byte val = in.readByte();
				System.out.println(val);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			in.close();
			// out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
