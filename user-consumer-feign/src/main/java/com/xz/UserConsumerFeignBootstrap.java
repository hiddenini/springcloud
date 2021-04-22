package com.xz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * 从Spring Cloud Edgware开始，@EnableDiscoveryClient 或@EnableEurekaClient 可省略
 * <p>
 * 只需加上相关依赖，并进行相应配置，即可将微服务注册到服务发现组件上
 *
 * EnableFeignClients ->FeignClientsRegistrar
 *
 * registerDefaultConfiguration 扫描EnableFeignClients标签里面的信息并注册
 *
 * registerFeignClients  扫描所有@FeignClient注解的类注入spring容器
 *
 * registerFeignClient(registry, annotationMetadata, attributes);
 *
 * 将每一个FeignClient注解的类的信息交给FeignClientFactoryBean 代理 将其注入到容器中 (即使用FactoryBean)
 *BeanDefinitionBuilder definition = BeanDefinitionBuilder
 * 				.genericBeanDefinition(FeignClientFactoryBean.class);
 *
 *FeignClientFactoryBean ->getObject() -> getTarget()
 *
 *不带url 带name  loadBalance() feign集成ribbon
 *
 *Targeter targeter = get(context, Targeter.class);
 * return targeter.target(this, builder, context, target);
 *
 * 	return feign.target(target);
 *
 *    public <T> T target(Target<T> target) {
 *       return build().newInstance(target);
 *     }
 *
 *ReflectiveFeign.newInstance jdk动态代理
 *
 *
 *
 * 带url
 *
 *  * //这里获取的Client是LoadBalancerFeignClient，在DefaultFeignLoadBalancedConfiguration/FeignRibbonClientAutoConfiguration（版本不同初始化类不同）类中初始化。
 *  Client client = getOptional(context, Client.class);
 *
 *  		return (T) targeter.target(this, builder, context, new HardCodedTarget<>(
 * 				this.type, this.name, url)); 和上面类似也是用动态代理
 *
 * 总的来说:通过jdk的代理，当请求Feign Client的方法时会被拦截，代码在ReflectiveFeign类
 *
 * 在SynchronousMethodHandler类进行拦截处理，当被FeignClient的方法被拦截会根据参数生成RequestTemplate对象，该对象就是http请求的模板
 *
 *  SynchronousMethodHandler.invoke) ->  return executeAndDecode(template);
 *
 *  response = client.execute(request, options); client-->LoadBalancerFeignClient
 *
 *  其中Client组件是一个非常重要的组件，Feign最终发送request请求以及接收response响应，都是由Client组件完成的
 *  其中Client的实现类，只要有Client.Default，该类由HttpURLConnnection实现网络请求，另外还支持HttpClient、Okhttp
 *
 * 从FeignRibbonClientAutoConfiguration -->@Import DefaultFeignLoadBalancedConfiguration.class
 *
 * 可以直到容器中的client是LoadBalancerFeignClient
 *
 * .execute  --> executeWithLoadBalancer
 *
 *AbstractLoadBalancerAwareClient.executeWithLoadBalancer
 *
 * command.submit
 *
 *LoadBalancerCommand.submit
 *
 * selectServer()
 *
 *Server server = loadBalancerContext.getServerFromLoadBalancer(loadBalancerURI, loadBalancerKey);
 *
 *  getServerFromLoadBalancer.getServerFromLoadBalancer  中的 ILoadBalancer lb = getLoadBalancer();
 *
 *  lb是在 RibbonClientConfiguration配置ZoneAwareLoadBalancer
 *
 * https://www.cnblogs.com/lay2017/p/11954707.html
 *
 *  Server svc = lb.chooseServer(loadBalancerKey); -> BaseLoadBalancer.chooseServer 即回到ribbon那一套了
 *
 * loadBalancerContext，即Ribbon
 *
 *
 * 首先通过@EnableFeignCleints注解开启FeignCleint
 * 根据Feign的规则实现接口，并加@FeignCleint注解
 * 程序启动后，会进行包扫描，扫描所有的@ FeignCleint的注解的类，并将这些信息注入到ioc容器中。
 * 当接口的方法被调用，通过jdk的代理，来生成具体的RequesTemplate
 * RequesTemplate在生成Request
 * Request交给Client去处理，其中Client可以是HttpUrlConnection、HttpClient也可以是Okhttp
 * 最后Client被封装到LoadBalanceClient类，这个类结合类Ribbon做到了负载均衡。
 *
 * Feign内部默认整合了Ribbon，因为Feign发起的请求并没有指定实例，所以要用ribbon选取一台实例，这也就是客户端负载均衡
 *
 *
 * eureka中保存了 server信息 localRegionApps AtomicReference<Applications> localRegionApps
 *
 * ribbon 在初始化ZoneAwareLoadBalancer 时使用了eurekaClient去获取eurekaClient 的server信息缓存localRegionApps
 *
 *feign内置了ribbon 也是使用ZoneAwareLoadBalancer
 *
 */


@SpringBootApplication
@EnableFeignClients(basePackages = "com.xz")
//@EnableDiscoveryClient
public class UserConsumerFeignBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(UserConsumerFeignBootstrap.class, args);
    }
}
