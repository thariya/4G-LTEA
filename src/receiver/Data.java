package receiver;

import java.io.Serializable;

public class Data implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8112877623525415460L;

	public byte[] y;
	public float[][] a;

	public Data(byte[] b, float[][] f) {
		y = b;
		a = f;
	}

	public void seta(float[][] f) {
		this.a = f;
	}

	public void sety(byte[] b) {
		this.y = b;
	}

}
