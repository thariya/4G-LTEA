package receiver;

public class Demodulator extends edu.mit.streamjit.api.Filter<Float, Byte> {
	public Demodulator() {
		super(2, 6);
	}

	@Override
	public void work() {
		float[] real = { -7, -7, -7, -7, -7, -7, -7, -7, -5, -5, -5, -5, -5, -5, -5, -5, -1, -1, -1, -1, -1, -1, -1, -1,
				-3, -3, -3, -3, -3, -3, -3, -3, 7, 7, 7, 7, 7, 7, 7, 7, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 1,
				3, 3, 3, 3, 3, 3, 3, 3 };
		float[] complex = { -7, -5, -1, -3, 7, 5, 1, 3, -7, -5, -1, -3, 7, 5, 1, 3, -7, -5, -1, -3, 7, 5, 1, 3, -7, -5,
				-1, -3, 7, 5, 1, 3, -7, -5, -1, -3, 7, 5, 1, 3, -7, -5, -1, -3, 7, 5, 1, 3, -7, -5, -1, -3, 7, 5, 1, 3,
				-7, -5, -1, -3, 7, 5, 1, 3 };

		int rel = Math.round(pop());
		int img = Math.round(pop());
		int start = 0;
		int within = 0;
		switch (rel) {
		case -5:
			start = 8;
			break;
		case -1:
			start = 16;
			break;
		case -3:
			start = 24;
			break;
		case 7:
			start = 32;
			break;
		case 5:
			start = 40;
			break;
		case 1:
			start = 48;
			break;
		case 3:
			start = 56;
			break;
		default:
			start = 0;

		}
		switch (img) {
		case -5:
			within = 1;
			break;
		case -1:
			within = 2;
			break;
		case -3:
			within = 3;
			break;
		case 7:
			within = 4;
			break;
		case 5:
			within = 5;
			break;
		case 1:
			within = 6;
			break;
		case 3:
			within = 7;
			break;
		default:
			within = 0;

		}

		int value = start + within;
		for (int i = 0; i < 6; i++) {
			if (value % 2 == 1)
				push((byte) 1);
			else
				push((byte) 0);

			value = value / 2;
		}

	}
}
