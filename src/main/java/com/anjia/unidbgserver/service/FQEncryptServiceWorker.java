package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("fqEncryptWorker")
public class FQEncryptServiceWorker extends Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private FQEncryptService fqEncryptService;

    @Autowired
    public void init(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
    }

    public FQEncryptServiceWorker() {
        super(WorkerPoolFactory.create(FQEncryptServiceWorker::new, Runtime.getRuntime().availableProcessors()));
    }

    public FQEncryptServiceWorker(WorkerPool pool) {
        super(pool);
    }

    @Autowired
    public FQEncryptServiceWorker(UnidbgProperties unidbgProperties,
                                    @Value("${spring.task.execution.pool.core-size:4}") int poolSize) {
        super(WorkerPoolFactory.create(FQEncryptServiceWorker::new, Runtime.getRuntime().availableProcessors()));
        this.unidbgProperties = unidbgProperties;
        if (this.unidbgProperties.isAsync()) {
            pool = WorkerPoolFactory.create(pool -> new FQEncryptServiceWorker(unidbgProperties.isDynarmic(),
                unidbgProperties.isVerbose(), pool), Math.max(poolSize, 4));
            log.info("FQ签名服务线程池大小为:{}", Math.max(poolSize, 4));
        } else {
            this.fqEncryptService = new FQEncryptService(unidbgProperties);
        }
    }

    public FQEncryptServiceWorker(boolean dynarmic, boolean verbose, WorkerPool pool) {
        super(pool);
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("FQ签名服务 - 是否启用动态引擎:{}, 是否打印详细信息:{}", dynarmic, verbose);
        this.fqEncryptService = new FQEncryptService(unidbgProperties);
    }

    /**
     * 异步生成FQ签名headers
     *
     * @param url 请求的URL
     * @param headers 请求头信息
     * @return 包含签名信息的CompletableFuture
     */
    @Async
    @SneakyThrows
    public CompletableFuture<Map<String, String>> generateSignatureHeaders(String url, String headers) {
        FQEncryptServiceWorker worker;
        Map<String, String> result;

        if (this.unidbgProperties.isAsync()) {
            // 异步模式使用工作池
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                result = worker.doWork(url, headers);
                pool.release(worker);
                break;
            }
        } else {
            // 同步模式直接使用当前实例
            synchronized (this) {
                result = this.doWork(url, headers);
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 异步生成FQ签名headers (重载方法，支持Map格式的headers)
     *
     * @param url 请求的URL
     * @param headerMap 请求头的Map
     * @return 包含签名信息的CompletableFuture
     */
    @Async
    @SneakyThrows
    public CompletableFuture<Map<String, String>> generateSignatureHeaders(String url, Map<String, String> headerMap) {
        FQEncryptServiceWorker worker;
        Map<String, String> result;

        if (this.unidbgProperties.isAsync()) {
            // 异步模式使用工作池
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                result = worker.doWorkWithMap(url, headerMap);
                pool.release(worker);
                break;
            }
        } else {
            // 同步模式直接使用当前实例
            synchronized (this) {
                result = this.doWorkWithMap(url, headerMap);
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 执行签名生成工作 (字符串格式headers)
     */
    private Map<String, String> doWork(String url, String headers) {
        return fqEncryptService.generateSignatureHeaders(url, headers);
    }

    /**
     * 执行签名生成工作 (Map格式headers)
     */
    private Map<String, String> doWorkWithMap(String url, Map<String, String> headerMap) {
        return fqEncryptService.generateSignatureHeaders(url, headerMap);
    }

    @SneakyThrows
    @Override
    public void destroy() {
        if (fqEncryptService != null) {
            fqEncryptService.destroy();
        }
    }
}
