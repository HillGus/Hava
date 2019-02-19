package hava.annotation.spring.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.annotations.Filter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.persistence.Transient;

public class ServiceGenerator {


	private CodeGenerator codeGenerator;

	private boolean pagination;

	public ServiceGenerator(CodeGenerator codeGenerator) {

		this.codeGenerator = codeGenerator;
	}

	public TypeSpec generate(String prefix, Filter filter, boolean pagination) {

		this.pagination = pagination;

		MethodSpec save = this.createMethod("save")
			.addParameter(this.codeGenerator.eleUtils.elementParam())
			.addStatement("return $T.ok(this.repository.save(entity))", ResponseEntity.class)
			.build();

		MethodSpec one = this.createMethod("one")
			.addParameter(this.codeGenerator.eleUtils.elementIdParam())
			.addStatement("return $T.ok(this.repository.findById(id))", ResponseEntity.class)
			.build();

		MethodSpec all = filter.fields().length > 0 ? this.filterableAll(filter) : this.simpleAll();

		MethodSpec delete = this.createMethod("delete")
			.addParameter(this.codeGenerator.eleUtils.elementIdParam())
			.addStatement("this.repository.deleteById(id)")
			.addStatement("return new $T($T.NO_CONTENT)", ResponseEntity.class, HttpStatus.class)
			.build();

		return TypeSpec.classBuilder(prefix + this.codeGenerator.serSuffix())
			.addModifiers(Modifier.PUBLIC)
			.addField(codeGenerator.autowire("repository", prefix + this.codeGenerator.repSuffix()))
			.addAnnotation(Component.class)
			.addMethod(save)
			.addMethod(one)
			.addMethod(all)
			.addMethod(delete)
			.build();
	}


	private MethodSpec simpleAll() {

		MethodSpec.Builder builder = this.createMethod("all");

		if (!this.pagination)
			builder.addStatement("return $T.ok(this.repository.findAll())", ResponseEntity.class);
		else {
			addPageability(builder);
			builder
				.addStatement("return $T.ok(this.repository.findAll(pageable))", ResponseEntity.class);
		}

		return builder
			.build();
	}

	private MethodSpec filterableAll(Filter filter) {

		String[] fields = filter.fields();

		if (fields.length == 1 && "*".equals(fields[0])) {

			fields = codeGenerator.eleUtils
				.getNamesWithoutAnnotationByKind(Transient.class, ElementKind.FIELD)
				.toArray(new String[]{});
		}

		MethodSpec.Builder builder = this.addArguments(this.createMethod("allByFilter"), fields);

		if (this.pagination)
			addPageability(builder);

		return builder
			.addStatement(getReturn(fields), ResponseEntity.class)
			.build();
	}

	private String getReturn(String[] fields) {

		String r = "return $T.ok(this.repository.allByFilter(";
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			r += field;
			if (i < fields.length - 1)
				r += ", ";
		}

		if (this.pagination)
			r += ", pageable";

		r += "))";
		return r;
	}

	private MethodSpec.Builder addArguments(MethodSpec.Builder builder, String[] fields) {


		for(int i = 0; i < fields.length; i++) {

			String param = fields[i];
			String paramType = codeGenerator.eleUtils.getEnclosingElement(param).asType().toString();

			builder.addParameter(
				codeGenerator.parUtils.build(
					param,
					codeGenerator.getTypeName(paramType, "")));
		}

		return builder;
	}

	private void addPageability(MethodSpec.Builder builder) {

		builder.addParameter(codeGenerator.parUtils.build("page", Integer.class))
			.addParameter(codeGenerator.parUtils.build("pageSize", Integer.class));
		builder.addStatement("$T pageable", Pageable.class)
			.beginControlFlow("if (page == null || pageSize == null)")
			.addStatement("pageable = $T.of(0, $T.MAX_VALUE)", PageRequest.class, Integer.class)
			.nextControlFlow("else")
			.addStatement("pageable = $T.of(page, pageSize)", PageRequest.class)
			.endControlFlow();
	}


	private MethodSpec.Builder createMethod(String name) {

		return MethodSpec.methodBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.returns(ResponseEntity.class);
	}
}
