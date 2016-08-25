package com.roc.restaurant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 生产者消费者模型--餐厅模型
 *
 * @author Roc
 * @title DesignPatterns
 * @date 16/8/25
 */
public class Restaurant {

    Meal meal;
    ExecutorService executorService = Executors.newCachedThreadPool();
    WaitPerson waitPerson = new WaitPerson(this);
    Chef chef = new Chef(this);

    public Restaurant() {
        executorService.execute(chef);
        executorService.execute(waitPerson);
    }

    public static void main(String[] args) {
        new Restaurant();
    }


}

class Meal {
    private final int orderNum;

    public Meal(int orderNum) {
        this.orderNum = orderNum;
    }

    @Override
    public String toString() {
        return "Meal " + orderNum;
    }
}

class WaitPerson implements Runnable {

    private Restaurant restaurant;

    public WaitPerson(Restaurant r) {
        this.restaurant = r;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (restaurant.meal == null) {
                        wait();
                    }
                }

                System.out.println("WaitPerson got " + restaurant.meal);

                synchronized (restaurant.meal) {
                    restaurant.meal = null;
                    restaurant.chef.notifyAll();
                }

            }
        } catch (InterruptedException e) {
            System.out.println("WaitPerson interrupted!");
        }
    }
}

class Chef implements Runnable {

    private Restaurant restaurant;
    private int count = 0;

    public Chef(Restaurant r) {
        this.restaurant = r;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (restaurant.meal != null) {
                        wait();
                    }
                }
            }
            if (++count == 10) {
                System.out.println("Out of food, closing.");
                restaurant.executorService.shutdownNow();
            }
            System.out.println("Order up! ");
            synchronized (restaurant.waitPerson){
                restaurant.meal = new Meal(count);
                restaurant.waitPerson.notifyAll();
            }
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            System.out.println("Chef interrupted!");
        }
    }
}
