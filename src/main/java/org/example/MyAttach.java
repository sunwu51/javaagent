package org.example;

import com.sun.tools.attach.VirtualMachine;

import java.util.Scanner;

public class MyAttach {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String pid = scanner.nextLine().trim();

        // 连接到目标Java进程
        VirtualMachine vm = VirtualMachine.attach(pid);

        // 加载Java Agent
        String agentJarPath = "C:\\Users\\sunwu\\Desktop\\code\\untitled\\target\\untitled-1.0-SNAPSHOT-shaded.jar";
        String agentArgs = "hello";
        vm.loadAgent(agentJarPath, agentArgs);
        // 断开与目标进程的连接
        vm.detach();
    }
}

