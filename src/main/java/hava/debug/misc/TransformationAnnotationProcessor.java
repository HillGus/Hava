package hava.debug.misc;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import hava.debug.annotation.TransformationTree;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class TransformationAnnotationProcessor extends AbstractProcessor {

	private Elements eleUtils;
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		this.eleUtils = processingEnv.getElementUtils();
		this.filer = processingEnv.getFiler();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {

		Set<String> supported = new HashSet<>();
		supported.add(TransformationTree.class.getCanonicalName());

		return supported;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {

		return SourceVersion.RELEASE_8;
	}


	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if (roundEnv.processingOver())
			return true;

		String packageName = TransformationUtils.class.getPackage().getName();

		TypeSpec.Builder instrumentationUtilsBuilder = TypeSpec.classBuilder("InstrumentationTreeUtils")
			.addModifiers(Modifier.PUBLIC);

		MethodSpec isInTrasformationTree = MethodSpec.methodBuilder("isInTransformationTree")
			.addModifiers(Modifier.PUBLIC)
			.returns(TypeName.BOOLEAN)
			.addParameter(String.class, "className")
			.addStatement("className = className.replace('/', '.')")
			.beginControlFlow("for ($T pkg : transformationTree)", String.class)
				.beginControlFlow("if (!className.contains(\".\") || className.startsWith(pkg + \".\"))")
					.addStatement("return true")
				.endControlFlow()
			.endControlFlow()
			.addStatement("return false")
			.build();

		final ClassName clsName = ClassName.get(packageName, "InstrumentationTreeUtils");

		MethodSpec getInstance = MethodSpec.methodBuilder("getInstance")
			.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
			.returns(clsName)
			.beginControlFlow("if (instance == null)")
				.addStatement("instance = new $T()", clsName)
			.endControlFlow()
			.addStatement("return instance")
			.build();

		FieldSpec instance = FieldSpec.builder(
			clsName, "instance",
			Modifier.PRIVATE, Modifier.STATIC)
			.initializer("getInstance()")
			.build();

		FieldSpec transformationTree = FieldSpec.builder(
			ParameterizedTypeName.get(ArrayList.class, String.class),
			"transformationTree")
			.addModifiers(Modifier.PRIVATE)
			.build();

		instrumentationUtilsBuilder
			.addField(transformationTree)
			.addField(instance)
			.addMethod(isInTrasformationTree)
			.addMethod(getInstance);

		MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
			.addStatement("this.transformationTree = new $T<>()", ArrayList.class);

		for (Element element : roundEnv.getElementsAnnotatedWith(TransformationTree.class)) {

			String elementPackage = element.getAnnotation(TransformationTree.class).rootPackage();

			if (elementPackage != null && elementPackage.isEmpty())
				elementPackage = this.eleUtils.getPackageOf(element).getQualifiedName().toString();

			constructor.addStatement("transformationTree.add($S)", elementPackage);
		}

		TypeSpec instrumentationUtils = instrumentationUtilsBuilder
			.addMethod(constructor.build())
			.build();

		JavaFile file = JavaFile.builder(packageName, instrumentationUtils).build();

		try {
			file.writeTo(this.filer);
		} catch (IOException e) {

			//throw new RuntimeException("Could not create InstrumentationTreeUtils class: " + e.getMessage());
		}

		return true;
	}
}
