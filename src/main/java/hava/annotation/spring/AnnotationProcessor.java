package hava.annotation.spring;

import com.google.auto.service.AutoService;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.generators.CodeGenerator;
import org.springframework.util.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Entity;
import javax.tools.Diagnostic.Kind;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {


  private Filer filer;
  private Messager messager;
  private Types typeUtils;
  private Elements elementUtils;
  private CodeGenerator codeGenerator;


  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
    this.typeUtils = processingEnv.getTypeUtils();
    this.elementUtils = processingEnv.getElementUtils();
    
    this.codeGenerator = new CodeGenerator(this.elementUtils, this.typeUtils, this.messager, this.filer);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {

    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {

    Set<String> annotations = new LinkedHashSet<>();

    annotations.add(CRUD.class.getCanonicalName());
    annotations.add(HASConfiguration.class.getCanonicalName());

    return annotations;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    for (Element el : roundEnv.getElementsAnnotatedWith(HASConfiguration.class)) {

      HASConfiguration config = el.getAnnotation(HASConfiguration.class);

      this.codeGenerator.setSuffixes(config.suffixes());
      this.codeGenerator.setDebug(config.debug());
    }

    for (Element element : roundEnv.getElementsAnnotatedWith(CRUD.class)) {

      if (element.getAnnotationsByType(Entity.class).length == 0) {
        this.messager.printMessage(Kind.ERROR, "A element annotated with CRUD must be annotated with \"javax.persistence.Entity\"");
        return false;
      }

      try {
        this.codeGenerator.generateClasses(element);
      } catch (RuntimeException e) {

        e.printStackTrace();
        this.messager.printMessage(Kind.ERROR, "An exception occurred while generating code: " + e.getMessage(), element);
      }
    }
    
    return true;
  }

  /*private void generateCode(TypeElement element) throws IOException {

    MethodSpec method = MethodSpec.methodBuilder("all").addModifiers(Modifier.PUBLIC)
        .returns(String.class).addAnnotation(GetMapping.class).addAnnotation(ResponseBody.class)
        .addStatement("return $S", "Teste").build();
    
    TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC).addMethod(method)
        .addAnnotation(
            AnnotationSpec.builder(RestController.class).addMember("value", "\"/teste\"").build());

    element.getEnclosedElements().stream().forEach(el -> {
      if (el.getKind() == ElementKind.METHOD) {}
    });
    
    TypeSpec clazz = builder.build();
    
    JavaFile javaFile = JavaFile.builder(
        this.elementUtils.getPackageOf(element).getQualifiedName().toString(), 
        clazz).build();

    javaFile.writeTo(System.out);

    javaFile.writeTo(this.filer);
  }*/
}
