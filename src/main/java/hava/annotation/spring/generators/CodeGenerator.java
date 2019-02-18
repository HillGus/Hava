package hava.annotation.spring.generators;

import com.google.gson.Gson;
import com.squareup.javapoet.*;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.Filter;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.annotations.Suffixes;
import hava.annotation.spring.utils.AnnotationUtils;
import hava.annotation.spring.utils.ParameterUtils;
import hava.annotation.spring.utils.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Id;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.Arrays;

public class CodeGenerator {


	public final ReflectionUtils refUtils = new ReflectionUtils();
	public final ParameterUtils parUtils = new ParameterUtils();
	public final AnnotationUtils annUtils = new AnnotationUtils();

	private ServiceGenerator serGenerator = new ServiceGenerator(this);
	private ControllerGenerator conGenerator = new ControllerGenerator(this);

	private Filer filer;
	private Messager messager;
	private Types typeUtils;
	private Elements elementUtils;

	private TypeName elementType;
	private TypeName elementIdType = null;

	private boolean debug;
	private String repSuffix;
	private String serSuffix;
	private String conSuffix;

	private String prefix;
	private String packageName;
	private Filter filter;


	public CodeGenerator(Elements elementUtils, Types typeUtils, Messager messager, Filer filer) {

		this.filer = filer;
		this.messager = messager;
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;

		try {
			setDebug((boolean) HASConfiguration.class.getDeclaredMethod("debug").getDefaultValue());
			setSuffixes((Suffixes) HASConfiguration.class.getDeclaredMethod("suffixes").getDefaultValue());
		} catch (Exception e) {
			throw new RuntimeException("Could not load default HASConfigurations: " + e.getMessage());
		}
	}


	public enum Layer {
		REPOSITORY, SERVICE, CONTROLLER
	}


	public void generateClasses(Element element, String type, boolean isAnnotated) throws RuntimeException {

		Layer startFrom = type.equals("Entity") ? Layer.REPOSITORY : type.equals("Repository") ?
			Layer.SERVICE : type.equals("Service") ? Layer.SERVICE : type.equals("Controller") ? Layer.CONTROLLER : null;

		if (startFrom == null) {
			this.messager.printMessage(Kind.ERROR, "Invalid type: " + type, element);
			throw new RuntimeException("Invalid type: " + type);
		}

		final String elementName = element.getSimpleName().toString();
		prefix = isAnnotated ? elementName : elementName.substring(0, elementName.length() - type.length());
		packageName = this.elementUtils.getPackageOf(element).getQualifiedName().toString();

		this.elementType = getTypeName(element.getSimpleName().toString(), "Cound not find class");

		CRUD annCrud = element.getAnnotation(CRUD.class);
		String annEndpoint = annCrud.endpoint();
		String endpoint = annEndpoint == "" ? this.prefix.toLowerCase() : annEndpoint;
		this.filter = annCrud.filter();

		switch (startFrom) {
			case REPOSITORY:
				generateRepository(element);
			case SERVICE:
				generateService();
			case CONTROLLER:
				generateController(endpoint);
		}
	}

	private void generateRepository(Element element) throws RuntimeException {

		for (Element el : element.getEnclosedElements()) {

			if (el.getAnnotation(Id.class) != null) {
				this.elementIdType = getTypeName(ClassName.get(el.asType()).toString(),
					"Could not get class name of " + element.getSimpleName().toString());
			}
		}

		if (this.elementIdType == null)
			throw new RuntimeException("A Entity must have a field annotated with javax.persistence.Id");

		TypeSpec repositorySpec = TypeSpec.interfaceBuilder(prefix + repSuffix)
			.addModifiers(Modifier.PUBLIC)
			.addSuperinterface(getParameterizedTypeName(JpaRepository.class, elementType, elementIdType))
			.build();

		save(repositorySpec);
	}

	private void generateService() throws RuntimeException {

		save(this.serGenerator.generate(this.prefix, this.filter));
	}

	private void generateController(String endpoint) throws RuntimeException {

		save(this.conGenerator.generate(this.prefix, endpoint, this.filter));
	}


	private void save(TypeSpec spec) throws RuntimeException {

		JavaFile file = JavaFile.builder(packageName, spec).build();

		try {
			if (debug) {
				System.out.println("===================================\n");
				file.writeTo(System.out);
				System.out.println("===================================\n");
			}

			file.writeTo(this.filer);
		} catch (IOException e) {
			throw new RuntimeException("Could not write class to filer: " + e.getMessage());
		}
	}

	public FieldSpec autowire(String fieldName, String typeName) {

		return this.autowire(fieldName, typeName, "Could not autowire " + fieldName + " using class " + typeName);
	}

	private FieldSpec autowire(String fieldName, String typeName, String exceptionMessage) {

		TypeName type = getTypeName(typeName, exceptionMessage);

		return FieldSpec.builder(type, fieldName)
			.addAnnotation(Autowired.class).build();
	}

	TypeName getTypeName(String typeName, String exceptionMessage) {

		try {

			return this.refUtils.construct(
				TypeName.class,
				new Class[]{String.class},
				new Object[]{typeName});
		} catch (RuntimeException e) {

			e.printStackTrace();
			throw new RuntimeException(exceptionMessage + ": " + e.getMessage());
		}
	}

	private ParameterizedTypeName getParameterizedTypeName(Class<?> className, TypeName... types) {

		try {
			ParameterizedTypeName instance = new Gson().fromJson(
				new Gson().toJson(ParameterizedTypeName.get(JpaRepository.class)), ParameterizedTypeName.class);
			this.refUtils.setValues(
				instance,
				new String[]{"rawType", "typeArguments"},
				new Object[]{ClassName.get(className), Arrays.asList(types)});
			return instance;
		} catch (IllegalArgumentException | SecurityException e) {

			throw new RuntimeException(
				String.format("Could not get ParameterizedTypeName for %s using generic arguments %s: %s",
					className, Arrays.asList(types), e.getMessage()));
		}
	}

	ParameterSpec elementParam() {

		return this.parUtils.build("entity", this.elementType);
	}

	ParameterSpec elementReqBodyParam() {

		return this.parUtils.build("entity", this.elementType, RequestBody.class);
	}

	ParameterSpec elementIdParam() {

		return this.parUtils.build("id", this.elementIdType);
	}

	ParameterSpec elementIdPathParam() {

		return this.parUtils.build("id", this.elementIdType, this.annUtils.buildWithValue(PathVariable.class, "{id}"));
	}


	public void setDebug(boolean debug) {

		this.debug = debug;
	}

	public void setSuffixes(Suffixes suffixes) {

		this.repSuffix = suffixes.repository();
		this.serSuffix = suffixes.service();
		this.conSuffix = suffixes.controller();
	}

	String repSuffix() {

		return this.repSuffix;
	}

	String serSuffix() {

		return this.serSuffix;
	}

	String conSuffix() {

		return this.conSuffix;
	}
}