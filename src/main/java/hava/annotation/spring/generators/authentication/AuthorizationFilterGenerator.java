package hava.annotation.spring.generators.authentication;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.generators.CodeGenerator;
import hava.annotation.spring.utils.MiscUtils;

import javax.lang.model.element.Modifier;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationFilterGenerator {


	private ParameterBuilder parBuilder;
	private MiscUtils miscUtils;

	private String classesPrefix;
	private String packageName;

	private final String webAuthPackage = "org.springframework.security.web.authentication.www";
	private final String securityAuthPackage = "org.springframework.security.authentication";
	private final String securityCoreContextPackage = "org.springframework.security.core.context";
	private final String securityUserDetailsPackage = "org.springframework.security.core.userdetails";

	public AuthorizationFilterGenerator(CodeGenerator codeGenerator, String classesPrefix, String packageName) {

		this.miscUtils = codeGenerator.miscUtils;
		this.parBuilder = codeGenerator.parBuilder;
		this.classesPrefix = classesPrefix;
		this.packageName = packageName;
	}

	public TypeSpec generate() {

		MethodSpec constructor = MethodSpec.constructorBuilder()
			.addParameter(
				this.parBuilder.build(
					"authManager",
					ClassName.get(this.securityAuthPackage, "AuthenticationManager")))
			.addParameter(
				this.parBuilder.build(
					"jwtUtil",
					ClassName.get(this.packageName, this.classesPrefix + "JWTUtil")))
			.addParameter(
				this.parBuilder.build(
					"userDetailsService",
					ClassName.get(securityUserDetailsPackage, "UserDetailsService")))
			.addStatement("super(authManager)")
			.addStatement("this.jwtUtil = jwtUtil")
			.addStatement("this.userDetailsService = userDetailsService")
			.build();

		MethodSpec doFiterInternal = MethodSpec.methodBuilder("doFilterInternal")
			.addModifiers(Modifier.PROTECTED)
			.addAnnotation(Override.class)
			.addParameter(this.parBuilder.build("request", HttpServletRequest.class))
			.addParameter(this.parBuilder.build("response", HttpServletResponse.class))
			.addParameter(this.parBuilder.build("chain", FilterChain.class))
			.addException(IOException.class)
			.addException(ServletException.class)
			.addStatement("$T header = request.getHeader($S)", String.class, "Authorization")
			.addStatement("$T auth = null",
				ClassName.get(this.securityAuthPackage, "UsernamePasswordAuthenticationToken"))
			.beginControlFlow(
				"if ((header != null && !header.isEmpty()) && header.startsWith($S))", "Bearer ")
				.addStatement("auth = this.getAuthentication(header.substring(7))")
			.endControlFlow()
			.addStatement("$T.getContext().setAuthentication(auth)",
				ClassName.get(this.securityCoreContextPackage, "SecurityContextHolder"))
			.addStatement("chain.doFilter(request, response)")
			.build();

		MethodSpec getAuthentication = MethodSpec.methodBuilder("getAuthentication")
			.addModifiers(Modifier.PRIVATE)
			.addParameter(this.parBuilder.build("token", String.class))
			.addStatement("$T auth = null",
				ClassName.get(this.securityAuthPackage, "UsernamePasswordAuthenticationToken"))
			.beginControlFlow("if (this.jwtUtil.validToken(token))")
				.addStatement("$T username = this.jwtUtil.getUsername(token)", String.class)
				.beginControlFlow("try")
					.addStatement("$T user = this.userDetailsService.loadUserByUsername(username)",
						ClassName.get(this.securityUserDetailsPackage, "UserDetails"))
					.addStatement("auth = new $T(user, null, user.getAuthorities())",
						ClassName.get(this.securityAuthPackage, "UsernamePasswordAuthenticationToken"))
				.nextControlFlow("catch ($T e)",
					ClassName.get(this.securityUserDetailsPackage, "UsernameNotFoundException"))
				.endControlFlow()
			.endControlFlow()
			.addStatement("return auth")
			.returns(ClassName.get(this.securityAuthPackage, "UsernamePasswordAuthenticationToken"))
			.build();

		FieldSpec jwtUtilField = FieldSpec.builder(
			ClassName.get(this.packageName, this.classesPrefix + "JWTUtil"),
			"jwtUtil",
			Modifier.PRIVATE)
			.build();

		FieldSpec udsField = FieldSpec.builder(
			ClassName.get(this.securityUserDetailsPackage, "UserDetailsService"),
			"userDetailsService",
			Modifier.PRIVATE)
			.build();

		return TypeSpec.classBuilder(this.classesPrefix + "JWTAuthorizationFilter")
			.superclass(ClassName.get(this.webAuthPackage, "BasicAuthenticationFilter"))
			.addField(jwtUtilField)
			.addField(udsField)
			.addMethod(constructor)
			.addMethod(doFiterInternal)
			.addMethod(getAuthentication)
			.build();
	}
}
