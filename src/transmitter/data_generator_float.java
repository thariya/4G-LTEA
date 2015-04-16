package transmitter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class data_generator_float {

	public static void main(String[] args) {
		int data_length=24;
		
		
		FileOutputStream out=null;
		FileInputStream in=null;
		try {
			out = new FileOutputStream("data/fft_data.in");
			in = new FileInputStream("data/fft_data.in");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedOutputStream buffout=new BufferedOutputStream(out, 4);
		BufferedInputStream buffin=new BufferedInputStream(in, 4);
		
		float[] data={1f,1f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f};
		
		for (int i = 0; i < data_length; i++) {
			
		try {
			buffout.write(ByteBuffer.allocate(4).putFloat(data[i]).array());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
		
		
//		for (int i = 0; i < data_length; i++) {
//			if(i==0)	data[i]=2f;
//			else if(i==1)	data[i]=0f;
//			else if(i==2)	data[i]=1f;
//			else if(i==3)	data[i]=0f;
//			else	data[i]=0f;
//			try {
//				buffout.write(ByteBuffer.allocate(4).putFloat(data[i]).array());
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
//		for (int i = 0; i < data_length; i++) {
//			
//			try {
//				byte[] b=new byte[4];
//				buffin.read(b);
//				System.out.println(ByteBuffer.wrap(b).getFloat());
//			
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
		
	}

}
