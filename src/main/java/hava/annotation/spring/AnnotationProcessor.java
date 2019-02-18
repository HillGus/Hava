package hava.annotation.spring;

import com.google.auto.service.AutoService;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.generators.CodeGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Entity;
import javax.tools.Diagnostic.Kind;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {


  private Filer filer;
  private Messager messager;
  private Types typeUtils;
  private Elements elementUtils;
  private CodeGenerator codeGenerator;
  

  private HashMap<String, Class<? extends Annotation>[]> identifiers = new HashMap<>();

  {
    identifiers.put("Controller", toArray(RestController.class, Controller.class));
    identifiers.put("Service", toArray(Service.class, hava.annotation.spring.annotations.Service.class));
    identifiers.put("Entity", toArray(Entity.class));
    identifiers.put("Repository", toArray(Repository.class, hava.annotation.spring.annotations.Repository.class));
  }
  
  private Class<? extends Annotation>[] toArray(Class<? extends Annotation>... t) {
    
    return (Class<? extends Annotation>[]) Arrays.asList(t).toArray();
  }


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

    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(CRUD.class)) {

      String type = "";
      boolean isAnnotated = false;
      
      for (Entry<String, Class<? extends Annotation>[]> entrada : this.identifiers.entrySet()) {
        
        if (annotatedElement.getSimpleName().toString().endsWith(entrada.getKey())) {
          type = entrada.getKey();
        }
        
        for (Class<? extends Annotation> annotation : entrada.getValue()) {
          
          if (annotatedElement.getAnnotation(annotation) != null) {
            type = entrada.getKey();
            isAnnotated = true;
            break;
          }
        }
      }
      
      if (StringUtils.isEmpty(type)) {
        this.messager.printMessage(Kind.ERROR, "A element that is annotated with CRUD must either: \n"
                                             + "Be annotated with: org.springframework.stereotype.Repository, \n"
                                             + "                   hava.annotation.spring.Repository, \n"
                                             + "                   javax.persistence.Entity, \n"
                                             + "                   org.springframework.stereotype.Service, \n"
                                             + "                   hava.annotation.spring.Service, \n"
                                             + "                   org.springframework.stereotype.Controller or \n"
                                             + "                   org.springframework.web.bind.annotation.RestController, \n"
                                             + "or have it's class name ending with: \"Entity\", \"Service\", \"Controller\" or \"Repository\".", annotatedElement);
        return false;
      }

      try {
        this.codeGenerator.generateClasses(annotatedElement, type, isAnnotated);
      } catch (RuntimeException e) {
       
        this.messager.printMessage(Kind.ERROR, "An exception occurred while generating code: " + e.getMessage(), annotatedElement);
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
