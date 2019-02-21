package hava.annotation.spring.annotations;

import io.jsonwebtoken.SignatureAlgorithm;

public @interface Authentication {

	public String secret();
	public Class<?> encoder();
	public long expiration() default 3600000L;
	public SignatureAlgorithm algorithm() default SignatureAlgorithm.HS512;
}
