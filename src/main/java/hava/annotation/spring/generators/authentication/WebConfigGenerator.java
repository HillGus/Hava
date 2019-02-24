package hava.annotation.spring.generators.authentication;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.configurators.AuthenticationConfigurator;
import hava.annotation.spring.generators.CodeGenerator;
import hava.annotation.spring.utils.MiscUtils;
import org.springframework.context.annotation.Configuration;

import javax.lang.model.element.Modifier;

public class WebConfigGenerator {


	private ParameterBuilder parBuilder;
	private MiscUtils miscUtils;

	private String classesPrefix;

	private final String httpSecurityPackage = "org.springframework.security.config.annotation.web.builders";
	private final String webConfigPackage = "org.springframework.security.config.annotation.web.configuration";
	private final String authBuildersPackage = "org.springframework.security.config.annotation.authentication.builders";

	public WebConfigGenerator(CodeGenerator codeGenerator, String classesPrefix) {

		this.miscUtils = codeGenerator.miscUtils;
		this.parBuilder = codeGenerator.parBuilder;
		this.classesPrefix = classesPrefix;
	}

	public TypeSpec generate() {

		MethodSpec configureHttp = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(Exception.class)
			.addParameter(
				this.parBuilder.build("http",
					ClassName.get(httpSecurityPackage, "HttpSecurity")))
			.addStatement("http.cors().and().csrf().disable()")
			.addStatement("this.authenticationConfig.configure(http, authenticationManager())")
			.addStatement("http.authorizeRequests().anyRequest().authenticated().antMatchers(\"/login\").permitAll()")
			.build();

		MethodSpec configureAuth = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(Exception.class)
			.addParameter(
				this.parBuilder.build("auth",
					ClassName.get(authBuildersPackage, "AuthenticationManagerBuilder")))
			.addStatement("this.authenticationConfig.configure(auth)")
			.build();

		FieldSpec authConfig = this.miscUtils.autowire("authenticationConfig", AuthenticationConfigurator.class.getCanonicalName());
		
		TypeSpec classe = TypeSpec.classBuilder(this.classesPrefix + "WebSecurityConfigurer")
			.addAnnotation(Configuration.class)
			.addAnnotation(ClassName.get(this.webConfigPackage, "EnableWebSecurity"))
			.superclass(ClassName.get(this.webConfigPackage, "WebSecurityConfigurerAdapter"))
			.addField(authConfig)
			.addMethod(configureHttp)
			.addMethod(configureAuth)
			.build();

		return classe;
	}
}