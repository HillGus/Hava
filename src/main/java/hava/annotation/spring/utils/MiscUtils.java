package hava.annotation.spring.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.type.TypeMirror;

public class MiscUtils {


	public FieldSpec autowire(String fieldName, String typeName) {

		String[] names = this.splitQualifiedName(typeName);

		String packageName = names[0];
		String objectName = names[1];

		TypeName type = ClassName.get(packageName, objectName);

		return FieldSpec.builder(type, fieldName)
			.addAnnotation(Autowired.class).build();
	}

	public String[] splitQualifiedName(String qualifiedName) {

		StringBuilder packageName = new StringBuilder();
		String objectName = "";

		String[] divisoes = qualifiedName.split("\\.");
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

		return new String[]{packageName.toString(), objectName};
	}

	public TypeName getTypeName(TypeMirror type) {

		return TypeName.get(type);
	}

	public TypeName getTypeName(Class<?> typeClass) {

		return ClassName.get(typeClass);
	}
}
