package hava.annotation.spring;

import com.google.gson.Gson;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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


	private ReflectionUtils refUtils = new ReflectionUtils();
	private ParameterUtils parUtils = new ParameterUtils();
	private AnnotationUtils annUtils = new AnnotationUtils();

	private Filer filer;
	private Messager messager;
	private Types typeUtils;
	private Elements elementUtils;

	private TypeName elementType;
	private TypeName elementIdType = null;

	private boolean debug = true;

	private String prefix;
	private String packageName;


	public CodeGenerator(Elements elementUtils, Types typeUtils, Messager messager, Filer filer) {

		this.filer = filer;
		this.messager = messager;
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
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

		switch (startFrom) {
			case REPOSITORY:
				generateRepository(element);
			case SERVICE:
				generateService();
			case CONTROLLER:
				generateController();
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

		TypeSpec repositorySpec = TypeSpec.interfaceBuilder(prefix + "Repository")
			.addModifiers(Modifier.PUBLIC)
			.addSuperinterface(getParameterizedTypeName(JpaRepository.class, elementType, elementIdType))
			.build();

		save(repositorySpec);
	}

	private void generateService() throws RuntimeException {


		MethodSpec save = this.serviceMethod("save")
			.addParameter(this.elementParam())
			.addStatement("return $T.ok(this.repository.save(entity))", ResponseEntity.class)
			.build();

		MethodSpec one = this.serviceMethod("one")
			.addParameter(this.elementIdParam())
			.addStatement("return $T.ok(this.repository.findById(id))", ResponseEntity.class)
			.build();

		MethodSpec all = this.serviceMethod("all")
			.addStatement("return $T.ok(this.repository.findAll())", ResponseEntity.class)
			.build();

		MethodSpec delete = this.serviceMethod("delete")
			.addParameter(this.elementIdParam())
			.addStatement("this.repository.deleteById(id)")
			.addStatement("return new $T($T.NO_CONTENT)", ResponseEntity.class, HttpStatus.class)
			.build();

		TypeSpec serviceSpec = TypeSpec.classBuilder(prefix + "Service")
			.addModifiers(Modifier.PUBLIC)
			.addField(autowire("repository", prefix + "Repository"))
			.addAnnotation(Component.class)
			.addMethod(save)
			.addMethod(one)
			.addMethod(all)
			.addMethod(delete)
			.build();

		save(serviceSpec);
	}

	private void generateController() throws RuntimeException {

		MethodSpec one = MethodSpec.methodBuilder("one")
			.addAnnotation(this.annUtils.getMapping("{id}"))
			.addParameter(this.elementIdPathParam())
			.addStatement("return this.service.one(id)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec all = MethodSpec.methodBuilder("all")
			.addAnnotation(this.annUtils.getMapping(""))
			.addStatement("return this.service.all()")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec save = MethodSpec.methodBuilder("save")
			.addAnnotation(this.annUtils.postMapping(""))
			.addParameter(this.elementReqBodyParam())
			.addStatement("return this.service.save(entity)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec delete = MethodSpec.methodBuilder("delete")
			.addAnnotation(this.annUtils.deleteMapping("{id}"))
			.addParameter(this.elementIdPathParam())
			.addStatement("return this.service.delete(id)")
			.returns(ResponseEntity.class)
			.build();

		TypeSpec controllerSpec = null;

		controllerSpec = TypeSpec.classBuilder(prefix + "Controller")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(RestController.class)
			.addAnnotation(this.annUtils.requestMapping("/" + prefix.toLowerCase()))
			.addField(autowire("service", prefix + "Service"))
			.addMethod(all)
			.addMethod(one)
			.addMethod(save)
			.addMethod(delete)
			.build();

		save(controllerSpec);
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

	private FieldSpec autowire(String fieldName, String typeName) {

		return this.autowire(fieldName, typeName, "Could not autowire " + fieldName + " using class " + typeName);
	}

	private FieldSpec autowire(String fieldName, String typeName, String exceptionMessage) {

		TypeName type = getTypeName(typeName, exceptionMessage);

		return FieldSpec.builder(type, fieldName)
			.addAnnotation(Autowired.class).build();
	}

	private TypeName getTypeName(String typeName, String exceptionMessage) {

		try {

			return this.refUtils.construct(
				TypeName.class,
				new Class[]{String.class},
				new Object[]{typeName});
		} catch (RuntimeException e) {

			throw new RuntimeException(exceptionMessage + ": " + e.getMessage());
		}
	}

	private ParameterizedTypeName getParameterizedTypeName(Class<?> className, TypeName... types) {

		Class<ParameterizedTypeName> cls = ParameterizedTypeName.class;

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

	private ParameterSpec elementParam() {

		return this.parUtils.build("entity", this.elementType);
	}

	private ParameterSpec elementReqBodyParam() {

		return this.parUtils.build("entity", this.elementType, RequestBody.class);
	}

	private ParameterSpec elementIdParam() {

		return this.parUtils.build("id", this.elementIdType);
	}

	private ParameterSpec elementIdPathParam() {

		return this.parUtils.build("id", this.elementIdType, this.annUtils.buildWithValue(PathVariable.class, "{id}"));
	}

	private MethodSpec.Builder serviceMethod(String name) {

		return MethodSpec.methodBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.returns(ResponseEntity.class);
	}
}