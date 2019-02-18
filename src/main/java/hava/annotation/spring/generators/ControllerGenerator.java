package hava.annotation.spring.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.annotations.Filter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypesException;

public class ControllerGenerator {


	private CodeGenerator codeGenerator;

	public ControllerGenerator(CodeGenerator codeGenerator) {

		this.codeGenerator = codeGenerator;
	}

	public TypeSpec generate(String prefix, String endpoint, Filter filter) {

		MethodSpec one = MethodSpec.methodBuilder("one")
			.addAnnotation(this.codeGenerator.annUtils.getMapping("{id}"))
			.addParameter(this.codeGenerator.elementIdPathParam())
			.addStatement("return this.service.one(id)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec all = filter.parameters().length > 0 ? filterableAll(filter) : simpleAll();

		MethodSpec save = MethodSpec.methodBuilder("save")
			.addAnnotation(this.codeGenerator.annUtils.postMapping(""))
			.addParameter(this.codeGenerator.elementReqBodyParam())
			.addStatement("return this.service.save(entity)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec delete = MethodSpec.methodBuilder("delete")
			.addAnnotation(this.codeGenerator.annUtils.deleteMapping("{id}"))
			.addParameter(this.codeGenerator.elementIdPathParam())
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

		return MethodSpec.methodBuilder("all")
			.addAnnotation(this.codeGenerator.annUtils.getMapping(""))
			.addStatement("return $T.ok(this.repository.findAll())", ResponseEntity.class)
			.returns(ResponseEntity.class)
			.build();
	}

	private MethodSpec filterableAll(Filter filter) {

		MethodSpec.Builder builder = MethodSpec.methodBuilder("all")
			.addAnnotation(this.codeGenerator.annUtils.getMapping(""));

		return this.addArguments(builder, filter)
			.addStatement(getReturn(filter.parameters()), ResponseEntity.class)
			.returns(ResponseEntity.class)
			.build();
	}

	private String getReturn(String[] fields) {

		String r = "return this.service.all(";
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
					codeGenerator.getTypeName(paramType, ""),
					codeGenerator.annUtils.build(RequestParam.class,
						new Object[]{"required", "$L", false},
						new Object[]{"value", "$S", param})));
		}

		return builder;
	}
}
