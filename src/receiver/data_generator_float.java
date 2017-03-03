package receiver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class data_generator_float {

	public static void main(String[] args) {
		int data_length = 528;

		FileOutputStream out = null;
		FileInputStream in = null;
		try {
			out = new FileOutputStream("src/edu/mit/streamjit/receiver/data_float.in");
			in = new FileInputStream("src/edu/mit/streamjit/receiver/data_float.in");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedOutputStream buffout = new BufferedOutputStream(out, 4);
		BufferedInputStream buffin = new BufferedInputStream(in, 4);

		float[] data = new float[data_length];

		for (int i = 0; i < data_length; i++) {
			if (i == 0)
				data[i] = 2f;
			else if (i == 1)
				data[i] = 0f;
			else if (i == 2)
				data[i] = 1f;
			else if (i == 3)
				data[i] = 0f;
			else
				data[i] = 0f;
			try {
				buffout.write(ByteBuffer.allocate(4).putFloat(1f).array());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < data_length; i++) {

			try {
				byte[] b = new byte[4];
				buffin.read(b);
				System.out.println(ByteBuffer.wrap(b).getFloat());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
