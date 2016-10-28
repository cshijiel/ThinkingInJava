package com.roc.thread.wait;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Roc
 * @title thinkinginjava
 * @date 2016/10/28
 */
public class Client {
    public static void main(String[] args) {
        for (int index = 0; index < 100; index++) {
            ExecutorService executorService = Executors.newFixedThreadPool(8);
            CountDownLatch latch = new CountDownLatch(3);

            Boss boss = new Boss(latch);
            executorService.execute(boss);
            for (int i = 0; i < 3; i++) {
                Employee employee = new Employee("employee" + i, latch);
                executorService.execute(employee);
            }

            executorService.shutdown();
            try {
                executorService.awaitTermination(10000, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}

/**
 * 员工
 */
class Employee implements Runnable {

    private String name;
    private CountDownLatch latch;

    public Employee(String name, CountDownLatch latch) {
        this.name = name;
        this.latch = latch;
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
//        System.out.println(thread + name + " 员工正在处理");
        try {
            thread.sleep(300 + (int) (Math.random() * 2000));
            System.out.println(thread + name + " 员工处理结束");
            latch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * 老板
 */
class Boss implements Runnable {

    private CountDownLatch latch;

    public Boss(CountDownLatch latch) {
        this.latch = latch;
    }


    @Override
    public void run() {
        long t = 0;
        try {
            long start = System.currentTimeMillis();
            latch.await();
            t = System.currentTimeMillis() - start;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Boss开始处理...wait: " + t);
    }
}
