package hava.annotation.spring.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ReflectionUtils {

	public Constructor<?> getConstructor(Class<?> objectClass, Class<?>... paramsClasses) {

		try {
			Constructor<?> typeConstructor = objectClass.getDeclaredConstructor(paramsClasses);
			typeConstructor.setAccessible(true);

			return typeConstructor;
		} catch (NoSuchMethodException | SecurityException e) {

			throw new RuntimeException(
				String.format("Could not get constructor with parameters %s of class %s: %s",
					paramsClasses, objectClass, e.getMessage()));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T construct(Class<T> objectClass, Class<?>[] paramsClasses, Object[] params) {

		try {
			return (T) this.getConstructor(objectClass, paramsClasses).newInstance(params);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {

			throw new RuntimeException(
				String.format("Could not create new instance of class %s: %s",
					objectClass, e.getMessage()));
		}
	}

	public void setAccessible(Class<?> objectClass, String field) {

		try {
			objectClass.getField(field).setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(
				String.format("Could not set field %s of class %s accessible: %s",
					field, objectClass, e.getMessage()));
		}
	}

	public void setAccessible(Class<?> objectClass, String... fields) {

		Arrays.stream(fields).forEach(f -> this.setAccessible(objectClass, f));
	}

	public void setAccessible(Field field) {

		field.setAccessible(true);
	}

	public void removeModifiers(Field field, Integer... modifiers) {

		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);

			for (Integer modifier : modifiers) {

				modifiersField.set(field, field.getModifiers() & ~modifier);
			}

		} catch (NoSuchFieldException | IllegalAccessException e) {

			e.printStackTrace();
			throw new RuntimeException(
				String.format("Could not remove modifiers %s for %s: %s",
					modifiers, field, e.getMessage()));
		}
	}

	public void removeModifiers(Field[] fields, Integer... modifiers) {

		Arrays.stream(fields).forEach(f -> this.removeModifiers(f, modifiers));
	}

	public void removeModifiers(Class<?> objectClass, String field, Integer... modifiers) {

		try {
			this.removeModifiers(objectClass.getDeclaredField(field), modifiers);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(
				String.format("Could not find Field %s: %s",
					field, e.getMessage()));
		}
	}

	public void removeModifiers(Class<?> objectClass, String[] fields, Integer... modifiers) {

		Arrays.stream(fields).forEach(f -> this.removeModifiers(objectClass, f, modifiers));
	}

	public void setAccessibleAndRemoveModifiers(Field field, Integer... modifiers) {

		this.setAccessible(field);
		this.removeModifiers(field, modifiers);
	}

	public void setAccessibleAndRemoveModifiers(Field[] fields, Integer... modifiers) {

		Arrays.stream(fields).forEach(f -> {
			this.setAccessibleAndRemoveModifiers(f);
		});
	}

	public void setAccessibleAndRemoveModifiers(Class<?> objectClass, String field, Integer... modifiers) {

		this.setAccessible(objectClass, field);
		this.removeModifiers(objectClass, field, modifiers);
	}

	public void setAccessibleAndRemoveModifiers(Class<?> objectClass, String[] fields, Integer... modifiers) {

		Arrays.stream(fields).forEach(f -> this.setAccessibleAndRemoveModifiers(objectClass, f, modifiers));
	}

	public <T> void setValues(T object, String[] fields, Object[] values) {

		for (int i = 0; i < fields.length; i++) {

			try {
				Field field = object.getClass().getDeclaredField(fields[i]);
				this.setAccessibleAndRemoveModifiers(field, Modifier.FINAL);
				field.set(object, values[i]);
			} catch (NoSuchFieldException | IllegalAccessException e) {

				throw new RuntimeException(
					String.format("Could not set values %s for %s: %s",
						values, fields, e.getMessage()));
			}
		}
	}
}
