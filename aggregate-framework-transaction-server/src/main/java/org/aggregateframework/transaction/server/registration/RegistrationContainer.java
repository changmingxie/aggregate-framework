package org.aggregateframework.transaction.server.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aggregateframework.ha.registry.Registration;
import org.aggregateframework.ha.zookeeper.ZkPathConstants;
import org.aggregateframework.transaction.server.model.Registration0;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lee on 2020/6/29 16:49.
 * aggregate-framework
 */

@Component
@Slf4j
public class RegistrationContainer implements SmartLifecycle, ApplicationEventPublisherAware {
    
    
    private static final String ROOT = ZkPathConstants.REGISTRATION_ROOT;
    
    private PathChildrenCache cache;
    
    private final CuratorFramework          curator;
    private final ObjectMapper              _jackson;
    private final AtomicBoolean             running = new AtomicBoolean(false);
    private       ApplicationEventPublisher applicationEventPublisher;
    
    
    public RegistrationContainer(CuratorFramework curator, ObjectMapper jackson) {
        this.curator  = curator;
        this._jackson = jackson;
    }
    
    @Override
    @SneakyThrows
    public void start() {
        
        if (running.compareAndSet(false, true)) {
            // 缓存数据
            cache = new PathChildrenCache(curator, ROOT, true);
            
            cache.getListenable()
                 .addListener((client, event) -> {
                     log.info("@@@==== {} ==> {}}", event.getType(), event.getData().getPath());
                     switch (event.getType()) {
                    
                         case CHILD_ADDED:
                         case CHILD_UPDATED:
                             handleModified(event.getData());
                             break;
                         case CHILD_REMOVED:
                             handleRemoved(event.getData());
                             break;
//                         case INITIALIZED:
//                             handleModified(event.getInitialData());
//                             break;
                     }
                
                 });
            
            try {
                cache.start();
            } catch (Exception e) {
                ExceptionUtils.rethrow(e);
            }
            
        }
        
        
    }
    
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                cache.close();
            } catch (IOException e) {
                ExceptionUtils.rethrow(e);
            }
        }
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    private void handleModified(List<ChildData> datas) throws Exception {
        for (ChildData d : datas) {
            handleModified(d);
        }
        
    }
    
    
    private void handleModified(ChildData data) throws Exception {
        log.info("@@@====handleModified  ==> {}", data.getPath());
        try {
            
            Registration registration = _jackson.readValue(data.getData(), Registration.class);
            publish(registration);
            
        } catch (JedisException jce) {
            
            ExceptionUtils.rethrow(jce);
            
        } catch (Exception ig) {
            
            Registration0 registration0 = _jackson.readValue(data.getData(), Registration0.class);
            publish(registration0);
        }
        
        
    }
    
    private void handleRemoved(ChildData data) {
        String path = data.getPath();
        log.info("handleRemoved  ==> {}", path);
        publish(ZKPaths.getNodeFromPath(path));
    }
    
    
    public void add(Registration registration) throws Exception {
        String path = ZKPaths.makePath(ROOT, registration.getDomain());
        byte[] data = _jackson.writeValueAsBytes(registration);
        int max = 2;
        boolean ok = false;
        for (int i = 0; i < max && !ok; i++) {
            try {
                curator.create()
                       .creatingParentsIfNeeded()
                       .withMode(CreateMode.PERSISTENT)
                       .forPath(path, data);
                ok = true;
                log.info("Succeed to register infrastructure [{}] for path {}", registration, path);
            } catch (KeeperException.NodeExistsException ignore) {
                curator.delete().forPath(path);
            }
        }
    }
    
    public void remove(String domain) throws Exception {
        
        String path = ZKPaths.makePath(ROOT, domain);
        try {
            curator.delete().forPath(path);
        } catch (KeeperException.NoNodeException ignore) {
        
        }
        
    }
    
    
    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    
    private void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
