package hava.annotation.spring.generators;

import com.squareup.javapoet.*;
import hava.annotation.spring.annotations.Authentication;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.annotations.Suffixes;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.generators.authentication.AuthenticationFilterGenerator;
import hava.annotation.spring.generators.authentication.AuthorizationFilterGenerator;
import hava.annotation.spring.generators.authentication.UtilGenerator;
import hava.annotation.spring.generators.authentication.WebConfigGenerator;
import hava.annotation.spring.generators.crud.ControllerGenerator;
import hava.annotation.spring.generators.crud.RepositoryGenerator;
import hava.annotation.spring.generators.crud.ServiceGenerator;
import hava.annotation.spring.utils.ElementUtils;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.utils.MiscUtils;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.List;

public class CodeGenerator {


	public ParameterBuilder parBuilder = new ParameterBuilder();
	public 	AnnotationBuilder annBuilder = new AnnotationBuilder();
	public MiscUtils miscUtils = new MiscUtils();
	public ElementUtils eleUtils;

	private Filer filer;
	private Elements elementUtils;
	private Types typeUtils;
	private Messager messager;

	private boolean debug;
	private String repSuffix;
	private String serSuffix;
	private String conSuffix;
	private String classesPrefix;

	private String generatedFor;
	private String packageName;

	public CodeGenerator(Elements elementUtils, Filer filer, Types typeUtils, Messager messager) {

		this.filer = filer;
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
		this.messager = messager;

		try {
			setDebug((boolean) HASConfiguration.class.getDeclaredMethod("debug").getDefaultValue());
			setSuffixes((Suffixes) HASConfiguration.class.getDeclaredMethod("suffixes").getDefaultValue());
			setClassesPrefix((String) HASConfiguration.class.getDeclaredMethod("classesPrefix").getDefaultValue());
		} catch (Exception e) {
			throw new RuntimeException("Could not load default HASConfigurations: " + e.getMessage());
		}
	}


	public void generateAuthClasses(Authentication auth, String packageName) throws RuntimeException {

		this.generatedFor = "JWT Authentication";
		TypeMirror encoderType = null;

		try {
			auth.encoder();
		} catch (MirroredTypeException e) {
			encoderType = e.getTypeMirror();
		}

		boolean useEncoderGetInstance = hasGetInstanceMethod(encoderType);
		verifyEncoderType(encoderType);

		String secret = auth.secret();
		SignatureAlgorithm algorithm = auth.algorithm();
		Long expiration = auth.expiration();

		WebConfigGenerator webConfigGenerator = new WebConfigGenerator(this, this.classesPrefix, packageName);
		UtilGenerator utilGenerator = new UtilGenerator(this, this.classesPrefix);
		AuthenticationFilterGenerator authFilterGenerator = new AuthenticationFilterGenerator(this, this.classesPrefix, packageName);
		AuthorizationFilterGenerator authoFilterGenerator = new AuthorizationFilterGenerator(this, this.classesPrefix, packageName);


		save(
			webConfigGenerator.generate(encoderType, useEncoderGetInstance),
			packageName);

		save(
			utilGenerator.generate(secret, expiration, algorithm),
			packageName);

		save(authFilterGenerator.generate(),
			packageName);

		save(authoFilterGenerator.generate(),
			packageName);
	}

	public boolean hasGetInstanceMethod(TypeMirror type) {

		boolean result = false;

		Element clazz = this.typeUtils.asElement(type);

		for (Element element : clazz.getEnclosedElements()) {

			if (element.getKind() == ElementKind.METHOD) {

				String name = element.getSimpleName().toString();
				String erasure = this.typeUtils.erasure(element.asType()).toString();
				if ("getInstance".equals(name)
					&& ("()" + type.toString()).equals(erasure)
					&& (element.getModifiers().contains(Modifier.PUBLIC))
					&& (element.getModifiers().contains(Modifier.STATIC)))
					result = true;
			}
		}

		if (!result)
			this.messager.printMessage(Kind.NOTE, String.format(
				"----- Consider creating a public static %s getInstance() method at %s -----",
				type.toString(), type.toString()
			));

		return result;
	}

