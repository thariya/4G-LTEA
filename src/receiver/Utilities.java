package receiver;

public class Utilities {

	public static float max_e(float a, float b) {
		if (a == 0f)
			return b;
		else if (b == 0f)
			return a;
		else
			return (float) (Math.max(a, b) + Math.log(1 + Math.exp(-1.0 * Math.abs(b - a))));

	}

	public static float max_e(float[] a) {
		float temp = max_e(a[0], a[1]);

		for (int i = 0; i < a.length - 2; i++) {
			temp = max_e(temp, a[i + 2]);
		}
		return temp;
	}
}
