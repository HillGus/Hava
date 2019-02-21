package hava.annotation.spring.generators.authentication;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.generators.CodeGenerator;
import hava.annotation.spring.utils.MiscUtils;
import org.springframework.context.annotation.Configuration;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class WebConfigGenerator {


	private ParameterBuilder parBuilder;
	private MiscUtils miscUtils;

	private String classesPrefix;
	private String packageName;

	private final String httpSecurityPackage = "org.springframework.security.config.annotation.web.builders";
	private final String webConfigPackage = "org.springframework.security.config.annotation.web.configuration";
	private final String authBuildersPackage = "org.springframework.security.config.annotation.authentication.builders";
	private final String securityUserDetailsPackage = "org.springframework.security.core.userdetails";

	public WebConfigGenerator(CodeGenerator codeGenerator, String classesPrefix, String packageName) {

		this.miscUtils = codeGenerator.miscUtils;
		this.parBuilder = codeGenerator.parBuilder;
		this.classesPrefix = classesPrefix;
		this.packageName = packageName;
	}

	public TypeSpec generate(TypeMirror passwordEncoder, boolean useEncoderGetInstance) {

		MethodSpec configureHttp = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(Exception.class)
			.addParameter(
				this.parBuilder.build("http",
					ClassName.get(httpSecurityPackage, "HttpSecurity")))
			.addStatement(
				"http.addFilter(new $L(authenticationManager(), jwtUtil))",
				ClassName.get(this.packageName, this.classesPrefix + "JWTAuthenticationFilter"))
			.addStatement(
				"http.addFilter(new $L(authenticationManager(), jwtUtil, userDetailsService))",
				ClassName.get(this.packageName, this.classesPrefix + "JWTAuthorizationFilter"))
			.build();

		MethodSpec configureAuth = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(Exception.class)
			.addParameter(
				this.parBuilder.build("auth",
					ClassName.get(authBuildersPackage, "AuthenticationManagerBuilder")))
			.addStatement("auth.userDetailsService(this.userDetailsService).passwordEncoder(this.getPasswordEncoder())")
			.build();

		MethodSpec getPasswordEncoder = generateGetPasswordEncoder(passwordEncoder, useEncoderGetInstance);

		TypeSpec classe = TypeSpec.classBuilder(this.classesPrefix + "WebSecurityConfig")
			.addAnnotation(Configuration.class)
			.addAnnotation(ClassName.get(this.webConfigPackage, "EnableWebSecurity"))
			.superclass(ClassName.get(this.webConfigPackage, "WebSecurityConfigurerAdapter"))
			.addField(this.miscUtils.autowire("jwtUtil", this.getUtilCanonicalName()))
			.addField(this.miscUtils.autowire("userDetailsService",
				ClassName.get(this.securityUserDetailsPackage, "UserDetailsService").toString()))
			.addMethod(configureHttp)
			.addMethod(configureAuth)
			.addMethod(getPasswordEncoder)
			.build();
		return classe;
	}

	private MethodSpec generateGetPasswordEncoder(TypeMirror passwordEncoder, boolean useEncoderGetInstance) {

		MethodSpec.Builder builder = MethodSpec.methodBuilder("getPasswordEncoder")
			.addModifiers(Modifier.PRIVATE)
			.returns(ClassName.get(
				"org.springframework.security.crypto.password", "PasswordEncoder"));

		if (useEncoderGetInstance)
			builder.addStatement("return $T.getInstance()", passwordEncoder);
		else {
			builder.addStatement("return $T.class.newInstance()", passwordEncoder)
				.addException(IllegalAccessException.class)
				.addException(InstantiationException.class);
		}

		return builder.build();
	}

	private String getUtilCanonicalName() {

		return this.packageName + "." + this.classesPrefix + "JWTUtil";
	}
}