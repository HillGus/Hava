package hava.annotation.spring.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.annotations.Filter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.persistence.Transient;

public class ControllerGenerator {


	private CodeGenerator codeGenerator;

	private boolean pagination;


	public ControllerGenerator(CodeGenerator codeGenerator) {

		this.codeGenerator = codeGenerator;
	}

	public TypeSpec generate(String prefix, Filter filter, boolean pagination, String endpoint) {

		this.pagination = pagination;

		MethodSpec one = MethodSpec.methodBuilder("one")
			.addAnnotation(this.codeGenerator.annUtils.getMapping("{id}"))
			.addParameter(this.codeGenerator.eleUtils.elementIdPathParam())
			.addStatement("return this.service.one(id)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec all = filter.fields().length > 0 ? filterableAll(filter) : simpleAll();

		MethodSpec save = MethodSpec.methodBuilder("save")
			.addAnnotation(this.codeGenerator.annUtils.postMapping(""))
			.addParameter(this.codeGenerator.eleUtils.elementReqBodyParam())
			.addStatement("return this.service.save(entity)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec delete = MethodSpec.methodBuilder("delete")
			.addAnnotation(this.codeGenerator.annUtils.deleteMapping("{id}"))
			.addParameter(this.codeGenerator.eleUtils.elementIdPathParam())
			.addStatement("return this.service.delete(id)")
			.returns(ResponseEntity.class)
			.build();

		return TypeSpec.classBuilder(prefix + this.codeGenerator.conSuffix())
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(RestController.class)
			.addAnnotation(this.codeGenerator.annUtils.requestMapping("/" + endpoint))
			.addField(this.codeGenerator.autowire("service", prefix + this.codeGenerator.serSuffix()))
			.addMethod(all)
			.addMethod(one)
			.addMethod(save)
			.addMethod(delete)
			.build();
	}

	private MethodSpec simpleAll() {

		MethodSpec.Builder builder = MethodSpec.methodBuilder("all")
			.addAnnotation(this.codeGenerator.annUtils.getMapping(""));

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

			fields = codeGenerator.eleUtils
				.getNamesWithoutAnnotationByKind(Transient.class, ElementKind.FIELD)
				.toArray(new String[]{});
		}

		MethodSpec.Builder builder = MethodSpec.methodBuilder("allByFilter")
			.addAnnotation(this.codeGenerator.annUtils.getMapping(""));

		if (this.pagination)
			addPageability(builder);

		return this.addArguments(builder, fields)
			.addStatement(getReturn(fields), ResponseEntity.class)
			.returns(ResponseEntity.class)
			.build();
	}

	private String getReturn(String[] fields) {

		String r = "return this.service.allByFilter(";
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			r += field;
			if (i < fields.length - 1)
				r += ", ";
		}

		if (this.pagination)
			r += ", page, pageSize";

		r += ")";
		return r;
	}

	private void addPageability(MethodSpec.Builder builder) {

		builder
			.addParameter(
				codeGenerator.parUtils.build("page", Integer.class,
					codeGenerator.annUtils.requestParam(false)))
			.addParameter(
				codeGenerator.parUtils.build("pageSize", Integer.class,
					codeGenerator.annUtils.requestParam(false)));
	}

	private MethodSpec.Builder addArguments(MethodSpec.Builder builder, String[] fields) {

		for(int i = 0; i < fields.length; i++) {

			String param = fields[i];
			String paramType = codeGenerator.eleUtils.getEnclosingElement(param).asType().toString();

			builder.addParameter(
				codeGenerator.parUtils.build(
					param,
					codeGenerator.getTypeName(paramType, ""),
					codeGenerator.annUtils.requestParam( false)));
		}

		return builder;
	}
}
