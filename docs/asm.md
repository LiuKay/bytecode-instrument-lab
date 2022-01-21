# ASM 

[ASM 官方文档](https://asm.ow2.io/index.html)


ASM provides three core components based on the ClassVisitor API to generate and transform classes:
- The `ClassReader` class parses a compiled class given as a byte array,
and calls the corresponding visitXxx methods on the ClassVisitor
instance passed as argument to its accept method. It can be seen as an
event producer.
- The `ClassWriter` class is a subclass of the ClassVisitor abstract class
that builds compiled classes directly in binary form. It produces as
output a byte array containing the compiled class, which can be retrieved
with the toByteArray method. It can be seen as an event consumer.
- The `ClassVisitor` class delegates all the method calls it receives to
another ClassVisitor instance. It can be seen as an event filter.

ASM 是基于访问者模式实现，提供了2种 API来操作 class：
- Core API: 基于事件驱动的API，代表就是 MethodWriter,visitXXX等一系列 visit 方法，Core API 消耗更少的内存，性能也更好，但是开发难度较大
- Tree API: 基于面向对象的API，代表就是 ClassNode，MethodNode等一些 XXXNode 对象，Tree API 的底层实现也是基于Core API，会占用多一点的内存，但是面向对象的方法开发起来难度相对来说更小

在 Core API 中提供一下几个关键类：
- ClassReader: 用于读取已经编译好的 .class 文件
- ClassWriter: 用于编辑 class 或者生成新的字节码
- 各种 Visitor用于访问不同的数据：MethodVisitor，FieldVisitor，AnnotationVisitor等等

### Core API
用 ClassWriter 生成一个 `HelloWrold`类：
```java
public class HelloWorld extends ClassLoader{

    public static void main(String[] args) throws Exception {
        final byte[] bytes = generate();
        Utils.outputClass(bytes, "AsmHello");

        final Class<?> clazz = new HelloWorld().defineClass("com.kay.AsmHello", bytes, 0, bytes.length);

        final Method main = clazz.getMethod("main", String[].class);
        main.invoke(null, new Object[]{new String[]{}});
    }

    private static byte[] generate(){
        ClassWriter classWriter = new ClassWriter(0);

        //class header
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "com/kay/AsmHello", null, "java/lang/Object", null);

        //create method : <init>
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd(); //method end
        }

        //create method: main
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
                                                                  "([Ljava/lang/String;)V", null, null);
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");

            methodVisitor.visitLdcInsn("Hello World ASM!");

            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
                                          "(Ljava/lang/String;)V", false);

            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd(); //method end
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

}
```
运行以上代码将会生成一个如下的类：
```java
package com.kay;
public class AsmHello {
    public AsmHello() {
    }

    public static void main(String[] var0) {
        System.out.println("Hello World ASM!");
    }
}
```

### Tree API
Tree API 用来操作 Class 的类是 ClassNode, 生一个 `HelloWorld` class 的代码如下：
```java
public class TreeApiHelloWorld {

    public static void main(String[] args) {
        ClassNode classNode = new ClassNode();
        classNode.name = "com/kay/AsmTreeHelloWorld";
        classNode.version = V1_8;
        classNode.access = ACC_PUBLIC;
        classNode.superName = "java/lang/Object";

        //<init>
        final MethodNode mn = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
        final InsnList insnList = mn.instructions;
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        insnList.add(new InsnNode(RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 1;
        classNode.methods.add(mn);

        //main
        final MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
                                                     null);
        final InsnList instructions = methodNode.instructions;

        instructions.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStrem"));
        instructions.add(new LdcInsnNode("Hello World ASM!"));
        instructions.add(
                new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        instructions.add(new InsnNode(RETURN));
        methodNode.maxStack = 2;
        methodNode.maxLocals = 1;
        classNode.methods.add(methodNode);

        final ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);

        Utils.outputClass(classWriter.toByteArray(), "AsmTreeHelloWorld");
    }
}
```