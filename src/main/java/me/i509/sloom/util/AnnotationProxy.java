package me.i509.sloom.util;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

public class AnnotationProxy implements InvocationHandler {
	private static MethodHandle BINDING;

	private final Map<String, ITypeBinding> classMap = new HashMap<>();
	private final IAnnotationBinding binding;
	private final Map<String, Object> data;

	private AnnotationProxy(IAnnotationBinding binding) {
		this.binding = binding;
		this.data = Arrays.stream(binding.getAllMemberValuePairs())
			.collect(Collectors.toMap(IMemberValuePairBinding::getName, IMemberValuePairBinding::getValue));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> returnType = method.getReturnType();

		if (method.getDeclaringClass() == Object.class) {
			if (returnType == int.class) {
				return 0;
			} else if (returnType == boolean.class) {
				return false;
			}

			return "AnnotationProxy" + data;
		}

		Object value = valueOf(method.getName(), returnType, data.get(method.getName()));

		return value != null ? value : method.getDefaultValue();
	}

	private Object valueOf(String name, Class<?> returnType, Object value) throws Throwable {
		if (value == null) {
			return null;
		}

		if (returnType.isPrimitive()) {
			return value;
		}

		if (returnType == Class.class) {
			classMap.put(name, (ITypeBinding) value);
			return null;
		}

		if (returnType.isEnum()) {
			FieldBinding fieldBinding = (FieldBinding) BINDING.invokeExact(value);
			return Class.forName(fieldBinding.type.debugName()).getField(new String(fieldBinding.name));
		}

		if (returnType.isArray()) {
			int length = Array.getLength(value);
			Class<?> componentType = returnType.getComponentType();
			Object array = Array.newInstance(componentType, length);

			for (int i = 0; i < length; i++) {
				Array.set(array, i, valueOf(name + ':' + i, componentType, Array.get(value, i)));
			}

			return array;
		}

		if (returnType.isAnnotation()) {
			return createProxy(returnType, (IAnnotationBinding) value);
		}

		return null;
	}

	public static Annotation createProxy(Class<?> annotation, IAnnotationBinding binding) {
		assert annotation.isAnnotation();

		return (Annotation) Proxy.newProxyInstance(
			annotation.getClassLoader(),
			new Class<?>[]{annotation},
			new AnnotationProxy(binding));
	}

	public static IAnnotationBinding get(Object proxy) {
		return ((AnnotationProxy) Proxy.getInvocationHandler(proxy)).binding;
	}

	public static ITypeBinding get(Object proxy, String name) {
		return ((AnnotationProxy) Proxy.getInvocationHandler(proxy)).classMap.get(name);
	}

	public static List<ITypeBinding> getAll(Object proxy, String name) {
		Map<String, ITypeBinding> map = ((AnnotationProxy) Proxy.getInvocationHandler(proxy)).classMap;
		List<ITypeBinding> bindings = new ArrayList<>();

		map.forEach((n, value) -> {
			if (n.startsWith(name + ':')) {
				bindings.add(value);
			}
		});

		return bindings;
	}

	static {
		try {
			Field binding = Class.forName("org.eclipse.jdt.core.dom.VariableBinding").getDeclaredField("binding");
			binding.setAccessible(true);
			BINDING = MethodHandles.lookup().unreflectGetter(binding);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
