package hava.annotation.spring.utils;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import hava.annotation.spring.generators.CodeGenerator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Types;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class ElementUtils {


	private Element element;
	private TypeName elementType;
	private TypeName elementIdType = null;

	private ParameterUtils parUtils = new ParameterUtils();
	private AnnotationUtils annUtils = new AnnotationUtils();
	private Types typeUtils;

	public ElementUtils(Types typeUtils) {

		this.typeUtils = typeUtils;
	}

	public ElementUtils(Element element) {

		this.element = element;

		this.elementType = new CodeGenerator()
			.getTypeName(element.getSimpleName().toString(),
				"Cound not find class");

		for (Element el : this.element.getEnclosedElements()) {

			if (el.getAnnotation(Id.class) != null) {
				this.elementIdType = new CodeGenerator().getTypeName(el.asType().toString(),
					"Could not get class name of " + element.getSimpleName().toString());
			}
		}

		if (this.elementIdType == null)
			throw new RuntimeException("A Entity must have a field annotated with javax.persistence.Id");
	}

	public Element getEnclosingElement(String name) {

		if (name == null)
			return null;

		for (Element element : this.element.getEnclosedElements()) {

			if (name.equals(element.getSimpleName().toString()))
				return element;
		}

		return null;
	}

	public Element getElement() {

		return this.element;
	}

	public List<? extends Element> getEnclosedElements() {

		return this.element.getEnclosedElements();
	}

	public List<? extends Element> getAllElementsWithoutAnnotationByKind(Class<? extends Annotation> annotation, ElementKind kind) {

		return getEnclosedElements().stream()
			.filter(el -> el.getAnnotationsByType(annotation).length == 0
				 && ((Element) el).getKind() == kind)
			.collect(Collectors.toList());
	}

	public List<String> getNamesWithoutAnnotationByKind(Class<? extends Annotation> annotation, ElementKind kind) {

		return getAllElementsWithoutAnnotationByKind(Transient.class, kind).stream()
			.map(el -> el.getSimpleName().toString())
			.collect(Collectors.toList());
	}

	public TypeName elementType() {

		return this.elementType;
	}

	public TypeName elementIdType() {

		return this.elementIdType;
	}

	public ParameterSpec elementParam() {

		return this.parUtils.build("entity", this.elementType);
	}

	public ParameterSpec elementReqBodyParam() {

		return this.parUtils.build("entity", this.elementType, RequestBody.class);
	}

	public ParameterSpec elementIdParam() {

		return this.parUtils.build("id", this.elementIdType);
	}

	public ParameterSpec elementIdPathParam() {

		return this.parUtils.build("id", this.elementIdType, this.annUtils.buildWithValue(PathVariable.class, "id"));
	}
}
