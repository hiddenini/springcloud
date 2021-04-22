package com.xz.controller;

import com.xz.entity.User;
import com.xz.feign.UserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserFeign userFeign;


    @GetMapping("hello")
    public String hello() {
        return userFeign.hello();
    }

    @GetMapping("hi")
    public String hi() {
        return "hi";
    }

    @GetMapping("/getAllUser")
    List<User> getAllUser() {
        return userFeign.getAllUser();
    }
}
