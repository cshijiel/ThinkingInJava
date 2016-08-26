package com.roc.restaurant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者消费者模型--餐厅模型
 *
 * @author Roc
 * @title 生产者消费者模型
 * @date 16/8/25
 */
public class Restaurant {

    Meal meal;
    WaitPerson waitPerson = new WaitPerson(this);
    Chef chef = new Chef(this);
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ExecutorService executorService = Executors.newCachedThreadPool();

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
                restaurant.lock.lock();
                while (restaurant.meal == null) {
                    restaurant.condition.await();
                }

                TimeUnit.MILLISECONDS.sleep(500 + ((int) (Math.random() * 200)));
                System.out.println("WaitPerson got " + restaurant.meal);

                restaurant.meal = null;
                restaurant.condition.signalAll();

            }
        } catch (InterruptedException e) {
            System.out.println("WaitPerson interrupted!");
        } finally {
            restaurant.lock.unlock();
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
                restaurant.lock.lock();
                while (restaurant.meal != null) {
                    restaurant.condition.await();
                }


                if (++count == 10) {
                    System.out.println("Out of food, closing.");
                    restaurant.executorService.shutdownNow();
                }
                System.out.println("Order up! ");
                restaurant.meal = new Meal(count);
                restaurant.condition.signalAll();
                TimeUnit.MILLISECONDS.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("Chef interrupted!");
            System.exit(0);
        } finally {
            restaurant.lock.unlock();
        }
    }
}
