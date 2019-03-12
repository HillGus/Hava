package hava.debug.transformer;

import hava.debug.annotation.Input;
import hava.debug.misc.CtClassWrapper;
import hava.debug.misc.TransformationUtils;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class InputTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!TransformationUtils.isInTransformationTree(className))
			return null;

		try {
			CtClassWrapper classWrapper = new CtClassWrapper(classfileBuffer);
			CtClass cls = classWrapper.getCtClass();

			for (CtMethod method : classWrapper.methodsAnnotatedWith(Input.class)) {

				if (method.getParameterTypes().length == 0)
					continue;

				method.insertBefore(String.format(
					"System.out.println(\"Input values for %s.%s(): \" + java.util.Arrays.asList($args));",
					className.replace('/', '.'), method.getName()
				));
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
