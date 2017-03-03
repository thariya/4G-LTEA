package transmitter;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import com.jeffreybosboom.serviceproviderprocessor.ServiceProvider;

import edu.mit.streamjit.api.Input;
import edu.mit.streamjit.api.Pipeline;
import edu.mit.streamjit.api.StreamCompiler;
import edu.mit.streamjit.impl.compiler2.Compiler2StreamCompiler;
import edu.mit.streamjit.test.Benchmark;
import edu.mit.streamjit.test.Benchmarker;
import edu.mit.streamjit.test.SuppliedBenchmark;

public class Transmitter {

	public static void main(String[] args) throws InterruptedException {

		// compile2streamcompiler
		StreamCompiler sc = new Compiler2StreamCompiler();
		Benchmarker.runBenchmark(new TransmitterBenchmark(), sc).get(0).print(System.out);
		// OneToOneElement<Byte, Byte> streamgraph = new Pipeline<>(new
		// TurboEncoder(),new Modulator(),new AntennaArray());
		// StreamCompiler compiler = new DebugStreamCompiler();
		// Path path = Paths.get("src/edu/mit/streamjit/transmitter/data.in");
		// Input<Byte> input = Input.fromBinaryFile(path, Byte.class,
		// ByteOrder.LITTLE_ENDIAN);
		// Input<Byte> repeated = Datasets.nCopies(1, input);
		// Output<Byte> out = Output.blackHole();
		// CompiledStream stream = compiler.compile(streamgraph, repeated, out);
		// stream.awaitDrained();
	}

	@ServiceProvider(Benchmark.class)
	public static final class TransmitterBenchmark extends SuppliedBenchmark {
		public TransmitterBenchmark() {
			super("Transmitter", TransmitterKernel.class, new Dataset("src/transmitter/data.in", (Input) Input
					.fromBinaryFile(Paths.get("src/transmitter/data.in"), Byte.class, ByteOrder.LITTLE_ENDIAN)
			// ,
			// (Supplier)Suppliers.ofInstance((Input)Input.fromBinaryFile(Paths.get("/home/jbosboom/streamit/streams/apps/benchmarks/asplos06/fft/streamit/FFT5.out"),
			// Float.class, ByteOrder.LITTLE_ENDIAN))
			));
		}
	}

	public static final class TransmitterKernel extends Pipeline<Byte, Float> {

		public TransmitterKernel() {
			this.add(new TurboEncoder(), new Modulator(), new AntennaArray());
		}

	}

	private static class Add extends edu.mit.streamjit.api.Filter<Byte, Byte> {

		public Add() {
			super(2, 1);
		}

		@Override
		public void work() {
			Byte a = pop();
			Byte b = pop();
			System.out.println(a + " " + b + " ");
			Byte c = (byte) (a + b);
			push(c);
		}
	}

	private static class Printer extends edu.mit.streamjit.api.Filter<Float, Float> {

		public Printer() {
			super(1, 1);
		}

		@Override
		public void work() {
			float a = pop();
			System.out.println(a);
			push(a);

		}
	}

	private static class BytePrinter extends edu.mit.streamjit.api.Filter<Byte, Byte> {

		public BytePrinter() {
			super(1, 1);
		}

		@Override
		public void work() {
			byte a = pop();
			System.out.println(a);
			push(a);

		}
	}

	private static class Filer extends edu.mit.streamjit.api.Filter<Float, Float> {
		DataOutputStream out;

		public Filer(String name) {
			super(1, 1);
			out = null;

			try {
				out = new DataOutputStream(new FileOutputStream("src/channel/" + name));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		@Override
		public void work() {
			float a = pop();
			try {
				out.writeFloat(a);
			} catch (IOException e) {
				e.printStackTrace();
			}
			push(a);

		}
	}

	private static class ByteFiler extends edu.mit.streamjit.api.Filter<Byte, Byte> {
		DataOutputStream out;

		public ByteFiler(String name) {
			super(1, 1);
			out = null;

			try {
				out = new DataOutputStream(new FileOutputStream("src/edu/mit/streamjit/" + name));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		@Override
		public void work() {
			byte a = pop();
			try {
				out.writeByte(a);
			} catch (IOException e) {
				e.printStackTrace();
			}
			push(a);

		}
	}
}
