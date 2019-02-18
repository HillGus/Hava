package hava.annotation.spring;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Id;
import javax.tools.Diagnostic.Kind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class CodeGenerator {

  
  private Filer filer;
  private Messager messager;
  private Types typeUtils;
  private Elements elementUtils;
  
  private TypeName elementType;
  private TypeName elementIdType;
  
  private boolean debug = true;
  
  
  public CodeGenerator(Elements elementUtils, Types typeUtils, Messager messager, Filer filer) {
    
    this.filer = filer;
    this.messager = messager;
    this.typeUtils = typeUtils;
    this.elementUtils = elementUtils;
  }
  
  
  public enum Layer {
    REPOSITORY, SERVICE, CONTROLLER;
  };
  
  public interface ReturnableRunnable {
    
    <T> T run();
  }
  
  
  public void generateClasses(Element element, String type, boolean isAnnotated) throws RuntimeException {
    
    Layer startFrom = type.equals("Entity") ? Layer.REPOSITORY : type.equals("Repository") ? Layer.SERVICE : type.equals("Service") ? Layer.SERVICE : type.equals("Controller") ? Layer.CONTROLLER : null;
    
    if (startFrom == null) {
      this.messager.printMessage(Kind.ERROR, "Invalid type: " + type, element);
      throw new RuntimeException("Invalid type: " + type);
    }
    
    final String elementName = element.getSimpleName().toString();
    String prefix = isAnnotated ? elementName : elementName.substring(0, elementName.length() - type.length());
    String packageName = this.elementUtils.getPackageOf(element).getQualifiedName().toString();
   
    this.elementType = getTypeName(element.getSimpleName().toString(), "Cound not find class");
    
    switch (startFrom) {
      case REPOSITORY: generateRepository(element, prefix, packageName);
      case SERVICE: generateService(prefix, packageName);
      case CONTROLLER: generateController(prefix, packageName);
    }
  }
  
  private void generateRepository(Element element, String prefix, String packageName) throws RuntimeException {
    
    TypeName elementIdType = null;
    
    for (Element el : element.getEnclosedElements()) {
      
      if (el.getAnnotation(Id.class) != null)
        try {
          elementIdType = getTypeName(el.getSimpleName().toString(), "Could not get class name of " + element.getSimpleName().toString());
        } catch (Exception e) {}
    }
    
    if (elementIdType == null)
      throw new RuntimeException("A Entity must have a field annotated with \"Id\"");
    
    this.elementIdType = elementIdType;
    
    TypeSpec repositorySpec = TypeSpec.interfaceBuilder(prefix + "Repository")
        .addSuperinterface(getParameterizedTypeName(ClassName.get(JpaRepository.class), elementType, elementIdType))
        .build();
    
    save(repositorySpec, packageName);
  }
  
  private void generateService(String prefix, String packageName) throws RuntimeException {
        
    
    MethodSpec save = MethodSpec.methodBuilder("save")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(this.elementType, "entity").build())
        .returns(ResponseEntity.class)
        .addStatement("return $T.ok(this.repository.save(entity))", ResponseEntity.class)
        .build();
    
    MethodSpec one = MethodSpec.methodBuilder("one")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(this.elementIdType, "id").build())
        .returns(ResponseEntity.class)
        .addStatement("return $T.ok(this.repository.findById(id))", ResponseEntity.class)
        .build();
    
    MethodSpec all = MethodSpec.methodBuilder("all")
        .addModifiers(Modifier.PUBLIC)
        .returns(ResponseEntity.class)
        .addStatement("return $T.ok(this.repository.findAll())", ResponseEntity.class)
        .build();
    
    MethodSpec delete = MethodSpec.methodBuilder("delete")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(this.elementIdType, "id").build())
        .returns(ResponseEntity.class)
        .addStatement("this.repository.deleteById(id)")
        .addStatement("return $T.noContent().build()", ResponseEntity.class)
        .build();
    
    TypeSpec serviceSpec = TypeSpec.classBuilder(prefix + "Service")
        .addField(autowire("repository", packageName + "." + prefix + "Repository", "Could not autowire repository field using class " + prefix + "Repository"))
        .addMethod(save)
        .addMethod(one)
        .addMethod(all)
        .addMethod(delete)
        .build();
    
    save(serviceSpec, packageName);
  }
  
  private void generateController(String prefix, String packageName) throws RuntimeException {
    
    MethodSpec one = MethodSpec.methodBuilder("one")
        .addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "\"{id}\"").build())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(this.elementIdType, "id").addAnnotation(AnnotationSpec.builder(PathVariable.class).addMember("value", "\"id\"").build()).build())
        .addStatement("return this.service.one(id)")
        .returns(ResponseEntity.class)
        .build();
    
    MethodSpec all = MethodSpec.methodBuilder("all")
        .addAnnotation(GetMapping.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return this.service.all()")
        .returns(ResponseEntity.class)
        .build();
    
    MethodSpec save = MethodSpec.methodBuilder("save")
        .addAnnotation(PostMapping.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(this.elementType, "entity").addAnnotation(RequestBody.class).build())
        .addStatement("return this.service.save(entity)")
        .returns(ResponseEntity.class)
        .build();
    
    MethodSpec delete = MethodSpec.methodBuilder("delete")
        .addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "\"{id}\"").build())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(this.elementIdType, "id").addAnnotation(AnnotationSpec.builder(PathVariable.class).addMember("value", "\"id\"").build()).build())
        .addStatement("return this.service.delete(id)")
        .returns(ResponseEntity.class)
        .build();
    
    TypeSpec controllerSpec = null;
    
    controllerSpec = TypeSpec.classBuilder(prefix + "Controller")
        .addAnnotation(AnnotationSpec.builder(RestController.class).addMember("value", "\"/" + prefix.toLowerCase() + "\"").build())
        .addField(autowire("service", packageName + "." + prefix + "Service", "Could not autowire service field using class " + prefix + "Service"))
        .addMethod(one)
        .addMethod(all)
        .addMethod(save)
        .addMethod(delete)
        .build();
    
    save(controllerSpec, packageName);
  }
  
  private void save(TypeSpec spec, String packageName) throws RuntimeException {
    
    JavaFile file = JavaFile.builder(packageName, spec).build();
    
    try {
      if (debug) {
        System.out.println("===================================\n");
        file.writeTo(System.out);
        System.out.println("===================================\n");
      }
        
      file.writeTo(this.filer);
    } catch (IOException e) {
      throw new RuntimeException("Could not write class to filer: " + e.getMessage());
    }
  }
  
  
  private FieldSpec autowire(String fieldName, String typeName, String exceptionMessage) {
    
    TypeName type = getTypeName(typeName, exceptionMessage);
    
    return FieldSpec.builder(type, fieldName, Modifier.PRIVATE)
        .addAnnotation(Autowired.class).build();
  }
  
  private TypeName getTypeName(String typeName, String exceptionMessage) {
    
    Class<TypeName> typeNameClass = TypeName.class;
    Constructor<TypeName> typeConstructor;
    try {
      typeConstructor = typeNameClass.getDeclaredConstructor(String.class);
      typeConstructor.setAccessible(true);
      return typeConstructor.newInstance(typeName);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      
      throw new RuntimeException(exceptionMessage + ": " + e.getMessage());
    } 
  }
  
  private ParameterizedTypeName getParameterizedTypeName(ClassName className, TypeName... types) {
    
    Class<ParameterizedTypeName> cls = ParameterizedTypeName.class;
    
    try {
      cls.getField("rawType").setAccessible(true);
      cls.getField("typeArguments").setAccessible(true);
      ParameterizedTypeName instance = cls.newInstance();
      cls.getField("rawType").set(instance, className);
      cls.getField("typeArguments").set(instance, Arrays.asList(types));
      return instance;
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
        | SecurityException | InstantiationException e) {
      
      e.printStackTrace();
      throw new RuntimeException("Could not get ParameterizedTypeName for " + className.simpleName() + " using generic arguments " + Arrays.asList(types) + ": " + e.getMessage());
    }
  }
}