package org.aggregateframework.discovery.registry.zookeeper;

import org.aggregateframework.discovery.registry.AbstractRegistryService;
import org.aggregateframework.discovery.registry.RegistryConfig;
import org.aggregateframework.exception.RegistryException;
import org.aggregateframework.utils.NetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 16:33
 */
public class ZookeeperRegistryServiceImpl extends AbstractRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryServiceImpl.class.getSimpleName());

    private static final String BASE_PATH = "/agg/server/";

    private ZookeeperRegistryProperties properties;

    private ZookeeperInstance instance;

    private String targetPathForClient;

    private String targetPathForDashboard;

    public ZookeeperRegistryServiceImpl(RegistryConfig registryConfig) {
        setClusterName(registryConfig.getClusterName());
        this.targetPathForClient = BASE_PATH + getClusterName()+"/client";
        this.targetPathForDashboard = BASE_PATH + getClusterName()+"/dashboard";
        this.properties = registryConfig.getZookeeperRegistryProperties();
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(properties.getConnectString())
                .sessionTimeoutMs(properties.getSessionTimeout())
                .connectionTimeoutMs(properties.getConnectTimeout())
                .retryPolicy(new ExponentialBackoffRetry(properties.getBaseSleepTime(), properties.getMaxRetries()));

        if (StringUtils.isNotEmpty(properties.getDigest())) {
            builder.authorization("digest", properties.getDigest().getBytes(StandardCharsets.UTF_8))
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        this.instance = new ZookeeperInstance(properties.getConnectString(), properties.getDigest(), builder.build());
    }

    @Override
    public void start() {
        CuratorFramework curator = this.instance.getCurator();
        curator.start();
        boolean connected;
        try {
            connected = curator.blockUntilConnected(properties.getConnectTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RegistryException("Cant connect to the zookeeper", e);
        }
        if (!connected) {
            throw new RegistryException("Cant connect to the zookeeper");
        }
    }

    @Override
    protected void doRegister(InetSocketAddress addressForClient, InetSocketAddress addressForDashboard) throws Exception {
        CuratorFramework curator = this.instance.getCurator();

        createParentNode(curator, false);
        this.instance.setRegisteredPathForClient(curator.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(targetPathForClient + "/node", NetUtils.parseSocketAddress(addressForClient).getBytes(StandardCharsets.UTF_8)));

        createParentNode(curator, true);
        this.instance.setRegisteredPathForDashboard(curator.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(targetPathForDashboard + "/node", NetUtils.parseSocketAddress(addressForDashboard).getBytes(StandardCharsets.UTF_8)));

        logger.info("Registered with zookeeper. {},{}", addressForClient, addressForDashboard);

        curator.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                try {
                    if (curator.checkExists().forPath(instance.getRegisteredPathForClient()) == null) {
                        instance.setRegisteredPathForClient(curator.create()
                                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                                .forPath(targetPathForClient + "/node", NetUtils.parseSocketAddress(addressForClient).getBytes(StandardCharsets.UTF_8)));
                    }

                    if (curator.checkExists().forPath(instance.getRegisteredPathForDashboard()) == null) {
                        instance.setRegisteredPathForDashboard(curator.create()
                                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                                .forPath(targetPathForDashboard + "/node", NetUtils.parseSocketAddress(addressForDashboard).getBytes(StandardCharsets.UTF_8)));
                    }

                    logger.info("Re-registered with zookeeper");
                } catch (Exception e) {
                    logger.warn("Failed to register with zookeeper");
                }
            }
        });
    }

    @Override
    protected void doSubscribe(boolean forDashboard) throws Exception {
        CuratorFramework curator = this.instance.getCurator();

        createParentNode(curator, forDashboard);

        String path = forDashboard ? targetPathForDashboard : targetPathForClient;

        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, path, false);
        pathChildrenCache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
            switch (pathChildrenCacheEvent.getType()) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                case CHILD_UPDATED:
                    try {
                        updateServiceList(curator, forDashboard);
                    } catch (Exception e) {
                        logger.warn("Failed to update server addresses", e);
                    }
                    break;
                default:
                    break;
            }
        });
        pathChildrenCache.start();
        updateServiceList(curator, forDashboard);
    }

    @Override
    public void close() {
        try {
            this.instance.getCurator().close();
        } catch (Exception e) {
            //ignore
        }
    }

    private void updateServiceList(CuratorFramework curator, boolean forDashboard) throws Exception {
        String path = forDashboard ? targetPathForDashboard : targetPathForClient;
        List<String> nodePaths = curator.getChildren().forPath(path);
        List<String> newServerAddresses = new ArrayList<>();
        for (String nodePath : nodePaths) {
            newServerAddresses.add(new String(curator.getData().forPath(path + "/" + nodePath), StandardCharsets.UTF_8));
        }
        setServerAddresses(newServerAddresses, forDashboard);
    }

    private void createParentNode(CuratorFramework target, boolean forDashboard) throws Exception {
        String path = forDashboard ? targetPathForDashboard : targetPathForClient;
        if (target.checkExists().forPath(path) == null) {
            try {
                target.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path, "".getBytes(StandardCharsets.UTF_8));
            } catch (KeeperException.NodeExistsException ignore) {
            }
        }
    }
}
