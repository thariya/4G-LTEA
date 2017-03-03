package transmitter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class data_write_file {

	public static void main(String[] args) {

		for (int i = 100; i < 10001; i = i * 10) {
			for (int j = 0; j < 5; j++) {
				System.out.println(i + " " + j);
				int data_length = i * 320;

				FileOutputStream out = null;

				try {
					out = new FileOutputStream("src/edu/mit/streamjit/transmitter/data_" + i + "_" + j + ".in");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				for (int k = 0; k < data_length; k++) {
					byte l;
					if (Math.random() > 0.5)
						l = 1;
					else
						l = 0;

					try {
						out.write(l);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
