package com.xz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @LoadBalanced ->LoadBalancerAutoConfiguration
 *
 * LoadBalancerAutoConfigurationz红注入了一些bean
 *
 *LoadBalancerInterceptor
 *
 * RestTemplateCustomizer
 *
 * SmartInitializingSingleton
 *
 *  @LoadBalanced
 *  @Autowired(required = false)
 * 	private List<RestTemplate> restTemplates = Collections.emptyList();
 *
 * 	这个集合里面装了所有加了@LoadBalanced注解的restTemplate
 *
 *
 *  LoadBalancerInterceptor 中的intercept方法 -->this.loadBalancer.execute(serviceName, requestFactory.createRequest(request, body, execution));
 *
 * this.loadBalancer是一个 RibbonLoadBalancerClient  RibbonLoadBalancerClient是在
 *
 * @AutoConfigureBefore({LoadBalancerAutoConfiguration.class, AsyncLoadBalancerAutoConfiguration.class})
 * RibbonAutoConfiguration中被初始化的  这个类是在LoadBalancerAutoConfiguration.class初始化之前再被初始化的
 *
 * 所以说RibbonLoadBalancerClient 在这个类中被初始化了 然后在LoadBalancerAutoConfiguration初始化时就可以使用了
 *
 *
 *
 *this.loadBalancer.execute -> RibbonLoadBalancerClient.execute(String serviceId, LoadBalancerRequest<T> request)
 *
 * ILoadBalancer loadBalancer = getLoadBalancer(serviceId); 获取负载均衡器 loadBalancer
 *
 *ILoadBalancer 是从容器中获取的 那么是在哪里初始化的呢?
 *
 * 一般来说可以从同目录下面的xxxAutoConfiguration  或者xxxConfiguration类中去找
 *
 * 发现是在RibbonClientConfiguration 中被注入到容器中的 实际上是一个ZoneAwareLoadBalancer
 *
 *Server server = getServer(loadBalancer);
 *
 * return super.chooseServer(key);
 *
 *  BaseLoadBalancer.Server chooseServer(Object key)
 *
 *  return rule.choose(key); rule -->ZoneAvoidanceRule
 *
 *  PredicateBasedRule.choose 轮询  lb.getAllServers()  从loadBalancer获取到了所有的服务列表
 *
 *  那么是在哪里设置进去的？
 *
 *  在初始化ZoneAwareLoadBalancer 时 跟进去调用了 restOfInit(clientConfig);
 *
 * enableAndInitLearnNewServersFeature(); ribbon定时更新eureka实例列表
 *
 * serverListUpdater.start(updateAction);
 *
 *PollingServerListUpdater.start ->  updateAction.doUpdate(); -> updateListOfServers()
 *
 *
 *
 * updateListOfServers();  获取所有eureka实例列表
 *
 *  servers = serverListImpl.getUpdatedListOfServers();
 *
 *  DomainExtractingServerList中
 *
 *  List<DiscoveryEnabledServer> servers = setZones(this.list.getUpdatedListOfServers());
 *
 *  getUpdatedListOfServers() DiscoveryEnabledNIWSServerList
 *
 *  obtainServersViaDiscovery()
 *
 * EurekaClient eurekaClient = eurekaClientProvider.get();
 *
 * List<InstanceInfo> listOfInstanceInfo = eurekaClient.getInstancesByVipAddress(vipAddress, isSecure, targetRegion);
 *
 *  applications = this.localRegionApps.get();
 *
 *  restTemplate.getForObject("http://USER-PROVIDER/getUser/" + id, User.class);
 *
 *  这个请求是如何被拦截的?
 *
 *1--RestTemplate.execute
 *
 * 2--RestTemplate.doExecute
 *
 * 3--ClientHttpRequest request = createRequest(url, method);
 *
 *4--HttpAccessor.createRequest
 *
 * 5--AbstractClientHttpRequestFactoryWrapper.createRequest
 *
 * 6--InterceptingClientHttpRequestFactor ->return new InterceptingClientHttpRequest(requestFactory, this.interceptors, uri, httpMethod);
 *
 * 7--AbstractClientHttpRequest.execute() ->executeInternal(this.headers)
 *
 * 8--AbstractBufferingClientHttpRequest.executeInternal ->ClientHttpResponse result = executeInternal(headers, bytes);
 *
 * 9--InterceptingClientHttpRequest ->return requestExecution.execute(this, bufferedOutput); 实现了ClientHttpRequestExecution的execute方法
 *
 * 10--InterceptingClientHttpRequest.execute ->
 *
 *          if (this.iterator.hasNext()) {
 *              遍历拦截器 执行其拦截方法
 * 				ClientHttpRequestInterceptor nextInterceptor = this.iterator.next();
 * 				return nextInterceptor.intercept(request, body, this);
 *                        }
 *
 * 11->LoadBalancerInterceptor.intercept
 *
 * 12--RibbonLoadBalancerClient.execute
 *
 * 13--拿到server 将服务名称转换为ip
 *
 * 14--结束后回到InterceptingClientHttpRequest.execute 拦截器遍历完成 开始执行http请求
 *
 * 15--return delegate.execute();
 *
 * 16--AbstractBufferingClientHttpRequest. ClientHttpResponse result = executeInternal(headers, bytes)
 *
 * 17--SimpleBufferingClientHttpRequest.executeInternal
 *
 * 18--SimpleClientHttpRequestFactory this.connection.connect(); 进行http请求
 *
 * 19--RestTemplate  responseExtractor.extractData(response) 解析成user
 *
 */

@SpringBootApplication
public class UserConsumerBootStrap {
    public static void main(String[] args) {
        SpringApplication.run(UserConsumerBootStrap.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
