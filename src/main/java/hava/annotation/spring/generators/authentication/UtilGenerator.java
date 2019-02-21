package hava.annotation.spring.generators.authentication;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.generators.CodeGenerator;
import hava.annotation.spring.utils.MiscUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.Date;

public class UtilGenerator {

	private ParameterBuilder parBuilder;

	private String classesPrefix;

	public UtilGenerator(CodeGenerator codeGenerator, String classesPrefix) {

		this.parBuilder = codeGenerator.parBuilder;
		this.classesPrefix = classesPrefix;
	}

	public TypeSpec generate(String secret, Long expiration, SignatureAlgorithm algorithm) {

		MethodSpec generateToken = MethodSpec.methodBuilder("generateToken")
			.addParameter(this.parBuilder.build("username", String.class))
			.addStatement(
				"return $T.builder()\n"
				+ ".setSubject(username)\n"
				+ ".setExpiration(new $T($T.currentTimeMillis() + this.expiration))\n"
				+ ".signWith(this.signatureAlgorithm, this.secret.getBytes())\n"
				+ ".compact()"
				, Jwts.class, Date.class, System.class
			)
			.returns(String.class)
			.build();

		MethodSpec validToken = MethodSpec.methodBuilder("validToken")
			.addParameter(this.parBuilder.build("token", String.class))
			.beginControlFlow("if (token != null && !token.isEmpty())")
				.addStatement("$T claims = this.getClaims(token)", Claims.class)
				.beginControlFlow("if (claims != null)")
					.addStatement("$T username = claims.getSubject()", String.class)
					.addStatement("$T expirationDate = claims.getExpiration()", Date.class)
					.addStatement("$T now = new $T($T.currentTimeMillis())", Date.class, Date.class, System.class)
					.beginControlFlow("if ((username != null && !username.isEmpty()) && (expirationDate != null && now.before(expirationDate)))")
						.addStatement("return true")
					.endControlFlow()
				.endControlFlow()
			.endControlFlow()
			.addStatement("return false")
			.returns(TypeName.BOOLEAN)
			.build();

		MethodSpec getUsername = MethodSpec.methodBuilder("getUsername")
			.addParameter(this.parBuilder.build("token", String.class))
			.addStatement("$T username = null", String.class)
			.beginControlFlow("if (token != null && !token.isEmpty())")
				.addStatement("$T claims = this.getClaims(token)", Claims.class)
				.beginControlFlow("if (claims != null)")
					.addStatement("username = claims.getSubject()")
				.endControlFlow()
			.endControlFlow()
			.addStatement("return username")
			.returns(String.class)
			.build();

		MethodSpec getClaims = MethodSpec.methodBuilder("getClaims")
			.addParameter(this.parBuilder.build("token", String.class))
			.beginControlFlow("try")
				.addStatement(
					"return $T.parser()\n"
					+ ".setSigningKey(this.secret.getBytes())\n"
					+ ".parseClaimsJws(token)\n"
					+ ".getBody()"
					, Jwts.class
				)
			.nextControlFlow("catch (Exception e)")
				.addStatement("return null")
			.endControlFlow()
			.returns(Claims.class)
			.build();

		FieldSpec expirationField = FieldSpec.builder(
			Long.class, "expiration", Modifier.PRIVATE)
			.initializer("$L", expiration + "L")
			.build();

		FieldSpec algorithmField = FieldSpec.builder(
			SignatureAlgorithm.class, "signatureAlgorithm", Modifier.PRIVATE)
			.initializer("$T.$L", SignatureAlgorithm.class, algorithm)
			.build();

		FieldSpec secretField = FieldSpec.builder(
			String.class, "secret", Modifier.PRIVATE)
			.initializer("$S", secret)
			.build();

		TypeSpec classe = TypeSpec.classBuilder(this.classesPrefix + "JWTUtil")
			.addAnnotation(Component.class)
			.addField(expirationField)
			.addField(algorithmField)
			.addField(secretField)
			.addMethod(generateToken)
			.addMethod(validToken)
			.addMethod(getUsername)
			.addMethod(getClaims)
			.build();

		return classe;
	}
}
