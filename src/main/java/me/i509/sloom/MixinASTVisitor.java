package me.i509.sloom;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import me.i509.sloom.util.MixinConstants;
import org.cadixdev.mercury.util.BombeBindings;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelector;
import org.spongepowered.asm.mixin.injection.selectors.TargetSelector;

/**
 * Remaps Mixin types.
 *
 * <p>A Mixin class is defined as a class which contains a @Mixin annotation at the top.
 */
public class MixinASTVisitor extends ASTVisitor {
	@Override
	public boolean visit(NormalAnnotation node) {
		node.getTypeName().getFullyQualifiedName();
		Mixin mixin = MixinASTHelper.findMixin(node);
		if (mixin != null) {
			System.out.println(Arrays.toString(mixin.targets()));
			System.out.println(Arrays.toString(mixin.value()));
			System.out.println(mixin.priority());
		}

		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (!MixinASTHelper.isMixinAndShouldRemap(node)) {
			return true;
		}

		IMethodBinding binding = node.resolveBinding();
		ITypeBinding declaring = binding.getDeclaringClass();

		for (Annotation annotation : MixinASTHelper.findAnnotations(node)) {
			if (annotation instanceof Shadow) {
				// Cast a Shadow
				Shadow castShadow = (Shadow) annotation;
				String annoPrefix = castShadow.prefix();
				boolean usingDefaultPrefix = castShadow.prefix().equals(MixinConstants.DEFAULT_SHADOW_PREFIX);
				// BombeBindings // -- Useful
			}
		}

		return true;
	}
}
