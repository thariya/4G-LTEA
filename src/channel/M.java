package channel;

import java.io.Serializable;

public class M implements Serializable {
	double[][] Hreal;
	double[][] Himg;
	double[][] Houtreal;
	double[][] Houtimg;

	M(double[][] a, double[][] b, double[][] c, double[][] d) {
		Hreal = a;
		Himg = b;
		Houtreal = c;
		Houtimg = d;
	}
}
