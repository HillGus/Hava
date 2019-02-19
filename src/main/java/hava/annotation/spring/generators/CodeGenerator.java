package hava.annotation.spring.generators;

import com.squareup.javapoet.*;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.Filter;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.annotations.Suffixes;
import hava.annotation.spring.utils.AnnotationUtils;
import hava.annotation.spring.utils.ElementUtils;
import hava.annotation.spring.utils.ParameterUtils;
import hava.annotation.spring.utils.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;

public class CodeGenerator {


	public final ReflectionUtils refUtils = new ReflectionUtils();
	public final ParameterUtils parUtils = new ParameterUtils();
	public final AnnotationUtils annUtils = new AnnotationUtils();
	public  ElementUtils eleUtils;

	private ServiceGenerator serGenerator = new ServiceGenerator(this);
	private ControllerGenerator conGenerator = new ControllerGenerator(this);
	private RepositoryGenerator repGenerator = new RepositoryGenerator(this);

	private Filer filer;
	private Messager messager;
	private Types typeUtils;
	private Elements elementUtils;

	private Element element;

	private boolean debug;
	private String repSuffix;
	private String serSuffix;
	private String conSuffix;

	private String prefix;
	private String packageName;
	private Filter filter;
	private boolean pagination;


	public CodeGenerator() {}

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


	public void generateClasses(Element element) throws RuntimeException {

		this.element = element;
		this.prefix = element.getSimpleName().toString();
		this.packageName = this.elementUtils.getPackageOf(element).getQualifiedName().toString();

		this.eleUtils = new ElementUtils(element);

		CRUD annCrud = element.getAnnotation(CRUD.class);
		this.prefix = "".equals(annCrud.name()) ? this.prefix : annCrud.name();
		this.filter = annCrud.filter();
		this.pagination = annCrud.pagination();
		String annEndpoint = annCrud.endpoint();
		String endpoint = "".equals(annEndpoint) ? this.prefix.toLowerCase() : annEndpoint;

		generateRepository();
		generateService();
		generateController(endpoint);
	}


	private void generateRepository() throws RuntimeException {

		save(this.repGenerator.generate(this.prefix, this.filter, this.pagination));
	}

	private void generateService() throws RuntimeException {

		save(this.serGenerator.generate(this.prefix, this.filter, this.pagination));
	}

	private void generateController(String endpoint) throws RuntimeException {

		save(this.conGenerator.generate(this.prefix, this.filter, this.pagination, endpoint));
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

	public TypeName getTypeName(String typeName, String exceptionMessage) {

		try {

			return this.refUtils.construct(
				TypeName.class,
				new Class[]{String.class},
				new Object[]{typeName});
		} catch (RuntimeException e) {

			throw new RuntimeException(exceptionMessage + ": " + e.getMessage());
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