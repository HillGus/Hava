package hava.debug.transformer;

import hava.debug.annotation.Output;
import hava.debug.misc.CtClassWrapper;
import hava.debug.misc.TransformationUtils;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class OutputTransformer implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!TransformationUtils.isInTransformationTree(className))
			return null;

		try {
			CtClassWrapper classWrapper = new CtClassWrapper(classfileBuffer);
			CtClass cls = classWrapper.getCtClass();

			for (CtMethod method : classWrapper.methodsAnnotatedWith(Output.class)) {

				if (method.getReturnType() == CtClass.voidType)
					continue;

				cls.removeMethod(method);

				CtMethod newMethod = new CtMethod(method.getReturnType(), method.getName(), method.getParameterTypes(), cls);
				newMethod.setModifiers(method.getModifiers());

				String newName = method.getLongName().replaceAll("[^a-zA-Z]", "_");
				method.setName(newName);

				cls.addMethod(method);

				newMethod.setBody("{" +
					method.getReturnType().getName() + " result = null;\n"
					+ "result = " + newName + "($$);"
					+ String.format(
					"System.out.println(\"Output value of %s.%s(): \" + result);",
					className.replace('/', '.'), newMethod.getName())
					+ "return result; }");

				cls.addMethod(newMethod);
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
