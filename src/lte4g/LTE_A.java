package lte4g;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import com.jeffreybosboom.serviceproviderprocessor.ServiceProvider;

import channel.Channel.LTEChannelKernel;
import edu.mit.streamjit.api.Input;
import edu.mit.streamjit.api.Pipeline;
import edu.mit.streamjit.api.StreamCompiler;
import edu.mit.streamjit.impl.compiler2.Compiler2StreamCompiler;
import edu.mit.streamjit.test.Benchmark;
import edu.mit.streamjit.test.Benchmarker;
import edu.mit.streamjit.test.SuppliedBenchmark;
import receiver.Receiver.LTEReceiverKernel;
import transmitter.Transmitter.LTETransmitterKernel;

public class LTE_A {

	public static void main(String[] args) throws InterruptedException {
		StreamCompiler sc = new Compiler2StreamCompiler();
		Benchmarker.runBenchmark(new LTEBenchmark(), sc).get(0).print(System.out);
		// OneToOneElement<Byte, Byte> streamgraph = new Pipeline<>(new
		// TurboDecoder());
		// StreamCompiler compiler = new DebugStreamCompiler();
		// Path path = Paths.get("src/edu/mit/streamjit/receiver/data.in");
		// Input<Byte> input = Input.fromBinaryFile(path, Byte.class,
		// ByteOrder.LITTLE_ENDIAN);
		// Input<Byte> repeated = Datasets.nCopies(1, input);
		// Output<Byte> out = Output.blackHole();
		// CompiledStream stream = compiler.compile(streamgraph, repeated, out);
		// stream.awaitDrained();
	}

	@ServiceProvider(Benchmark.class)
	public static final class LTEBenchmark extends SuppliedBenchmark {
		public LTEBenchmark() {
			super("LTE", LTEKernel.class, new Dataset("src/edu/mit/streamjit/lte4g/data.in", (Input) Input
					.fromBinaryFile(Paths.get("src/edu/mit/streamjit/lte4g/data.in"), Byte.class, ByteOrder.BIG_ENDIAN)
			// ,
			// (Supplier)Suppliers.ofInstance((Input)Input.fromBinaryFile(Paths.get("/home/jbosboom/streamit/streams/apps/benchmarks/asplos06/fft/streamit/FFT5.out"),
			// Float.class, ByteOrder.LITTLE_ENDIAN))
			));
		}
	}

	public static final class LTEKernel extends Pipeline<Byte, Float> {

		public LTEKernel() {
			this.add(new BytePrinter(), new LTETransmitterKernel(), new LTEChannelKernel(), new LTEReceiverKernel());
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

	private static class IntPrinter extends edu.mit.streamjit.api.Filter<Byte, Byte> {

		public IntPrinter() {
			super(1, 1);
		}

		@Override
		public void work() {
			byte a = pop();
			System.out.println(a);
			push(a);

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
				out = new DataOutputStream(new FileOutputStream("src/edu/mit/streamjit/receiver/" + name));
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
}
