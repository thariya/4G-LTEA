package receiver;

public class LSDEqualizer extends
edu.mit.streamjit.api.Filter<Double, Double>{
	static int length=12;
	static int stuff=2;	
	public LSDEqualizer() {
		super((length+2*stuff)*2,length*2);		
	}
	
	@Override
	public void work() {
		
	}

}
