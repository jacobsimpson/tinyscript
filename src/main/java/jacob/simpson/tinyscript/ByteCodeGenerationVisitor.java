package jacob.simpson.tinyscript;

import jacob.simpson.tinyscript.grammar.TinyScriptBaseVisitor;
import jacob.simpson.tinyscript.grammar.TinyScriptParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;

public class ByteCodeGenerationVisitor extends TinyScriptBaseVisitor<Integer> {
    private final ClassWriter cw;
    private MethodVisitor mv;
    private Map<String, Integer> variables = new HashMap<>();
    private int localVariableCounter = 1;

    public ByteCodeGenerationVisitor(String className) {
        cw = new ClassWriter(0);

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public Integer visitAssignmentStatement(@NotNull TinyScriptParser.AssignmentStatementContext ctx) {
        mv.visitIntInsn(BIPUSH, Integer.parseInt(ctx.INTEGER().getText()));
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        String varName = ctx.IDENTIFIER().getText();
        if (!variables.containsKey(varName)) {
            throw new RuntimeException(format("The variable '%s' has not been defined.", varName));
        }
        mv.visitVarInsn(ASTORE, variables.get(varName));
        return visitChildren(ctx);
    }

    @Override
    public Integer visitDeclarationStatement(@NotNull TinyScriptParser.DeclarationStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        if (variables.containsKey(varName)) {
            throw new RuntimeException(format("The variable '%s' is already defined.", varName));
        }
        variables.put(varName, localVariableCounter);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, localVariableCounter);
        localVariableCounter++;
        return visitChildren(ctx);
    }

    @Override
    public Integer visitPrintStatement(@NotNull TinyScriptParser.PrintStatementContext ctx) {
        if (ctx.STRING() != null) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(ctx.STRING().getText());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        } else if (ctx.IDENTIFIER() != null) {
            String varName = ctx.IDENTIFIER().getText();
            if (!variables.containsKey(varName)) {
                throw new RuntimeException(format("The variable '%s' has not been defined.", varName));
            }
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, variables.get(varName));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
        } else {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "()V", false);
        }
        return visitChildren(ctx);
    }

    @Override
    public Integer visitProgram(@NotNull TinyScriptParser.ProgramContext ctx) {
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        Integer result = visitChildren(ctx);

        mv.visitInsn(RETURN);
        mv.visitMaxs(2, localVariableCounter);
        mv.visitEnd();
        cw.visitEnd();
        return result;
    }

    public byte[] getResult() {
        return cw.toByteArray();
    }
}
