package hava.debug.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransformationUtils {

	public static boolean isInTransformationTree(String className) {

		try {
			String packageName = TransformationUtils.class.getPackage().getName();
			Class<?> instUtilsClass = Class.forName(packageName + ".InstrumentationTreeUtils");
			Method getInstance = instUtilsClass.getMethod("getInstance");
			Method isInTransformationTree = instUtilsClass.getMethod("isInTransformationTree", String.class);

			return (boolean) isInTransformationTree.invoke(getInstance.invoke(null), className);
		} catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}
}
