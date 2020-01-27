package me.i509.sloom;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.i509.sloom.util.AnnotationProxy;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

public class MixinASTHelper {
	private static final Class<?>[] MIXIN_ANNOTATIONS = new Class[]{
		Accessor.class, Invoker.class, Implements.class, At.class, Inject.class, ModifyArg.class,
		ModifyArgs.class, ModifyConstant.class, ModifyVariable.class, Redirect.class,
		Interface.class, /* Mixin.class, */ Overwrite.class, Shadow.class
	};

	public static Set<Annotation> findAnnotations(BodyDeclaration declaration) {
		Set<java.lang.annotation.Annotation> annotations = new HashSet<>();
		List<IExtendedModifier> modifiers = declaration.modifiers();

		for (IExtendedModifier modifier : modifiers) {
			if (!(modifier instanceof NormalAnnotation)) {
				continue;
			}

			NormalAnnotation annotation = (NormalAnnotation) modifier;
			String name = annotation.resolveTypeBinding().getBinaryName();

			for (Class<?> clazz : MIXIN_ANNOTATIONS) {
				if (name.equals(clazz.getName())) {
					annotations.add(AnnotationProxy.createProxy(clazz, annotation.resolveAnnotationBinding()));
				}
			}
		}

		return annotations;
	}

	public static Mixin findMixin(ASTNode node) {
		if (node instanceof TypeDeclaration) {
			List<IExtendedModifier> modifiers = ((TypeDeclaration) node).modifiers();

			for (IExtendedModifier modifier : modifiers) {
				if (modifier instanceof NormalAnnotation) {
					NormalAnnotation annotation = (NormalAnnotation) modifier;

					if (annotation.resolveTypeBinding().getBinaryName().equals(Mixin.class.getName())) {
						return (Mixin) AnnotationProxy.createProxy(Mixin.class, annotation.resolveAnnotationBinding());
					}
				}
			}

			return null;
		}

		node = node.getParent();

		if (node == null) {
			return null;
		} else {
			return findMixin(node);
		}
	}

	public static boolean isMixinAndShouldRemap(ASTNode node) {
		Mixin mixin = MixinASTHelper.findMixin(node);

		if (mixin == null || !mixin.remap()) {
			return false;
		}

		return true;
	}
}
