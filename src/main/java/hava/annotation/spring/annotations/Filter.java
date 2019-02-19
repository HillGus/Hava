package hava.annotation.spring.annotations;

public @interface Filter {

	String[] fields() default {};
	LikeType likeType() default LikeType.BOTH;

	public enum LikeType {
		START, END, BOTH
	}
}
