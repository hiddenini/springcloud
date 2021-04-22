package com.xz.controller;

import com.xz.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping("getUser")
    public User findById(int id) {
        User user = this.restTemplate.getForObject("http://USER-PROVIDER/getUser/" + id, User.class);
        System.out.println(user);
        System.out.println(user);
        System.out.println(user);
        System.out.println(user);
        return user;
    }



}
