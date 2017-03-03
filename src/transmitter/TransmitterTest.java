package transmitter;

import java.nio.ByteOrder;
import java.nio.file.Paths;

import com.jeffreybosboom.serviceproviderprocessor.ServiceProvider;

import edu.mit.streamjit.api.Input;
import edu.mit.streamjit.api.Pipeline;
import edu.mit.streamjit.impl.compiler2.Compiler2StreamCompiler;
import edu.mit.streamjit.test.Benchmark;
import edu.mit.streamjit.test.Benchmarker;
import edu.mit.streamjit.test.Datasets;
import edu.mit.streamjit.test.SuppliedBenchmark;

public class TransmitterTest {

	public static void main(String[] args) throws InterruptedException {
		Compiler2StreamCompiler sc = new Compiler2StreamCompiler();
		for (int i = 4; i < 17; i = i * 2) {
			for (int j = 100000; j < 200001; j = j + 10000) {
				for (int k = 1; k < 500; k = k + 5) {
					for (int k2 = 0; k2 < 5; k2++) {
						// Compiler2StreamCompiler sc = new
						// Compiler2StreamCompiler();
						sc.maxNumCores(i);
						sc.multiplier(k);
						Benchmarker.runBenchmark(new TransmitterBenchmarkTest(j), sc).get(0).print(System.out);
					}

				}
			}
		}

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
	public static final class TransmitterBenchmarkTest extends SuppliedBenchmark {
		public TransmitterBenchmarkTest(int j) {
			super("Transmitter", TransmitterKernelTest.class, new Dataset("data.in", (Input) Datasets.nCopies(j,
					Input.fromBinaryFile(Paths.get("data.in"), Byte.class, ByteOrder.LITTLE_ENDIAN))
			// ,
			// (Supplier)Suppliers.ofInstance((Input)Input.fromBinaryFile(Paths.get("/home/jbosboom/streamit/streams/apps/benchmarks/asplos06/fft/streamit/FFT5.out"),
			// Float.class, ByteOrder.LITTLE_ENDIAN))
			));
		}
	}

	public static final class TransmitterKernelTest extends Pipeline<Byte, Float> {

		public TransmitterKernelTest() {
			this.add(new TurboEncoder(), new Modulator(), new AntennaArray());
		}

	}

}
