package org.aggregateframework;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.aggregateframework.discovery.registry.RegistryRole;
import org.aggregateframework.exception.SystemException;
import org.aggregateframework.storage.*;
import org.aggregateframework.support.FactoryBuilder;
import org.aggregateframework.constants.RemotingServiceCode;
import org.aggregateframework.discovery.loadbalance.LoadBalanceFactory;
import org.aggregateframework.discovery.loadbalance.LoadBalanceServcie;
import org.aggregateframework.discovery.registry.RegistryFactory;
import org.aggregateframework.discovery.registry.RegistryService;
import org.aggregateframework.processor.ClientRecoveryExecutor;
import org.aggregateframework.processor.ClientRecoveryProcessor;
import org.aggregateframework.recovery.RecoveryExecutor;
import org.aggregateframework.recovery.RecoveryScheduler;
import org.aggregateframework.recovery.TransactionStoreRecovery;
import org.aggregateframework.remoting.RemotingClient;
import org.aggregateframework.remoting.RequestProcessor;
import org.aggregateframework.remoting.netty.NettyRemotingClient;
import org.aggregateframework.remoting.netty.ServerAddressLoader;
import org.aggregateframework.remoting.protocol.RemotingCommand;
import org.aggregateframework.remoting.protocol.RemotingCommandCode;
import org.aggregateframework.transaction.repository.DefaultTransactionRepository;
import org.aggregateframework.transaction.repository.TransactionRepository;
import org.aggregateframework.transaction.serializer.*;
import org.aggregateframework.transaction.serializer.json.FastjsonTransactionSerializer;
import org.aggregateframework.transaction.serializer.kryo.RegisterableKryoTransactionSerializer;
import org.aggregateframework.storage.domain.DomainStore;
import org.aggregateframework.utils.NetUtils;
import org.aggregateframework.utils.StopUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by changming.xie on 11/25/17.ß
 */
public class AggClient implements AggService {

    static final Logger logger = LoggerFactory.getLogger(AggClient.class.getSimpleName());
    //attribute inject
    private ClientConfig clientConfig = ClientConfig.DEFAULT;
    //attribute inject
    private TransactionStorage transactionStorage;
    //attribute inject
    private RecoveryExecutor recoveryExecutor;
    //attribute inject
    private RequestProcessor requestProcessor;

    private TransactionStoreRecovery transactionStoreRecovery;

    private ExecutorService requestProcessExecutor;

    private TransactionStoreSerializer transactionStoreSerializer;

    private RecoveryScheduler scheduler;

    private RemotingClient remotingClient;

    //attribute inject
    private TransactionRepository transactionRepository;

    private TransactionSerializer transactionSerializer;

    private RemotingCommandSerializer remotingCommandSerializer;

    private RegistryService registryService;

    private LoadBalanceServcie loadBalanceServcie;

    private volatile boolean isStarting = false;

    public AggClient(ClientConfig clientConfig) {
        if (clientConfig != null) {
            this.clientConfig = clientConfig;
        }

        if (this.clientConfig.getSerializerType() == null) {
            throw new SystemException("serializerType should not be null");
        }
        switch (this.clientConfig.getSerializerType()) {
            case KRYO: {
                if (this.clientConfig.getKryoPoolSize() > 0) {
                    this.transactionSerializer = new RegisterableKryoTransactionSerializer(this.clientConfig.getKryoPoolSize());
                } else {
                    this.transactionSerializer = new RegisterableKryoTransactionSerializer();
                }
                break;
            }
            case FASTJSON: {
                this.transactionSerializer = new FastjsonTransactionSerializer();
                break;
            }
            case CUSTOMIZED: {
                if (StringUtils.isBlank(this.clientConfig.getTransactionSerializerClassName())) {
                    throw new SystemException("transactionSerializerClassName should not be null");
                }
                try {
                    Class<? extends TransactionSerializer> transactionSerializerClass = (Class<? extends TransactionSerializer>) Class.forName(this.clientConfig.getTransactionSerializerClassName());
                    this.transactionSerializer = FactoryBuilder.factoryOf(transactionSerializerClass).getInstance();
                } catch (ClassNotFoundException e) {
                    throw new SystemException(e);
                }
                break;
            }
            default:
                throw new SystemException(String.format("invalid serializerType: %s", this.clientConfig.getSerializerType()));
        }

        this.transactionStoreSerializer = new DefaultTransactionStoreSerializer();
        this.remotingCommandSerializer = new DefaultRemotingCommandSerializer();

        if (this.clientConfig.getRegistryRole() == RegistryRole.DASHBOARD) {
            this.registryService = RegistryFactory.getInstance(this.clientConfig);
        } else if (this.clientConfig.getStorageType() == StorageType.REMOTING) {
            this.registryService = RegistryFactory.getInstance(this.clientConfig);
            this.loadBalanceServcie = LoadBalanceFactory.getInstance(this.clientConfig);
            remotingClient = new NettyRemotingClient(this.remotingCommandSerializer, this.clientConfig,
                    new ServerAddressLoader() {
                        @Override
                        public String selectOneAvailableAddress() {
                            return loadBalanceServcie.select(registryService.lookup(false));
                        }

                        @Override
                        public List<String> getAllAvailableAddresses() {
                            return registryService.lookup(false);
                        }

                        @Override
                        public boolean isAvailableAddress(String address) {
                            List<String> serverAddresses = registryService.lookup(false);
                            return serverAddresses.contains(address);
                        }
                    });
        }

        this.transactionStorage = TransactionStorageFactory.create(transactionStoreSerializer, this.clientConfig, true);

        this.transactionRepository = new DefaultTransactionRepository(this.clientConfig.getDomain(), transactionSerializer, this.transactionStorage);

        this.scheduler = new RecoveryScheduler(this.clientConfig);

        this.recoveryExecutor = new ClientRecoveryExecutor(transactionSerializer, this.transactionRepository);

        this.requestProcessor = new ClientRecoveryProcessor(this.transactionStoreSerializer, this.recoveryExecutor);

        if (transactionRepository.supportRecovery() && this.clientConfig.isRecoveryEnabled()) {
            transactionStoreRecovery = new TransactionStoreRecovery(this.transactionStorage, this.recoveryExecutor, this.clientConfig);
        }
    }

