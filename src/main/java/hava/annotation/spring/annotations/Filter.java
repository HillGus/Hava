package hava.annotation.spring.annotations;

public @interface Filter {

	String[] parameters() default {};
	Class<?>[] parametersTypes() default {};
}
