package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.createAssignment;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createLocalPrimitiveVariable;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;
import static io.github.jorelali.javacompilerplugins.ASTHelper.newClassInstanceWithNoParamsOrGenerics;
import static io.github.jorelali.javacompilerplugins.ASTHelper.parseAnnotation;

import java.util.Map;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import io.github.jorelali.javacompilerplugins.ASTHelper;

public class ReflectionGeneratorTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	private CompilationUnitTree currentCompilationUnitTree;
	
	private static final boolean VERBOSE = false;
	
	public ReflectionGeneratorTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
		this.trees = Trees.instance(task);
	}
	
	/*
	 * TODO:
	 * //Class instantiation with parameters
	 */
	
	@Override
	public Void visitCompilationUnit(CompilationUnitTree compilationUnitTree, Void p) {
		if(VERBOSE) System.out.println("COMPUNIT: " + compilationUnitTree);
		currentCompilationUnitTree = compilationUnitTree;
		return super.visitCompilationUnit(compilationUnitTree, p);
	}
	
	@Override
	public Void visitClass(ClassTree classTree, Void p) {
		if(VERBOSE) System.out.println("CLASS: " + classTree.getSimpleName());
		return super.visitClass(classTree, p);
	}
	
	@Override
	public Void visitVariable(VariableTree variableTree, Void p) {
		if(VERBOSE) System.out.println("VARIABLE: " + variableTree.getName());
		return super.visitVariable(variableTree, p);
	}
	
	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		if(VERBOSE) System.out.println("METHOD: " + methodTree.getName());
		
		
		if(!methodTree.getModifiers().getAnnotations().isEmpty()) {
			for(AnnotationTree annotation : methodTree.getModifiers().getAnnotations()) {
				if(annotation.getAnnotationType().toString().equals("ReflectionMethod")) {
					
					TreeMaker maker = createTreeMaker(context, methodTree);
					Names names = Names.instance(context);

					Map<String, String> annotationMap = parseAnnotation(annotation);
					String methodName = methodTree.getName().toString();
					//Class targetClass = Class.forName(annotationMap.get("targetClass").replace(".class", ""));
					
					/*
					 * So: Method m = targetClass.getClass().getdeclaredmethod(methodName)
					 * inside, you want blah.invoke()
					 */
					
					System.out.println("\n\n");
					
					/* Method method = ExampleClass.class.getDeclaredMethod("sayHi"); */
					
					JCExpression methodClassExpr = ASTHelper.resolveName(maker, names, "java.lang.reflect.Method");

					JCAssign targetClassTree = (JCAssign) annotation.getArguments().stream().peek(o -> System.out.println(o)).filter(o -> ((JCAssign) o).lhs.toString().equals("targetClass")).findFirst().get();					
					JCExpression targetClass = (JCExpression) targetClassTree.rhs;
					
					System.out.println(targetClass);
					
					//Create getDeclaredMethod m
					//maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "java.lang.System.out.println"), List.of(maker.Literal("hello!")));
						JCMethodInvocation c = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, targetClass + ".getDeclaredMethod"), List.of(maker.Literal("sayHi")));
						System.out.println(c);
						//JCMethodInvocation declaredMethod = (JCMethodInvocation) ;
					//Method m = this.getClass().getDeclaredMethod(name, parameterTypes)
					
					JCVariableDecl method = maker.VarDef(maker.Modifiers(0), names.fromString("method"), methodClassExpr, c);
					System.out.println(method);
					
					
					
					
					/* method.setAccessible(true); */
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "method.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					System.out.println(compiledSetAccessible);
					
					
					
					//JCExpression methodField = maker.Ident(method);
//					JCFieldAccess methodFieldVar = maker.Select(method.nameexpr, ASTHelper.makeNameDirty("setAccessible"));
//					System.out.println(methodFieldVar);
//					
//					JCMethodInvocation setAccessible = maker.Apply(List.nil(), maker.Select(maker.Ident(method), names.fromString("setAccessible")), List.of(maker.Literal(true)));
//					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
//					System.out.println(compiledSetAccessible);
					
					
					
					/* method.invoke(null); */
					/*
					 * Note: This is ONLY for static methods!
					 */
					
					JCMethodInvocation invoke = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "method.invoke"), List.of(maker.Literal(TypeTag.BOT, null)));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					System.out.println(compiledInvoke);
					System.out.println(compiledInvoke);
					
					
					
					
					
					
					System.out.println("\n\n");
					
					
					
					
					//REST OF LINE IGNORED
					
					
					
					
					
					
					
					
					