	public void verifyEncoderType(TypeMirror type) {

		Element encoderEle = this.typeUtils.asElement(type);

		boolean noArgsConstructor = false;

		for (Element element : encoderEle.getEnclosedElements()) {

			if (element.getKind() == ElementKind.CONSTRUCTOR) {
				if ("()void".equals(this.typeUtils.erasure(element.asType()).toString())
				&& element.getModifiers().contains(Modifier.PUBLIC))
					noArgsConstructor = true;
			}
		}

		if (!noArgsConstructor)
			throw new RuntimeException("A class used as encoder for @Authentication must have a constructor with no arguments");

		List<? extends TypeMirror> encoderSuperClasses = this.typeUtils.directSupertypes(type);
		String passwordEncoderPath = "org.springframework.security.crypto.password.PasswordEncoder";

		boolean implementsPassEncoder = false;

		for (TypeMirror superClassType : encoderSuperClasses) {

			if (superClassType.toString().equals(passwordEncoderPath))
				implementsPassEncoder = true;
		}

		if (!implementsPassEncoder) {
			throw new RuntimeException("A class used as encoder for @Authentication must implement org.springframework.security.crypto.password.PasswordEncoder");
		}

	}

	public void generateCrudClasses(Element element) throws RuntimeException {

		this.eleUtils = new ElementUtils(element, elementUtils);

		RepositoryGenerator repGenerator = new RepositoryGenerator(this, this.repSuffix, this.classesPrefix);
		ServiceGenerator serGenerator = new ServiceGenerator(this, this.serSuffix, this.repSuffix, this.classesPrefix);
		ControllerGenerator conGenerator = new ControllerGenerator(this, this.conSuffix, this.serSuffix, this.classesPrefix);

		String prefix = element.getSimpleName().toString();
		this.packageName = this.eleUtils.packageOf(element);

		CRUD annCrud = element.getAnnotation(CRUD.class);
		prefix = "".equals(annCrud.name()) ? prefix : annCrud.name();
		String endpoint = "".equals(annCrud.endpoint()) ? prefix.toLowerCase() : annCrud.endpoint();

		save(repGenerator.generate(prefix, annCrud));
		save(serGenerator.generate(prefix, annCrud));
		save(conGenerator.generate(prefix, annCrud, endpoint));
	}


	private void save(TypeSpec spec) throws RuntimeException {

		this.save(spec, this.packageName);
	}

	private void save(TypeSpec spec, String packageName) throws RuntimeException {

		JavaFile file = JavaFile.builder(packageName, spec).build();

		String generatedFor = this.eleUtils != null ? this.eleUtils.elementTypeStr() : this.generatedFor;

		try {
			if (debug) {
				System.out.println("\n" + StringUtils.center(
					String.format(
						"Class generated for %s",
						generatedFor
					), 75, '-'));

				file.writeTo(System.out);

				System.out.println(StringUtils.center("", 75, '-'));
			}

			file.writeTo(this.filer);
		} catch (IOException e) {
			throw new RuntimeException("Could not write class to filer: " + e.getMessage());
		}
	}


	public void setDebug(boolean debug) {

		this.debug = debug;
	}

	public void setSuffixes(Suffixes suffixes) {

		this.repSuffix = suffixes.repository();
		this.serSuffix = suffixes.service();
		this.conSuffix = suffixes.controller();
	}

	public void setClassesPrefix(String classesPrefix) {

		this.classesPrefix = classesPrefix;
	}
}

class StringUtils {

	static String center(String s, int size, char pad) {
		if (s == null || size <= s.length())
			return s;

		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < (size - s.length()) / 2; i++) {
			sb.append(pad);
		}
		sb.append(s);
		while (sb.length() < size) {
			sb.append(pad);
		}
		return sb.toString();
	}
}