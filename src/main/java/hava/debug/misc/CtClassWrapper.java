package hava.debug.misc;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CtClassWrapper {

	private CtClass cls;

	public CtClassWrapper(byte[] byteCode) {

		this.cls = makeClass(byteCode);
	}

	private CtClass makeClass(byte[] byteCode) {

		ByteArrayInputStream input = new ByteArrayInputStream(byteCode);

		CtClass cls = null;
		try {
			cls = ClassPool.getDefault().makeClass(input);
			input.close();
		} catch (IOException e) {

			throw new RuntimeException("Could not make class from byte code");
		}

		return cls;
	}


	public CtClass getCtClass() {

		return this.cls;
	}

	public List<CtMethod> methodsAnnotatedWith(Class<? extends Annotation> annotation) {

		return Arrays.stream(this.cls.getDeclaredMethods()).filter(method -> {
			try {
				return method.getAnnotation(annotation) != null;
			} catch (ClassNotFoundException e) {
				return false;
			}
		}).collect(Collectors.toList());
	}
}
