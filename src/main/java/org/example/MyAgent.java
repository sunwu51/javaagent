package org.example;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

public class MyAgent {
    // 通过javaagent指定当前jar包，则在main函数之前会先运行premain函数
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println(agentArgs); // 打印agentArgs，这个参数通过-javaagent:xx.jar=arg传进来
        MyTransformer transformer = new MyTransformer();
        instrumentation.addTransformer(transformer); // 添加自定义的transformer对象，转换字节码
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws ClassNotFoundException, UnmodifiableClassException {
        System.out.println(agentArgs); // 打印agentArgs，这个参数通过-javaagent:xx.jar=arg传进来
        MyTransformer transformer = new MyTransformer();
        instrumentation.addTransformer(transformer, true); // 添加自定义的transformer对象，转换字节码
        instrumentation.retransformClasses(Class.forName("A"));
    }
}
class MyTransformer implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // 只对指定的类进行增强，这里就增强一下自己写的A这个类
        if (className.endsWith("A")) {
            try {
                // 使用Javassist获取类定义
                ClassPool classPool = ClassPool.getDefault();
                classPool.insertClassPath(new LoaderClassPath(loader));
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                // 遍历类的所有方法，进行增强
                for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                    // 如果添加不是基础类型的变量：ctMethod.addLocalVariable("str", classPool.get("java.lang.String"));
                    ctMethod.addLocalVariable("startTime", CtClass.longType);
                    ctMethod.addLocalVariable("endTime", CtClass.longType);
                    ctMethod.addLocalVariable("duration", CtClass.longType);
                    // 在方法的开头插入计时逻辑
                    ctMethod.insertBefore("startTime = System.currentTimeMillis();");
                    // 在方法的结尾插入计时逻辑
                    ctMethod.insertAfter("endTime = System.currentTimeMillis();");
                    ctMethod.insertAfter("duration = endTime - startTime;");
                    ctMethod.insertAfter("System.out.println(\"Method execution time: \" + duration + \"ms\");");
                }
                // 返回增强后的类字节码
                return ctClass.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 对于其他类，不进行增强，直接返回原始的类字节码
        return classfileBuffer;
    }
}