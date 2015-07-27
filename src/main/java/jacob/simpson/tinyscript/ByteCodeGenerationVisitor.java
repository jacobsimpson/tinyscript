package jacob.simpson.tinyscript;

import jacob.simpson.tinyscript.grammar.TinyScriptBaseVisitor;
import jacob.simpson.tinyscript.grammar.TinyScriptParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ByteCodeGenerationVisitor extends TinyScriptBaseVisitor<Integer> {
    private final ClassWriter cw;
    private MethodVisitor mv;
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
    public Integer visitPrintStatement(@NotNull TinyScriptParser.PrintStatementContext ctx) {
        if (ctx.STRING() != null) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(ctx.STRING().getText());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
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
