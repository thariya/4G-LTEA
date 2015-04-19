package channel;


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
import edu.mit.streamjit.impl.interp.DebugStreamCompiler;
import edu.mit.streamjit.test.Benchmark;
import edu.mit.streamjit.test.Benchmarker;
import edu.mit.streamjit.test.SuppliedBenchmark;


public class Channel {
	
	public static void main(String[] args) throws InterruptedException {
		
		//compiler2streamcompiler
		StreamCompiler sc = new DebugStreamCompiler();
		Benchmarker.runBenchmark(new ChannelBenchmark(), sc).get(0).print(System.out);
//		OneToOneElement<Byte, Byte> streamgraph = new Pipeline<>(new TurboEncoder(),new Modulator(),new AntennaArray());
//		StreamCompiler compiler = new DebugStreamCompiler();
//		Path path = Paths.get("src/edu/mit/streamjit/transmitter/data.in");
//		Input<Byte> input = Input.fromBinaryFile(path, Byte.class,
//				ByteOrder.LITTLE_ENDIAN);
//		Input<Byte> repeated = Datasets.nCopies(1, input);
//		Output<Byte> out = Output.blackHole();
//		CompiledStream stream = compiler.compile(streamgraph, repeated, out);
//		stream.awaitDrained();
	}
	
	@ServiceProvider(Benchmark.class)
	public static final class ChannelBenchmark extends SuppliedBenchmark {
		public ChannelBenchmark() {
			super("Channel", ChannelKernel.class, new Dataset("src/channel/transmitter_out.out",
					(Input)Input.fromBinaryFile(Paths.get("src/channel/transmitter_out.out"), Float.class, ByteOrder.BIG_ENDIAN)
//					, (Supplier)Suppliers.ofInstance((Input)Input.fromBinaryFile(Paths.get("/home/jbosboom/streamit/streams/apps/benchmarks/asplos06/fft/streamit/FFT5.out"), Float.class, ByteOrder.LITTLE_ENDIAN))
			));
		}
	}
	
	public static final class ChannelKernel extends Pipeline<Float, Float> {
		
		public ChannelKernel() {
			this.add(new Channel3(),new Filer("channel_out.out"));
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
			System.out.println(a+" "+b+" ");
			Byte c =(byte)(a+b);
			push(c);
		}
	}
	
	private static class Seperator extends edu.mit.streamjit.api.Filter<Float, Float> {

		public Seperator() {
			super(1, 1);
		}

		@Override
		public void work() {
			push(pop());
			System.out.print("*");			
		}
	}

	private static class Printer extends
			edu.mit.streamjit.api.Filter<Float,Float> {

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
	
	private static class BytePrinter extends
	edu.mit.streamjit.api.Filter<Byte,Byte> {

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
	
	private static class Filer extends
	edu.mit.streamjit.api.Filter<Float,Float> {
		DataOutputStream out;
		
		public Filer(String name) {
			super(1, 1);
			out=null;
			out=getDataStream(out,"src/receiver/"+name);	
			
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
		
		private static DataOutputStream getDataStream(DataOutputStream d,String s){
			try {
				d = new DataOutputStream(new FileOutputStream(s));				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			return d;
		}
	}
	
	
}