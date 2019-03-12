package hava.debug.transformer;

import hava.debug.annotation.ExecutionTime;
import hava.debug.misc.CtClassWrapper;
import hava.debug.misc.TransformationUtils;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class PerformanceTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!TransformationUtils.isInTransformationTree(className))
			return null;

		try {
			CtClassWrapper classWrapper = new CtClassWrapper(classfileBuffer);
			CtClass cls = classWrapper.getCtClass();

			for (CtMethod method : classWrapper.methodsAnnotatedWith(ExecutionTime.class)) {

				method.addLocalVariable("start", CtClass.longType);
				method.insertBefore("start = System.currentTimeMillis();");
				method.insertAfter(
					String.format("System.out.println(\"Execution time of %s.%s(): \" + %s + \" milliseconds\");",
						className.replace('/', '.'),
						method.getName(),
						"(System.currentTimeMillis() - start)")
				);
			}

			byte[] byteCode = cls.toBytecode();
			cls.detach();
			return byteCode;
		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}
}
