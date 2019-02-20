package hava.annotation.spring.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.type.TypeMirror;

public class MiscUtils {


	public FieldSpec autowire(String fieldName, String typeName) {

		StringBuilder packageName = new StringBuilder();
		String objectName = "";

		String[] divisoes = typeName.split("\\.");
		for (int i = 0; i < divisoes.length; i++) {

			String div = divisoes[i];

			if (i < divisoes.length - 1) {
				packageName.append(div);
				if (i < divisoes.length - 2)
					packageName.append('.');
			} else {
				objectName = div;
			}
		}

		TypeName type = ClassName.get(packageName.toString(), objectName);

		return FieldSpec.builder(type, fieldName)
			.addAnnotation(Autowired.class).build();
	}

	public TypeName getTypeName(TypeMirror type) {

		return TypeName.get(type);
	}

	public TypeName getTypeName(Class<?> typeClass) {

		return ClassName.get(typeClass);
	}
}
