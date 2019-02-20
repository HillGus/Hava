package hava.annotation.spring.generators;

import com.squareup.javapoet.*;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.annotations.Suffixes;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.utils.ElementUtils;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.utils.MiscUtils;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.io.IOException;

public class CodeGenerator {


	ParameterBuilder parBuilder = new ParameterBuilder();
	AnnotationBuilder annBuilder = new AnnotationBuilder();
	MiscUtils miscUtils = new MiscUtils();
	ElementUtils eleUtils;

	private ServiceGenerator serGenerator;
	private ControllerGenerator conGenerator;
	private RepositoryGenerator repGenerator;

	private Filer filer;
	private Elements elementUtils;

	private boolean debug;
	private String repSuffix;
	private String serSuffix;
	private String conSuffix;
	private String classesPrefix;

	private String prefix;
	private String packageName;
	private CRUD annCrud;

	public CodeGenerator(Elements elementUtils, Filer filer) {

		this.filer = filer;
		this.elementUtils = elementUtils;

		try {
			setDebug((boolean) HASConfiguration.class.getDeclaredMethod("debug").getDefaultValue());
			setSuffixes((Suffixes) HASConfiguration.class.getDeclaredMethod("suffixes").getDefaultValue());
			setClassesPrefix((String) HASConfiguration.class.getDeclaredMethod("classesPrefix").getDefaultValue());
		} catch (Exception e) {
			throw new RuntimeException("Could not load default HASConfigurations: " + e.getMessage());
		}
	}


	public void generateClasses(Element element) throws RuntimeException {

		this.eleUtils = new ElementUtils(element, elementUtils);

		this.repGenerator = new RepositoryGenerator(this, this.repSuffix, this.classesPrefix);
		this.serGenerator = new ServiceGenerator(this, this.serSuffix, this.repSuffix, this.classesPrefix);
		this.conGenerator = new ControllerGenerator(this, this.conSuffix, this.serSuffix, this.classesPrefix);

		this.prefix = element.getSimpleName().toString();
		this.packageName = this.eleUtils.packageOf(element);

		this.annCrud = element.getAnnotation(CRUD.class);
		this.prefix = "".equals(annCrud.name()) ? this.prefix : annCrud.name();
		String endpoint = "".equals(annCrud.endpoint()) ? this.prefix.toLowerCase() : annCrud.endpoint();

		generateRepository();
		generateService();
		generateController(endpoint);
	}


	private void generateRepository() throws RuntimeException {

		save(this.repGenerator.generate(this.prefix, this.annCrud));
	}

	private void generateService() throws RuntimeException {

		save(this.serGenerator.generate(this.prefix, this.annCrud));
	}

	private void generateController(String endpoint) throws RuntimeException {

		save(this.conGenerator.generate(this.prefix, this.annCrud, endpoint));
	}


	private void save(TypeSpec spec) throws RuntimeException {

		JavaFile file = JavaFile.builder(packageName, spec).build();

		try {
			if (debug) {
				System.out.println("\n" + StringUtils.center(
					String.format(
						"Class generated for %s",
						this.eleUtils.elementTypeStr()
					), 75, '-'));

				file.writeTo(System.out);

				System.out.println(StringUtils.center("", 75, '-'));
			}

			file.writeTo(this.filer);
		} catch (IOException e) {
			throw new RuntimeException("Could not write class to filer: " + e.getMessage());
		}
	}


	public void setDebug(boolean debug) {

		this.debug = debug;
	}

	public void setSuffixes(Suffixes suffixes) {

		this.repSuffix = suffixes.repository();
		this.serSuffix = suffixes.service();
		this.conSuffix = suffixes.controller();
	}

	public void setClassesPrefix(String classesPrefix) {

		this.classesPrefix = classesPrefix;
	}
}

class StringUtils {

	public static String center(String s, int size, char pad) {
		if (s == null || size <= s.length())
			return s;

		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < (size - s.length()) / 2; i++) {
			sb.append(pad);
		}
		sb.append(s);
		while (sb.length() < size) {
			sb.append(pad);
		}
		return sb.toString();
	}
}