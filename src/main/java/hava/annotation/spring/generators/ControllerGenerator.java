package hava.annotation.spring.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.Filter;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.utils.ElementUtils;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.utils.MiscUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;

public class ControllerGenerator {


	private ElementUtils eleUtils;
	private AnnotationBuilder annBuilder;
	private ParameterBuilder parBuilder;
	private MiscUtils miscUtils;

	private String suffix;
	private String serSuffix;
	private String classesPrefix;
	private String name;

	private boolean pagination;

	ControllerGenerator(CodeGenerator codeGenerator, String suffix, String serSuffix, String classesPrefix) {

		this.eleUtils = codeGenerator.eleUtils;
		this.annBuilder = codeGenerator.annBuilder;
		this.parBuilder = codeGenerator.parBuilder;
		this.miscUtils = codeGenerator.miscUtils;
		this.suffix = suffix;
		this.serSuffix = serSuffix;
		this.classesPrefix = classesPrefix;
	}

	TypeSpec generate(String name, CRUD crud, String endpoint) {

		this.pagination = crud.pagination();
		this.name = name;

		MethodSpec one = MethodSpec.methodBuilder("one")
			.addAnnotation(this.annBuilder.getMapping("{id}"))
			.addParameter(this.eleUtils.elementIdPathParam())
			.addStatement("return this.service.one(id)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec all = crud.filter().fields().length > 0 ? filterableAll(crud.filter()) : simpleAll();

		MethodSpec save = MethodSpec.methodBuilder("save")
			.addAnnotation(this.annBuilder.postMapping(""))
			.addParameter(this.eleUtils.elementReqBodyParam())
			.addStatement("return this.service.save(entity)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec delete = MethodSpec.methodBuilder("delete")
			.addAnnotation(this.annBuilder.deleteMapping("{id}"))
			.addParameter(this.eleUtils.elementIdPathParam())
			.addStatement("return this.service.delete(id)")
			.returns(ResponseEntity.class)
			.build();

		return TypeSpec.classBuilder(this.classesPrefix + name + this.suffix)
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(RestController.class)
			.addAnnotation(this.annBuilder.requestMapping("/" + endpoint))
			.addField(this.miscUtils.autowire("service",
				serviceClassName()))
			.addMethod(all)
			.addMethod(one)
			.addMethod(save)
			.addMethod(delete)
			.build();
	}

	private MethodSpec simpleAll() {

		MethodSpec.Builder builder = MethodSpec.methodBuilder("all")
			.addAnnotation(this.annBuilder.getMapping(""));

		if (!this.pagination)
			builder.addStatement("return $T.ok(this.service.all())", ResponseEntity.class);
		else {
			addPageability(builder);
			builder.addStatement("return $T.ok(this.service.all(page, pageSize))", ResponseEntity.class);
		}

		return builder
			.returns(ResponseEntity.class)
			.build();
	}

	private MethodSpec filterableAll(Filter filter) {

		String[] fields = filter.fields();

		if (fields.length == 1 && "*".equals(fields[0])) {

			fields = this.eleUtils
				.getNonTransientFieldsNames()
				.toArray(new String[]{});
		}

		MethodSpec.Builder builder = MethodSpec.methodBuilder("allByFilter")
			.addAnnotation(this.annBuilder.getMapping(""));

		if (this.pagination)
			addPageability(builder);

		return this.addArguments(builder, fields)
			.addStatement(getReturn(fields), ResponseEntity.class)
			.returns(ResponseEntity.class)
			.build();
	}

	private String getReturn(String[] fields) {

		StringBuilder builder = new StringBuilder("return this.service.allByFilter(");

		for (int i = 0; i < fields.length; i++) {

			String field = fields[i];
			builder.append(field);

			if (i < fields.length - 1)
				builder.append(", ");
		}

		if (this.pagination)
			builder.append(", page, pageSize");

		builder.append(")");
		return builder.toString();
	}

	private void addPageability(MethodSpec.Builder builder) {

		builder
			.addParameter(
				this.parBuilder.build(
					"page",
					Integer.class,
					this.annBuilder.requestParam(false)))
			.addParameter(
				this.parBuilder.build(
					"pageSize",
					Integer.class,
					this.annBuilder.requestParam(false)));
	}

	private MethodSpec.Builder addArguments(MethodSpec.Builder builder, String[] fields) {

		Arrays.stream(fields).forEach(field -> {

			TypeMirror fieldType = this.eleUtils.getEnclosedElement(field).asType();

			builder.addParameter(
				this.parBuilder.build(
					field,
					this.miscUtils.getTypeName(fieldType),
					this.annBuilder.requestParam( false)));
		});

		return builder;
	}

	private String serviceClassName() {

		return this.eleUtils.packageName() + "." + this.classesPrefix + this.name + this.serSuffix;
	}
}
