package com.xz.feign;

import com.xz.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 在项目中定义接口 使用@FeignClient注解 就不需要再引入其他模块的依赖了
 *
 * 也可以其他模块定义好 直接通过依赖调用
 */
@FeignClient(name = "USER-PROVIDER")
public interface UserFeign {

    @GetMapping("hello")
    String hello();

    @GetMapping("/getAllUser")
    List<User> getAllUser();
}
