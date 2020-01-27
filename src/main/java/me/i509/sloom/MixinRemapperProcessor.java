package me.i509.sloom;

import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.SourceContext;
import org.cadixdev.mercury.SourceRewriter;

public class MixinRemapperProcessor implements SourceRewriter {
	@Override
	public void rewrite(RewriteContext context) throws Exception {
		context.getCompilationUnit().accept(new MixinASTVisitor());
	}

	@Override
	public void process(SourceContext context) throws Exception {

	}
}
