package hava.annotation.spring;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParameterUtils {


	public ParameterSpec build(String paramName, TypeName typeName) {

		return this.build(paramName, typeName, new AnnotationSpec[]{});
	}

	public ParameterSpec build(String paramName, TypeName typeName, Class<? extends Annotation>... annotations) {

		List<AnnotationSpec> annotationSpecs = Arrays.stream(annotations).map(
			annotation -> {
				return AnnotationSpec.builder(annotation).build();
			}).collect(Collectors.toList());

		return this.build(paramName, typeName, annotationSpecs.toArray(new AnnotationSpec[]{}));
	}

	public ParameterSpec build(String paramName, TypeName typeName, AnnotationSpec... annotations) {

		ParameterSpec.Builder builder = ParameterSpec.builder(typeName, paramName);

		Arrays.stream(annotations).forEach(builder::addAnnotation);

		return builder.build();
	}
}