    @Override
    @PostConstruct
    public void start() throws Exception {
        this.isStarting = true;

        if (this.clientConfig.getRegistryRole() == RegistryRole.DASHBOARD) {
            try {
                this.registryService.start();
                this.registryService.subscribe(true);
            } catch (Exception e) {
                logger.error("failed to initialize registryService, stop the application!", e);
                StopUtils.stop();
            }
        }else if (this.clientConfig.getStorageType() == StorageType.REMOTING) {
            try {
                this.registryService.start();
                this.registryService.subscribe(false);
            } catch (Exception e) {
                logger.error("failed to initialize registryService, stop the application!", e);
                StopUtils.stop();
            }
            initializeRemotingClient();
        } else {

            registerDomain(this.clientConfig.getDomain());
            if (transactionRepository.supportRecovery() && this.clientConfig.isRecoveryEnabled()) {
                scheduler.registerScheduleAndStartIfNotPresent(this.clientConfig.getDomain());
            }
        }

        this.isStarting = false;
    }

    @Override
    @PreDestroy
    public void shutdown() throws Exception {

        if (this.clientConfig.getStorageType() == StorageType.REMOTING) {
            this.remotingClient.shutdown();
            this.remotingClient = null;

            if (this.registryService != null) {
                this.registryService.close();
                this.registryService = null;
            }
            this.loadBalanceServcie = null;
        }

        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (this.requestProcessExecutor != null) {
            this.requestProcessExecutor.shutdown();
        }

        if (this.transactionStoreRecovery != null) {
            this.transactionStoreRecovery.close();
        }

        if (this.transactionRepository != null) {
            this.transactionRepository.close();
        }

        this.transactionSerializer = null;
        this.transactionStoreSerializer = null;
        this.remotingCommandSerializer = null;

        this.recoveryExecutor = null;
        this.requestProcessor = null;
        this.transactionStorage = null;
    }

    @Override
    public TransactionStoreRecovery getTransactionStoreRecovery() {
        return transactionStoreRecovery;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public TransactionStorage getTransactionStorage() {
        return transactionStorage;
    }

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public RecoveryScheduler getScheduler() {
        return scheduler;
    }

    private void initializeRemotingClient() {

        if (this.transactionStorage instanceof RetryableTransactionStorage) {
            RemotingTransactionStorage remotingTransactionStorage = ((RemotingTransactionStorage) ((RetryableTransactionStorage) this.transactionStorage).getTargetTransactionStorage());
            remotingTransactionStorage.setRemotingClient(this.remotingClient);
        } else {
            ((RemotingTransactionStorage) this.transactionStorage).setRemotingClient(this.remotingClient);
        }

        this.requestProcessExecutor = new ThreadPoolExecutor(this.clientConfig.getRequestProcessThreadSize(),
                clientConfig.getRequestProcessThreadSize(),
                1000L * 60, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(this.clientConfig.getRequestProcessThreadQueueCapacity()),
                new ThreadFactory() {
                    private final AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("StoreTransactionThread_%d", threadIndex.getAndIncrement()));
                    }
                });

        this.remotingClient.registerDefaultProcessor(requestProcessor, this.requestProcessExecutor);

        this.remotingClient.registerChannelHandlers(new RegisterToServerHandler());

        this.remotingClient.start();

        registerToServer();
    }

    private void registerToServer() {
        RemotingCommand registerCommand = RemotingCommand.createCommand(RemotingCommandCode.SERVICE_REQ, null);
        registerCommand.setServiceCode(RemotingServiceCode.REGISTER);
        registerCommand.setBody(clientConfig.getDomain().getBytes());
        try {
            remotingClient.invokeOneway(registerCommand, clientConfig.getRequestTimeoutMillis());
        } catch (Exception e) {
            logger.error("failled to register to server", e);
        }
    }

    private void registerDomain(String domain) {
        if (transactionStorage.supportStorageRecoverable()) {
            ((StorageRecoverable) transactionStorage).registerDomain(new DomainStore(domain, clientConfig.getMaxRetryCount()));
        } else {
            logger.warn("transactionStorage:{} not StorageRecoverable, do not regist domain", transactionStorage.getClass().getSimpleName());
        }
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    @ChannelHandler.Sharable
    class RegisterToServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            //register to server if connected
            if (!isStarting) {
                ctx.channel().eventLoop().execute(() -> registerToServer(NetUtils.parseSocketAddress(ctx.channel().remoteAddress())));
            }
        }

        private void registerToServer(String address) {
            RemotingCommand registerCommand = RemotingCommand.createCommand(RemotingCommandCode.SERVICE_REQ, null);
            registerCommand.setServiceCode(RemotingServiceCode.REGISTER);
            registerCommand.setBody(clientConfig.getDomain().getBytes());
            try {
                remotingClient.invokeOneway(address, registerCommand, clientConfig.getRequestTimeoutMillis());
            } catch (Exception e) {
                logger.error("failled to register to server", e);
            }
        }
    }
}
