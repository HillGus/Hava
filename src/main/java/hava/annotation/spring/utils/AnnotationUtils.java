package hava.annotation.spring.utils;

import com.squareup.javapoet.AnnotationSpec;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Soundbank;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class AnnotationUtils {


	private final String javaPoetGit = "https://github.com/square/javapoet/blob/master/src/main/java/com/squareup/javapoet";


	public AnnotationSpec build(Class<? extends Annotation> annotationClass) {

		return AnnotationSpec.builder(annotationClass).build();
	}

	public AnnotationSpec build(Class<? extends Annotation> annotationClass, Object[]... keyValues) {

		AnnotationSpec.Builder builder = AnnotationSpec.builder(annotationClass);

		Arrays.stream(keyValues).forEach(
			keyValue -> {
				if (keyValue[0].getClass() != String.class)
					throw new RuntimeException(String.format("First value of keyValues must be a String. see method addMember " +
															" of inner Builder on %s/AnnotationSpec.java",
															this.javaPoetGit));

				if (keyValue[1].getClass() != String.class)
					throw new RuntimeException(String.format("Second value of keyValues must be a String. see method addMember " +
															" of inner Builder on %s/AnnotationSpec.java",
															"addMember(String, String, Object...)",
															this.javaPoetGit));

				List<Object> args = Arrays.asList(keyValue);
				args = args.subList(2, args.size());

				builder.addMember(keyValue[0].toString(), keyValue[1].toString(), args.toArray());
			});

		return builder.build();
	}

	public AnnotationSpec buildWithValue(Class<? extends Annotation> mappingClass, String value) {

		return AnnotationSpec.builder(mappingClass).addMember("value", "\"" + value + "\"").build();
	}

	public AnnotationSpec postMapping(String value) {

		return this.buildWithValue(PostMapping.class, value);
	}

	public AnnotationSpec getMapping(String value) {

		return this.buildWithValue(GetMapping.class, value);
	}

	public AnnotationSpec deleteMapping(String value) {

		return this.buildWithValue(DeleteMapping.class, value);
	}

	public AnnotationSpec putMapping(String value) {

		return this.buildWithValue(PutMapping.class, value);
	}

	public AnnotationSpec requestMapping(String value) {

		return this.buildWithValue(RequestMapping.class, value);
	}
}
