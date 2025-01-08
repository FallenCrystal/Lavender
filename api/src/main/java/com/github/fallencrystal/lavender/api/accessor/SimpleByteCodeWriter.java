package com.github.fallencrystal.lavender.api.accessor;

import lombok.Getter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import org.jetbrains.annotations.NotNull;

@Getter
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public final class SimpleByteCodeWriter {
    private final @NotNull MethodVisitor methodVisitor;
    private final @NotNull Label[] labels;

    public SimpleByteCodeWriter(final @NotNull MethodVisitor methodVisitor, final int labelSize) {
        this.methodVisitor = methodVisitor;
        this.labels = AccessorFactory.createLabelArray(labelSize);
    }

    public @NotNull SimpleByteCodeWriter aload(int varIndex) {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, varIndex);
        return this;
    }

    public @NotNull SimpleByteCodeWriter aload(int index1, int index2) {
        return aload(index1).aload(index2);
    }

    public @NotNull SimpleByteCodeWriter aload(int index1, int index2, int index3) {
        return aload(index1).aload(index2, index3);
    }

    public @NotNull SimpleByteCodeWriter aload(int index1, int index2, int index3, int index4) {
        return aload(index1).aload(index2, index3, index4);
    }

    public @NotNull SimpleByteCodeWriter aload(int... indexs) {
        for (int index : indexs) aload(index);
        return this;
    }

    public @NotNull SimpleByteCodeWriter aloadThis() {
        return aload(0);
    }

    public @NotNull SimpleByteCodeWriter invokeSpecial(
            final @NotNull String owner, final @NotNull String name,
            final @NotNull String descriptor, final boolean isInterface) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, descriptor, isInterface);
        return this;
    }

    public @NotNull SimpleByteCodeWriter invokeSpecial(
            final @NotNull String owner, final @NotNull String name,
            final @NotNull String descriptor) {
        return invokeSpecial(owner, name, descriptor, false);
    }

    public @NotNull SimpleByteCodeWriter invokeInit(final @NotNull String owner, final @NotNull String descriptor) {
        return invokeSpecial(owner, "<init>", "(" + descriptor + ")V");
    }

    public @NotNull SimpleByteCodeWriter invokeInit(final @NotNull String owner) {
        return invokeInit(owner, "");
    }

    public @NotNull SimpleByteCodeWriter callObjectInit() {
        return visitLabel(0).invokeInit("java/lang/Object");
    }

    public @NotNull SimpleByteCodeWriter ldc(final @NotNull String ldc) {
        methodVisitor.visitLdcInsn(ldc);
        return this;
    }

    public @NotNull SimpleByteCodeWriter invokeVirtual(
            final @NotNull String owner, final @NotNull String name,
            final @NotNull String descriptor, boolean isInterface) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, isInterface);
        return this;
    }

    public @NotNull SimpleByteCodeWriter invokeVirtual(
            final @NotNull String owner, final @NotNull String name,
            final @NotNull String descriptor) {
        return invokeVirtual(owner, name, descriptor, false);
    }

    public @NotNull SimpleByteCodeWriter invokeStringAppend() {
        return invokeAppend("String");
    }

    public @NotNull SimpleByteCodeWriter invokeStringAppend(final @NotNull String append) {
        return ldc(append).invokeStringAppend();
    }

    public @NotNull SimpleByteCodeWriter invokeObjectAppend() {
        return invokeAppend("Object");
    }

    private @NotNull SimpleByteCodeWriter invokeAppend(final @NotNull String type) {
        return invokeVirtual(
                "java/lang/StringBuilder",
                "append",
                "(Ljava/lang/" + type + ";)Ljava/lang/StringBuilder;"
        );
    }

    public @NotNull SimpleByteCodeWriter popAsClassName() {
        invokeVirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
        invokeVirtual("java/lang/Class", "getName", "()Ljava/lang/String;");
        return this;
    }

    public @NotNull SimpleByteCodeWriter instanceOf(final @NotNull String type) {
        methodVisitor.visitTypeInsn(Opcodes.INSTANCEOF, type);
        return this;
    }

    public @NotNull SimpleByteCodeWriter ifeq(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter ifne(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFNE, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter ifle(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFLE, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter ifgt(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFGT, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter ifge(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFGE, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter iflt(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFLE, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter ifnonnull(int labelIndex) {
        methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter checkCast(final @NotNull String type) {
        methodVisitor.visitInsn(Opcodes.CHECKCAST);
        return this;
    }

    public @NotNull SimpleByteCodeWriter putField(
            final @NotNull String owner,
            final @NotNull String name, final @NotNull String descriptor) {
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, owner, name, descriptor);
        return this;
    }

    public @NotNull SimpleByteCodeWriter gotoLabel(final int labelIndex) {
        methodVisitor.visitLabel(labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter visitLabel(final int labelIndex) {
        methodVisitor.visitLabel(labels[labelIndex]);
        return this;
    }

    public @NotNull SimpleByteCodeWriter newType(final @NotNull String type) {
        methodVisitor.visitTypeInsn(Opcodes.NEW, type);
        return this;
    }

    public @NotNull SimpleByteCodeWriter dup() {
        methodVisitor.visitInsn(Opcodes.DUP);
        return this;
    }

    public @NotNull SimpleByteCodeWriter newAndDup(final @NotNull String type) {
        return newType(type).dup();
    }

    public @NotNull SimpleByteCodeWriter athrow() {
        methodVisitor.visitInsn(Opcodes.ATHROW);
        return this;
    }

    public @NotNull SimpleByteCodeWriter writeReturn() {
        methodVisitor.visitInsn(Opcodes.RETURN);
        return this;
    }

    public @NotNull SimpleByteCodeWriter labelWithReturn(final int labelIndex) {
        return visitLabel(labelIndex).writeReturn();
    }
}
