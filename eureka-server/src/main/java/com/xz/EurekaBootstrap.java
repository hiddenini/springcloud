package com.xz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


/**
 * @EnableEurekaServer ->@Import(EurekaServerMarkerConfiguration.class) ->初始化bean Marker
 * <p>
 * 在org\springframework\cloud\spring-cloud-netflix-eureka-server\2.0.2.RELEASE\spring-cloud-netflix-eureka-server-2.0.2.RELEASE.jar jar包中
 * 的META-INF 中的spring.factories文件中设置了 这是springboot的机制 不细说了
 * @Import(EurekaServerInitializerConfiguration.class)
 * @ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class) EurekaServerAutoConfiguration
 * <p>
 * <p>
 * 当容器中有Marker.class 时才被注入
 * <p>
 * EurekaServerAutoConfiguration注入的bean
 * <p>
 * EurekaServerConfig 初始化eurekaServer的相关配置
 * <p>
 * EurekaController 初始化一些接口 用于获取eurekaServer的信息
 * <p>
 * PeerAwareInstanceRegistry 初始化集群注册表
 * <p>
 * eurekaServerContext 基于eurekaServer配置,注册表 集群节点集合 以及服务器实例初始化eurekaServer上下文
 * <p>
 * EurekaServerBootstrap  初始化spring cloud包装的eureka原生启动类
 * <p>
 * FilterRegistrationBean 初始化Jersey filter
 * <p>
 * EurekaServerInitializerConfiguration 实现了SmartLifecycle 会在初始化完成后根据 isAutoStartup()的值确定是否调用start()方法
 * start() eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
 * initEurekaEnvironment(); 初始化eureka 运行环境
 * initEurekaServerContext(); 初始化Eureka 上下文
 * <p>
 * 服务同步 从相邻的eureka节点中复制注册表
 * int registryCount = this.registry.syncUp();
 * <p>
 * 服务剔除
 * this.registry.openForTraffic(this.applicationInfoManager, registryCount);
 * <p>
 * registry 是在EurekaServerAutoConfiguration 中注入的一个InstanceRegistry
 * <p>
 * applicationInfoManager.setInstanceStatus(InstanceStatus.UP);
 * super.postInit(); 定时器
 * <p>
 * 开启定时剔除服务的任务 evictionTimer.schedule 服务剔除 EvictionTask
 * logger.info("Running the evict task with compensationTime {}ms", compensationTimeMs);
 * <p>
 * <p>
 * 服务端是如何响应客户端的请求的？ 获取注册信息接口 :ApplicationsResource.getContainers
 * <p>
 * responseCache.get(cacheKey)
 * <p>
 * getValue(final Key key, boolean useReadOnlyCache)
 * <p>
 * 首先从只读缓存中获取   readOnlyCacheMap.get(key)
 * <p>
 * 在ResponseCacheImpl类中的构造方法初始化时设置了一个定时任务  timer.schedule(getCacheUpdateTask() 每30秒刷新一次
 * <p>
 * 从读写缓存中去拿 不一致则覆盖
 * <p>
 * <p>
 * 若没有 则从读写缓存中获取    readWriteCacheMap.get(key) 180失效  然后回写只读缓存 readOnlyCacheMap.put(key, payload);
 * <p>
 * <p>
 * 若没有 则直接从内存注册表中获取  registry.getApplications()
 * <p>
 * readWriteCacheMap 在ResponseCacheImpl类中的构造方法初始化时  Value value = generatePayload(key)
 * <p>
 * payload = getPayLoad(key, registry.getApplications());
 * <p>
 * 注册接口:  ApplicationResource.addInstance
 * registry.register(info, "true".equals(isReplication)); 跟进去最后找到
 * <p>
 * public void register(InstanceInfo registrant, int leaseDuration, boolean isReplication)
 * <p>
 * Map<String, Lease<InstanceInfo>> gMap = registry.get(registrant.getAppName());
 * <p>
 * registry是一个 ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>
 * 一个服务名可能有多个实例
 * {
 * "USER-PROVIDER": {
 * "localhost:xxx:8080": "com.netflix.eureka.lease.lease@xxxx",
 * "localhost:xxx:8081": "com.netflix.eureka.lease.lease@xxxx",
 * "localhost:xxx:8082": "com.netflix.eureka.lease.lease@xxxx"
 * }
 * }
 * <p>
 * Lease<InstanceInfo> lease = new Lease<InstanceInfo>(registrant, leaseDuration);
 * <p>
 * gMap.put(registrant.getId(), lease);
 * <p>
 * 注册完成后将缓存失效  下次客户端来拿注册信息的时候就会拿最原始的信息 不会从读写缓存中拿了
 * invalidateCache(registrant.getAppName(), registrant.getVIPAddress(), registrant.getSecureVipAddress());
 * <p>
 * invalidate(Key... keys)
 * <p>
 * readWriteCacheMap.invalidate(key);
 * <p>
 * 只失效了读写缓存 那么只读缓存可能会暂时的不一致 这也是为什么eureka为什么是 ap 因为只保证最终以一致性
 * <p>
 * 一个bug 服务剔除不是默认的90s 而是 180s没有心跳的实例 eureka的bug
 *
 *
 *     public boolean isExpired(long additionalLeaseMs) {
 *         return (evictionTimestamp > 0 || System.currentTimeMillis() > (lastUpdateTimestamp + duration + additionalLeaseMs));
 *     }
 *
 *      将上次的续约时间设置为当前时间，但是这里加上了一个duration 所以在上面判断是否过期的时候 实际上是 2*duration
 *      public void renew() {
 *         lastUpdateTimestamp = System.currentTimeMillis() + duration;
 *
 *     }
 *
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(EurekaBootstrap.class);
    }
}
