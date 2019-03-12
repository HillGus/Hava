package hava.debug.misc;

import hava.debug.transformer.InputTransformer;
import hava.debug.transformer.OutputTransformer;
import hava.debug.transformer.PerformanceTransformer;

import java.lang.instrument.Instrumentation;

public class HavaAgent {

	public static void premain(String args, Instrumentation inst) {

		inst.addTransformer(new PerformanceTransformer());
		inst.addTransformer(new InputTransformer());
		inst.addTransformer(new OutputTransformer());
	}
}
