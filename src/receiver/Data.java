package receiver;

public class Data {
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
