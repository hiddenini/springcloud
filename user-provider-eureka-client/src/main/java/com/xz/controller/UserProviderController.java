package com.xz.controller;

import com.xz.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserProviderController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/getUser/{id}")
    public User getUserById(@PathVariable int id) {
        User user = new User();
        user.setId(id);
        user.setAge(11);
        user.setName("xz" + port);
        return user;
    }

    @GetMapping("hello")
    public String hello() {
        return "hello";
    }


    @GetMapping("/getAllUser")
    public List<User> getAllUser() {
        System.out.println("UserProviderController Enter getAllUser");
        User user1 = new User();
        user1.setAge(11);
        user1.setName("xz" + port);

        User user2 = new User();
        user2.setAge(22);
        user2.setName("xt" + port);

        List<User> list = new ArrayList<>();
        list.add(user1);
        list.add(user2);
        return list;
    }

}
