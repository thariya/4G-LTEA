package transmitter;

public class TurboEncoder extends edu.mit.streamjit.api.Pipeline<Byte, Byte> {
	
	public TurboEncoder(){
		super(	
				new Encoder()
//				new Printer()
		);
		
		
	}
	
	
	private static class Encoder extends
	edu.mit.streamjit.api.Filter<Byte, Byte> {
		
		private static int val=0;
		private int block_size=40;
		private static int[] perms=new int[40];
		private static int[] reverse_perms=new int[40];
		private static int f1=3;
		private static int f2=10;
		public Encoder() {
			super(40,132);
			for (int i = 0; i < perms.length; i++) {
				int val=(f1*i+f2*i*i)%block_size;
				perms[i]=val;
				reverse_perms[val]=i;
			}
		}

		@Override
		public void work() {
			byte[] input=new byte[40];
			byte[] permed=new byte[40];
			
			for (int i = 0; i <40; i++) {
				input[i]=pop();
			}	
			
			for (int i = 0; i < 40; i++) {
				permed[i]=(input[reverse_perms[i]]);
			}
			
			byte D0=0;
			byte D1=0;
			byte D2=0;
			byte D3=0;
			
			byte tD0=0;
			byte tD1=0;
			byte tD2=0;
			byte tD3=0;
			
			byte D02=0;
			byte D12=0;
			byte D22=0;
			byte D32=0;
			
			byte tD02=0;
			byte tD12=0;
			byte tD22=0;
			byte tD32=0;
			
			for (int i = 0; i < 40; i++) {
				tD0 = D0;
				tD1 = D1;
				tD2 = D2;
				tD3 = D3;
				
				tD02 = D02;
				tD12 = D12;
				tD22 = D22;
				tD32 = D32;
				
				D0 = input[i];
				D02 = permed[i];
				
				D3 = tD2;
				D2 = tD1;
				D1 = (byte) ((D0 + tD2 + tD3) % 2);
				
				D32 = tD22;
				D22 = tD12;
				D12 = (byte) ((D02 + tD22 + tD32) % 2);


				push(D0);
				push((byte) ((D1 + tD1 + tD3) % 2)) ;
				push((byte) ((D12 + tD12 + tD32) % 2)) ;

			}
			for (int k = 0; k < 3; k++) {
				tD0 = D0;
				tD1 = D1;
				tD2 = D2;
				tD3 = D3;
				D3 = tD2;
				D2 = tD1;
				
				D0 = (byte) ((tD2 + tD3) % 2);
				D1 = (byte) ((D0 + tD2 + tD3) % 2);
							
				push(D0);
				push((byte) ((D1 + tD1 + tD3) % 2)) ;
			}
			
			for (int k = 0; k < 3; k++) {
				tD02 = D02;
				tD12 = D12;
				tD22 = D22;
				tD32 = D32;
				D32 = tD22;
				D22 = tD12;

				D02 = (byte) ((tD22 + tD32) % 2);
				D12 = (byte) ((D02 + tD22 + tD32) % 2);
				
				push(D02);
				push((byte) ((D12 + tD12 + tD32) % 2)) ;
			}

		}
	
	}
		
	private static class Output1 extends
	edu.mit.streamjit.api.Filter<Byte, Byte> {
		
		public Output1() {
			super(1, 1);
		}

		@Override
		public void work() {
			byte a = pop();
			
			push(a);

		}
	}
	
	private static class Output2 extends
	edu.mit.streamjit.api.Filter<Byte, Byte> {
		
		byte[] d={0, 0 , 0 , 0};
		byte[] weights={1 ,0 , 1 , 1};
		
		public Output2() {
			super(1, 1);
		}

		@Override
		public void work() {
			d[0] = pop();
			
			byte out=0;
			
			for (int i = 0; i < d.length; i++) {
				out=(byte)(out^(d[i]*weights[i]));
			}
			
			push(out);
			
			for (int i =d.length-1; i>0; i--) {
				d[i]=d[i-1];				
			}
		}
	}
	
	
	private static class Output3 extends
	edu.mit.streamjit.api.Filter<Byte, Byte> {
		
		byte[] d={0, 0 , 0 , 0};
		byte[] weights={1 ,1 , 0 , 1};
		
		public Output3() {
			super(1, 1);
		}

		@Override
		public void work() {
			d[0] = pop();
			
			byte out=0;
			
			for (int i = 0; i < d.length; i++) {
				out=(byte)(out^(d[i]*weights[i]));
			}
			
			push(out);
			
			for (int i =d.length-1; i>0; i--) {
				d[i]=d[i-1];				
			}
			
		}
	}
	

	
//	private static class Interleaver extends
//	edu.mit.streamjit.api.Filter<Byte, Byte> {
//		
//		public Interleaver() {
//			super(40,40);
//		}
//
//		@Override
//		public void work() {
//			byte[] input=new byte[40];
//						
//			for (int i = 0; i <40; i++) {
//				input[i]=pop();
//			}								
//				
//			
//			for (int i = 0; i < 40; i++) {
//				push(input[reverse_perms[i]]);
//			}
//			
//		}
//	}
	
	private static class Printer extends
	edu.mit.streamjit.api.Filter<Byte, Byte> {
		
		public Printer() {
			super(1, 1);
		}

		@Override
		public void work() {
			byte a = pop();
			System.out.println(a);
			push(a);

		}
	}
		
}	

