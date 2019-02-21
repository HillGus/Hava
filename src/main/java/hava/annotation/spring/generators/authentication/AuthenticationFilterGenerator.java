package hava.annotation.spring.generators.authentication;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.generators.CodeGenerator;
import hava.annotation.spring.utils.MiscUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AuthenticationFilterGenerator {

	private ParameterBuilder parBuilder;
	private MiscUtils miscUtils;

	private String classesPrefix;
	private String packageName;

	private final String securityAuthPackage = "org.springframework.security.authentication";
	private final String securityCorePackage = "org.springframework.security.core";
	private final String webAuthPackage = "org.springframework.security.web.authentication";

	public AuthenticationFilterGenerator(CodeGenerator codeGenerator, String classesPrefix, String packageName) {

		this.miscUtils = codeGenerator.miscUtils;
		this.parBuilder = codeGenerator.parBuilder;
		this.classesPrefix = classesPrefix;
		this.packageName = packageName;
	}

	public TypeSpec generate() {

		MethodSpec constructor = MethodSpec.constructorBuilder()
			.addParameter(this.parBuilder.build("authManager",
				ClassName.get(this.securityAuthPackage, "AuthenticationManager")))
			.addParameter(this.parBuilder.build("jwtUtil",
				ClassName.get(this.packageName, this.classesPrefix + "JWTUtil")))
			.addStatement("this.authManager = authManager")
			.addStatement("this.jwtUtil = jwtUtil")
			.build();

		MethodSpec attemptAuthentication = MethodSpec.methodBuilder("attemptAuthentication")
			.addParameter(this.parBuilder.build("request", HttpServletRequest.class))
			.addParameter(this.parBuilder.build("response", HttpServletResponse.class))
			.addException(ClassName.get(this.securityCorePackage, "AuthenticationException"))
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.addStatement("$T username = request.getHeader($S)", String.class, "username")
			.addStatement("$T password = request.getHeader($S)", String.class, "password")
			.addStatement("$T authToken = new $T(username, password, new $T())",
				ClassName.get(this.securityAuthPackage, "UsernamePasswordAuthenticationToken"),
				ClassName.get(this.securityAuthPackage, "UsernamePasswordAuthenticationToken"),
				ArrayList.class)
			.addStatement("return this.authManager.authenticate(authToken)")
			.returns(ClassName.get(this.securityCorePackage, "Authentication"))
			.build();

		MethodSpec successfulAuthentication = MethodSpec.methodBuilder("successfulAuthentication")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(IOException.class)
			.addException(ServletException.class)
			.addParameter(this.parBuilder.build("request", HttpServletRequest.class))
			.addParameter(this.parBuilder.build("response", HttpServletResponse.class))
			.addParameter(this.parBuilder.build("chain", FilterChain.class))
			.addParameter(this.parBuilder.build("authResult",
				ClassName.get(this.securityCorePackage, "Authentication")))
			.addStatement("$T username = authResult.getName()", String.class)
			.beginControlFlow("if (username != null && !username.isEmpty())")
				.addStatement("$T token = this.jwtUtil.generateToken(username)", String.class)
				.addStatement("response.setStatus($T.SC_OK)", HttpServletResponse.class)
				.addStatement("response.setContentType($S)", "application/json")
				.addStatement("response.setCharacterEncoding($S)", "UTF-8")
				.addStatement("response.getWriter().append(this.createSuccessBody(token))")
			.endControlFlow()
			.build();

		MethodSpec unsuccessfulAuthentication = MethodSpec.methodBuilder("unsuccessfulAuthentication")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(IOException.class)
			.addException(ServletException.class)
			.addParameter(this.parBuilder.build("request", HttpServletRequest.class))
			.addParameter(this.parBuilder.build("response", HttpServletResponse.class))
			.addParameter(this.parBuilder.build("e",
				ClassName.get(this.securityCorePackage, "AuthenticationException")))
			.addStatement("response.setStatus($T.SC_UNAUTHORIZED)", HttpServletResponse.class)
			.addStatement("response.setContentType($S)", "application/json")
			.addStatement("response.setCharacterEncoding($S)", "UTF-8")
			.addStatement("response.getWriter().append(this.createUnsuccessBody(e.getMessage()))")
			.build();

		MethodSpec createSuccessBody = MethodSpec.methodBuilder("createSuccessBody")
			.addModifiers(Modifier.PRIVATE)
			.addParameter(this.parBuilder.build("token", String.class))
			.addStatement("return \""
				+ "{\\\"timestamp\\\": \" + new $T().getTime() + \", "
				+ "\\\"status\\\": \" + $T.SC_OK + \", "
				+ "\\\"token\\\": \" + token + \", "
				+ "\\\"message\\\", \\\"Authenticated successfully\\\"}\"", Date.class, HttpServletResponse.class)
			.returns(String.class)
			.build();

		MethodSpec createUnsuccessBody = MethodSpec.methodBuilder("createUnsuccessBody")
			.addModifiers(Modifier.PRIVATE)
			.addParameter(this.parBuilder.build("message", String.class))
			.addStatement("return \""
				+ "{\\\"timestamp\\\": \" + new $T().getTime() + \", "
				+ "\\\"status\\\": \" + $T.SC_UNAUTHORIZED + \", "
				+ "\\\"error\\\": \\\"Could not authenticate\\\", "
				+ "\\\"message\\\", \" + message + \"}\"", Date.class, HttpServletResponse.class)
			.returns(String.class)
			.build();

		FieldSpec jwtUtilField = FieldSpec.builder(
			ClassName.get(this.packageName, this.classesPrefix + "JWTUtil"),
			"jwtUtil",
			Modifier.PRIVATE)
			.build();

		FieldSpec authManagerField = FieldSpec.builder(
			ClassName.get(this.securityAuthPackage, "AuthenticationManager"),
			"authManager",
			Modifier.PRIVATE)
			.build();

		TypeSpec classe = TypeSpec.classBuilder(this.classesPrefix + "JWTAuthenticationFilter")
			.superclass(ClassName.get(this.webAuthPackage, "UsernamePasswordAuthenticationFilter"))
			.addMethod(constructor)
			.addMethod(attemptAuthentication)
			.addMethod(successfulAuthentication)
			.addMethod(unsuccessfulAuthentication)
			.addMethod(createSuccessBody)
			.addMethod(createUnsuccessBody)
			.addField(jwtUtilField)
			.addField(authManagerField)
			.build();

		return classe;
	}
}
