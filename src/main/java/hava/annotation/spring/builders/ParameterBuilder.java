package hava.annotation.spring.builders;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import hava.annotation.spring.utils.MiscUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParameterBuilder {


	private MiscUtils miscUtils = new MiscUtils();


	public ParameterSpec build(String paramName, Class<?> type) {

		return this.build(paramName, this.miscUtils.getTypeName(type));
	}

	public ParameterSpec build(String paramName, Class<?> type, AnnotationSpec... annotations) {

		return this.build(paramName,
			this.miscUtils.getTypeName(type),
			annotations);
	}

	public ParameterSpec build(String paramName, TypeName typeName) {

		return this.build(paramName, typeName, new AnnotationSpec[]{});
	}

	public ParameterSpec build(String paramName, TypeName typeName, Class<? extends Annotation>... annotations) {

		List<AnnotationSpec> annotationSpecs = Arrays.stream(annotations)
			.map(annotation -> AnnotationSpec.builder(annotation).build())
			.collect(Collectors.toList());

		return this.build(paramName, typeName, annotationSpecs.toArray(new AnnotationSpec[]{}));
	}

	public ParameterSpec build(String paramName, TypeName typeName, AnnotationSpec... annotations) {

		ParameterSpec.Builder builder = ParameterSpec.builder(typeName, paramName);

		Arrays.stream(annotations).forEach(builder::addAnnotation);

		return builder.build();
	}
}
