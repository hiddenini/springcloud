package com.xz.controller;

import com.xz.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserProviderController {

    @Value("${server.port}")
    private String port;

    @GetMapping("getUser/{id}")
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
}
