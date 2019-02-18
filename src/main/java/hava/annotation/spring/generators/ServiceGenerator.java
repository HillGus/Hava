package hava.annotation.spring.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.annotations.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypesException;

public class ServiceGenerator {


	private CodeGenerator codeGenerator;

	public ServiceGenerator(CodeGenerator codeGenerator) {

		this.codeGenerator = codeGenerator;
	}

	public TypeSpec generate(String prefix, Filter filter) {

		MethodSpec save = this.createMethod("save")
			.addParameter(this.codeGenerator.elementParam())
			.addStatement("return $T.ok(this.repository.save(entity))", ResponseEntity.class)
			.build();

		MethodSpec one = this.createMethod("one")
			.addParameter(this.codeGenerator.elementIdParam())
			.addStatement("return $T.ok(this.repository.findById(id))", ResponseEntity.class)
			.build();

		MethodSpec all = filter.parameters().length > 0 ? this.filterableAll(filter) : this.simpleAll();

		MethodSpec delete = this.createMethod("delete")
			.addParameter(this.codeGenerator.elementIdParam())
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

		return this.createMethod("all")
			.addStatement("return $T.ok(this.repository.findAll())", ResponseEntity.class)
			.build();
	}

	private MethodSpec filterableAll(Filter filter) {

		return this.addArguments(this.createMethod("all"), filter)
			.addStatement(getReturn(filter.parameters()), ResponseEntity.class)
			.build();
	}

	private String getReturn(String[] fields) {

		String r = "return this.repository.all(";
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			r += field;
			if (i < fields.length - 1)
				r += ", ";
		}
		r += ")";
		return r;
	}

	private MethodSpec.Builder addArguments(MethodSpec.Builder builder, Filter filter) {


		for(int i = 0; i < filter.parameters().length; i++) {

			String param = filter.parameters()[i];

			String paramType = null;

			try {
				paramType = filter.parametersTypes()[i].getCanonicalName();
			} catch (MirroredTypesException e) {

				paramType = e.getTypeMirrors().get(i).toString();
			}

			builder.addParameter(
				codeGenerator.parUtils.build(
					param,
					codeGenerator.getTypeName(paramType, "")));
		}

		return builder;
	}


	private MethodSpec.Builder createMethod(String name) {

		return MethodSpec.methodBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.returns(ResponseEntity.class);
	}
}
