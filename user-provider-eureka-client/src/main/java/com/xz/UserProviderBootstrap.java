package com.xz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EurekaClientAutoConfiguration  入口 为啥是这个 还是springboot的spi机制
 *
 * @ConditionalOnBean(EurekaDiscoveryClientConfiguration.Marker.class)
 * @AutoConfigureAfter(name = {"org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
 * "org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration",
 * "org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration"})
 * EurekaClientAutoConfiguration
 *
 *
 * @ConditionalOnProperty(value = "eureka.client.enabled", matchIfMissing = true)
 * EurekaDiscoveryClientConfiguration  ->注入Marker 启动EurekaClientAutoConfiguration
 *
 * EurekaClientAutoConfiguration中注入了一些bean
 * EurekaClientConfigBean 初始化eurekaClient信息
 *
 * springcloud eureka 是对netflix eureka的封装
 *
 * DiscoveryClient  -->需要EurekaClient
 *
 * EurekaClient也在 EurekaClientAutoConfiguration 注入了 跟进去最后找到下面
 *
 * return new CloudEurekaClient(manager, config, this.optionalArgs,
 * 					this.context);
 *
 * super(applicationInfoManager, config, args);
 *
 *  this(applicationInfoManager, config, args, new Provider<BackupRegistry>()
 *
 * @Inject
 *DiscoveryClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs args,
 *Provider < BackupRegistry > backupRegistryProvider)
 *
 * 在这里面  initScheduledTasks
 *              one-shot action 一次性的动作 但是为啥是每隔30秒
 *             scheduler.schedule(
 *                     new TimedSupervisorTask(
 *                             "cacheRefresh",
 *                             scheduler,
 *                             cacheRefreshExecutor,
 *                             registryFetchIntervalSeconds,
 *                             TimeUnit.SECONDS,
 *                             expBackOffBound,
 *                             new CacheRefreshThread()
 *                     ),
 *                     registryFetchIntervalSeconds, TimeUnit.SECONDS);
 *         }
 *
 *TimedSupervisorTask 的run方法中
 *       finally {
 *              又重新调用了一次这个任务 所以说并不是 定时任务 而是自己调用自己
 *             if (!scheduler.isShutdown()) {
 *                 scheduler.schedule(this, delay.get(), TimeUnit.MILLISECONDS);
 *             }
 *         }
 *
 * 其中还有一个闪光点 就是超时之后将超时时间加倍(在最大值之内) 进入下一个周期的调用 一旦任务不再超时则还原
 * 另外一个就是使用cas保证多线程安全问题
 *
 * CacheRefreshThread 任务类 获取服务端注册表信息
 *
 * DiscoveryClient.refreshRegistry(remoteRegionsModified)
 *
 *
 *  boolean success = fetchRegistry(remoteRegionsModified);
 *
 *  首次全量更新   getAndStoreFullRegistry();
 *
 *  增量更新      getAndUpdateDelta(applications);
 *
 *  eurekaTransport.queryClient.getApplications(remoteRegionsRef.get())
 *
 *  客户端需要和服务端进行通信 去获取注册表信息 用的是 AbstractJerseyEurekaHttpClient  Jersey:RESTFUL请求服务JAVA框架
 *  serviceUrl http://localhost:8761/eureka/
 *  jerseyClient.resource(serviceUrl).path(urlPath) 全量 http://localhost:8761/eureka/apps
 *
 *  getApplicationsInternal("apps/delta", regions) 增量
 *
 *getAndUpdateDelta eurekaTransport.queryClient.getDelta(remoteRegionsRef.get() 调用服务端的增量接口
 *
 * 有一个亮点
 * reconcileHashCode = getReconcileHashCode(applications);
 *             合并之后的applications 和增量接口返回的applications 的hash比对 如果不相等 则需要再次调用接口
 *            if (!reconcileHashCode.equals(delta.getAppsHashCode()) || clientConfig.shouldLogDeltaDiff()) {
 *                 reconcileAndLogDifference(delta, reconcileHashCode);  // this makes a remoteCall
 *             }
 *
 *  make a remote call to the server for the full registry
 *
 *  localRegionApps.set(this.filterAndShuffle(apps));
 *
 *  将调用服务端接口的数据设置到
 *
 *在   initScheduledTasks() 中除了上面的拉去注册表 还有服务续约
 *
 *             // Heartbeat timer
 *             scheduler.schedule(
 *                     new TimedSupervisorTask(
 *                             "heartbeat",
 *                             scheduler,
 *                             heartbeatExecutor,
 *                             renewalIntervalInSecs,
 *                             TimeUnit.SECONDS,
 *                             expBackOffBound,
 *                             new HeartbeatThread()
 *                     ),
 *                     renewalIntervalInSecs, TimeUnit.SECONDS);
 *
 *和上面的流程一致 去调用服务端的接口
 *
 *还有 注册接口
 *instanceInfoReplicator.start(clientConfig.getInitialInstanceInfoReplicationIntervalSeconds());
 *
 *  Future next = scheduler.schedule(this, initialDelayMs, TimeUnit.SECONDS);
 *
 * 找到当前类的run方法
 *
 *  discoveryClient.register();     httpResponse = eurekaTransport.registrationClient.register(instanceInfo);
 *  又是去调用服务端的接口
 */


@SpringBootApplication
public class UserProviderBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderBootstrap.class, args);
    }
}
