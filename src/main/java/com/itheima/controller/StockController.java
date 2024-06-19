package com.itheima.controller;

import com.itheima.entity.MyLock;
import com.itheima.mapper.MyLockMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

@RestController
public class StockController {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Autowired
    private MyLockMapper myLockMapper;


    @Autowired
    private CuratorFramework curatorFramework;

    @Autowired
    private RedissonClient redissonClient;


    @GetMapping("/lock")
    public String stock(){

/*        int stock = Integer.parseInt(redisTemplate.opsForValue().get("lock"));

        if (stock >0){
            stock --;
            redisTemplate.opsForValue().set("stock",stock+"");
            System.out.println("库存扣减成功，剩余库存"+stock);

        }else{
            System.out.println("库存不足！！！");
        }
        return "Ok";*/

/*        int stock = Integer.parseInt(redisTemplate.opsForValue().get("lock"));

        synchronized (this){
            if (stock >0){
                stock --;
                redisTemplate.opsForValue().set("stock",stock+"");
                System.out.println("库存扣减成功，剩余库存"+stock);

            }else{
                System.out.println("库存不足！！！");
            }
        }
        return "Ok";
    }*/

//数据库实现分布式锁


        //插入数据，如果不抛异常则表示插入成功，即获得锁


        //释放锁
        MyLock entity  = new MyLock();

        entity.setMethodName("mylock");

        try {
            //插入数据，如果不抛异常则表示插入成功，即获得锁

            myLockMapper.insert(entity);

            int stock = Integer.parseInt(redisTemplate.opsForValue().get("lock"));


            if (stock >0){
                stock --;
                redisTemplate.opsForValue().set("stock",stock+"");
                System.out.println("库存扣减成功，剩余库存"+stock);

            }else{
                System.out.println("库存不足！！！");
            }

            //释放锁
            myLockMapper.deleteByMethodName(entity.getMethodName());
        } catch (NumberFormatException e) {

            System.out.println("没有获取锁，不能执行减库存操作！！！");
        }

//ZooKeeper实现分布式锁
        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, "/mylock");

        try {
            //尝试获得锁
            boolean locked  = interProcessMutex.acquire(0, TimeUnit.SECONDS);

            if (locked) {
                int stock = Integer.parseInt(redisTemplate.opsForValue().get("lock"));

                if (stock >0){
                    stock --;
                    redisTemplate.opsForValue().set("stock",stock+"");
                    System.out.println("库存扣减成功，剩余库存"+stock);

                }else{
                    System.out.println("库存不足！！！");
                }
                //释放锁

                interProcessMutex.release();

            }else {
                System.out.println("没有获取锁，不能执行减库存操作！！！");
            }


        } catch (Exception e) {
            System.out.println("没有获取锁，不能执行减库存操作！！！");
        }

//Redis实现分布式锁

        try {
            //获取当前线程
            String threedId = Thread.currentThread().getId()+"";

            //尝试获得锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent("mylock", threedId,5000,TimeUnit.MINUTES);

            if (locked) {
                int stock = Integer.parseInt(redisTemplate.opsForValue().get("lock"));

                if (stock >0){
                    stock --;
                    redisTemplate.opsForValue().set("stock",stock+"");
                    System.out.println("库存扣减成功，剩余库存"+stock);

                }else{
                    System.out.println("库存不足！！！");
                }
                //释放锁
                redisTemplate.delete("mylock");
            }else {
                System.out.println("没有获取锁，不能执行减库存操作！！！");
            }


        } catch (Exception e) {
            System.out.println("没有获取锁，不能执行减库存操作！！！");
        }
//Redisson分布式锁使用方式
        //尝试获得锁
        RLock lock  = redissonClient.getLock("mylock");

        try {
               //尝试加锁，如果加锁成功则后续程序继续执行，如果加锁不成功则阻塞等待
               lock.lock(5000,TimeUnit.MILLISECONDS);

                int stock = Integer.parseInt(redisTemplate.opsForValue().get("lock"));

                if (stock >0){
                    stock --;
                    redisTemplate.opsForValue().set("stock",stock+"");

                    System.out.println("库存扣减成功，剩余库存"+stock);

                }else{
                    System.out.println("库存不足！！！");
                }
                //释放锁
                redisTemplate.delete("mylock");



        } catch (Exception e) {
            System.out.println("没有获取锁，不能执行减库存操作！！！");
        }finally {
          lock.unlock();
        }

        return "Ok";

    }

}
