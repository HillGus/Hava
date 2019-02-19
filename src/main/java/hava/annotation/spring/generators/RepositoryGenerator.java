package hava.annotation.spring.generators;

import com.google.gson.Gson;
import com.squareup.javapoet.*;
import hava.annotation.spring.annotations.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.List;

public class RepositoryGenerator {

	private CodeGenerator codeGenerator;

	public RepositoryGenerator(CodeGenerator codeGenerator) {

		this.codeGenerator = codeGenerator;
	}

	public TypeSpec generate(String prefix, Filter filter, boolean pagination) {

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(prefix + this.codeGenerator.repSuffix())
			.addModifiers(Modifier.PUBLIC)
			.addSuperinterface(getParameterizedTypeName(JpaRepository.class,
				this.codeGenerator.eleUtils.elementType(),
				this.codeGenerator.eleUtils.elementIdType()));

		if (filter.fields().length > 0)
			builder.addMethod(getFilterMethod(filter, pagination));

		return builder.build();
	}


	private MethodSpec getFilterMethod(Filter filter, boolean pagination) {

		String[] fields = filter.fields();

		if (fields.length == 1 && "*".equals(fields[0])) {

			fields = codeGenerator.eleUtils
				.getNamesWithoutAnnotationByKind(Transient.class, ElementKind.FIELD)
				.toArray(new String[]{});
		}

		MethodSpec.Builder builder = addArguments(MethodSpec.methodBuilder("allByFilter"), fields);
		builder.addAnnotation(codeGenerator.annUtils.buildWithValue(Query.class, getFilterQuery(fields, filter.likeType())));
		builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

		if (!pagination) {
			builder.returns(getParameterizedTypeName(List.class, codeGenerator.eleUtils.elementType()));
		} else {
			builder.returns(getParameterizedTypeName(Page.class, codeGenerator.eleUtils.elementType()));
			builder.addParameter(codeGenerator
				.parUtils.build("pageable", Pageable.class));
		}

		return builder.build();
	}

	private String getFilterQuery(String[] fields, Filter.LikeType likeType) {

		String select = "select o from " + codeGenerator.eleUtils.elementType().toString() + " o";

		if (fields.length > 0)
			select += " where ";

		for (int i = 0; i < fields.length; i++) {

			String field = fields[i];
			String line = "";
			String fieldType = codeGenerator.eleUtils.getEnclosingElement(field).asType().toString();

			line += "(:" + field + " is null or ";

			if (fieldType.equals("java.lang.String")) {
				line += "LOWER(o." + field + ") like LOWER(CONCAT(";

				if (likeType == Filter.LikeType.START || likeType == Filter.LikeType.BOTH)
					line += "'%', ";

				line += ":" + field;

				if (likeType == Filter.LikeType.END || likeType == Filter.LikeType.BOTH)
					line += ", '%'";

				line += ")))";
			} else {
				line += "o." + field + " = :" + field + ")";
			}

			if (i < fields.length - 1)
				line += " and ";

			select += line;
		}

		return select;
	}

	private MethodSpec.Builder addArguments(MethodSpec.Builder builder, String[] fields) {

		for (String field : fields) {

			String fieldType = codeGenerator.eleUtils.getEnclosingElement(field).asType().toString();

			builder.addParameter(
				codeGenerator.parUtils.build(field,
					codeGenerator.getTypeName(fieldType, ""),
					codeGenerator.annUtils.buildWithValue(Param.class, field)));
		}

		return builder;
	}

	private ParameterizedTypeName getParameterizedTypeName(Class<?> className, TypeName... types) {

		try {
			ParameterizedTypeName instance = new Gson().fromJson(
				new Gson().toJson(ParameterizedTypeName.get(JpaRepository.class)), ParameterizedTypeName.class);
			this.codeGenerator.refUtils.setValues(
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
}
