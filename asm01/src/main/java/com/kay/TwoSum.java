package com.kay;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

public class TwoSum extends ClassLoader{

    public static void main(String[] args) throws Exception {
        final byte[] bytes = generate();
        outputClazz(bytes);

        final Class<?> clazz = new TwoSum().defineClass("com.kay.AsmTwoSum", bytes, 0, bytes.length);

        final Object instance = clazz.getDeclaredConstructor().newInstance();

        final Method sum = clazz.getMethod("sum", int.class, int.class);

        final Object result = sum.invoke(instance, 1, 2);

        System.out.println("result=" + result);
    }


    private static byte[] generate(){
        ClassWriter classWriter = new ClassWriter(0);

        {
            //header of class
            classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "com/kay/AsmTwoSum", null, "java/lang/Object",
                              null);

            //empty constructor
            MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }

        //sum method
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "sum", "(II)I", null, null);
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 1);
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 2);
            methodVisitor.visitInsn(Opcodes.IADD);
            methodVisitor.visitInsn(Opcodes.IRETURN);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }

        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private static void outputClazz(byte[] bytes){
        try (FileOutputStream out = new FileOutputStream("AsmTowSum.class")) {
            System.out.println("ASM output path:" + (new File("")).getAbsolutePath());
            out.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