//					Scanner scanner = new Scanner(System.in);
//					System.out.println(scanner.nextLine());
					
					
					JCVariableDecl declareInt = createLocalPrimitiveVariable(maker, "hello", TypeTag.INT, maker.Literal(TypeTag.INT, 0));
					//System.out.println(declareInt);
					JCExpressionStatement assignIntTo2 = createAssignment(maker, declareInt, maker.Literal(TypeTag.INT, 2));
					JCExpressionStatement newModifier = newClassInstanceWithNoParamsOrGenerics(maker, names, "java.lang.reflect.Modifier");
					
					JCExpression modifierExpr = ASTHelper.resolveName(maker, names, "java.lang.reflect.Modifier");
					
					JCVariableDecl modifierDecl = maker.VarDef(maker.Modifiers(0), ASTHelper.makeNameDirty("myModifier"), modifierExpr, maker.NewClass(null, List.nil(), modifierExpr, List.nil(), null));
					//System.out.println(modifierDecl);
					
					JCExpression exception = ASTHelper.resolveName(maker, names, "java.lang.Exception");
					
					//ASTHelper.resolveName(maker, names, "java.lang.reflect.M")
					
					
					// private static final <loggerType> log = <factoryMethod>(<parameter>);
//					JCExpression loggerType = chainDotsString(typeNode, framework.getLoggerTypeName());
//					JCExpression factoryMethod = chainDotsString(typeNode, framework.getLoggerFactoryMethodName());
//					
//					JCExpression loggerName = framework.createFactoryParameter(typeNode, loggingType);
//					JCMethodInvocation factoryMethodCall = maker.Apply(List.<JCExpression>nil(), factoryMethod, List.<JCExpression>of(loggerName));

					
					class Example { }					
					//System.out.println(maker.Literal(TypeTag.CLASS, Example.class));
					
					
					//methodTree.getThrows()
//					JCMethodDecl thisMethod = (JCMethodDecl) methodTree;
//					thisMethod.thrown = thisMethod.thrown.append(exception);
					
//					JCThrow _throw = maker.Throw(maker.NewClass(null, List.nil(), exception, List.nil(), null));
//					
//					JCVariableDecl catchVar = maker.VarDef(maker.Modifiers(0), ASTHelper.makeNameDirty("e"), exception, null);
//					JCCatch _catch = maker.Catch(catchVar, maker.Block(0, List.nil()));
//					
//					JCTry _try = maker.Try(maker.Block(0, List.of(_throw)), List.of(_catch), null);
//					System.out.println("===");
//					System.out.println(_try);
//					System.out.println("===");
					
					//System.out.println("");
					
					JCExpression out = ASTHelper.resolveName(maker, names, "java.lang.System.out");
					JCFieldAccess f = (JCFieldAccess) ASTHelper.resolveName(maker, names, "java.lang.System.out");
					//System.out.println(f);
					
					//= maker.Apply(List.<JCExpression>nil(), factoryMethod, List.<JCExpression>of(loggerName));
					
					
					JCMethodInvocation m = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "java.lang.System.out.println"), List.of(maker.Literal("hello!")));
					//System.out.println(m);
					
					
					
					ASTHelper.addExceptionToMethodDeclaredThrows(maker, names, methodTree, Exception.class);
					
					
					//INSTRUMENTATION START
					
					
					
					
					JCBlock logicBlock = maker.Block(0, List.of(/*declareInt, assignIntTo2, newModifier,/* _try,*//* modifierDecl, maker.Exec(m), */method, compiledSetAccessible, compiledInvoke));
					System.out.println("=== Preparing to instrument " + methodTree.getName() + " ===");
					System.out.println(logicBlock);
					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.append(logicBlock);
										
				}
			}
			
		}
		
		
		// TODO Auto-generated method stub
		return super.visitMethod(methodTree, p);
	}
	
}
