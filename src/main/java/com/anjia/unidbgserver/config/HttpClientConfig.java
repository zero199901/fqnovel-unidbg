package com.anjia.unidbgserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class HttpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(HttpClientConfig.class);

    @Autowired
    private FQApiProperties fqApiProperties;

    private JedisPool jedisPool;

    private Proxy buildProxyFromHostPort(String type, String host, Integer port) {
        Proxy.Type proxyType = (type != null && type.equalsIgnoreCase("socks")) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
        return new Proxy(proxyType, new InetSocketAddress(host, port));
    }

    private void initJedisPoolIfNeeded() {
        try {
            if (jedisPool != null) {
                return;
            }
            FQApiProperties.RedisConfig rc = fqApiProperties.getRedis();
            if (rc == null || !Boolean.TRUE.equals(rc.getEnabled())) {
                return;
            }
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(4);
            poolConfig.setMaxIdle(2);
            poolConfig.setMinIdle(0);
            if (rc.getPassword() != null && !rc.getPassword().isEmpty()) {
                jedisPool = new JedisPool(poolConfig, rc.getHost(), rc.getPort(), rc.getTimeoutMs(), rc.getPassword(), rc.getDatabase());
            } else {
                jedisPool = new JedisPool(poolConfig, rc.getHost(), rc.getPort(), rc.getTimeoutMs(), null, rc.getDatabase());
            }
        } catch (Exception e) {
            log.warn("初始化Redis连接池失败，将忽略Redis代理: {}", e.getMessage());
        }
    }

    private String pickProxyFromRedis() {
        FQApiProperties.RedisConfig rc = fqApiProperties.getRedis();
        if (rc == null || !Boolean.TRUE.equals(rc.getEnabled())) {
            return null;
        }
        initJedisPoolIfNeeded();
        if (jedisPool == null) {
            return null;
        }
        String keysCfg = rc.getProxyKeys();
        if (keysCfg == null || keysCfg.trim().isEmpty()) {
            return null;
        }
        String[] keys = keysCfg.split(",");
        try (Jedis jedis = jedisPool.getResource()) {
            for (String rawKey : keys) {
                String key = rawKey.trim();
                try {
                    java.util.Set<String> members = jedis.smembers(key);
                    if (members != null && !members.isEmpty()) {
                        for (String m : members) {
                            if (m == null || m.trim().isEmpty()) continue;
                            String s = m.trim();
                            if (!s.contains(":")) {
                                s = s + ":" + rc.getDefaultProxyPort();
                            }
                            return s; // 简单取第一个
                        }
                    }
                } catch (Exception ex) {
                    log.warn("从Redis集合 {} 获取代理失败: {}", key, ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("读取Redis代理失败: {}", e.getMessage());
        }
        return null;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(15_000);
        requestFactory.setReadTimeout(30_000);

        boolean proxyEnabled = fqApiProperties.getProxy() != null && Boolean.TRUE.equals(fqApiProperties.getProxy().getEnabled());
        boolean useRedis = proxyEnabled && Boolean.TRUE.equals(fqApiProperties.getProxy().getUseRedis());

        if (proxyEnabled) {
            try {
                if (useRedis) {
                    String selected = pickProxyFromRedis();
                    if (selected != null) {
                        String[] hp = selected.split(":");
                        String host = hp[0];
                        Integer port = Integer.parseInt(hp[1]);
                        String type = fqApiProperties.getProxy().getType();
                        Proxy proxy = buildProxyFromHostPort(type, host, port);
                        requestFactory.setProxy(proxy);
                        log.info("RestTemplate 代理(来自Redis)已启用: {}:{} (type={})", host, port, type);
                    } else {
                        log.info("Redis未获取到代理，未设置代理");
                    }
                } else {
                    String host = fqApiProperties.getProxy().getHost();
                    Integer port = fqApiProperties.getProxy().getPort();
                    String type = fqApiProperties.getProxy().getType();
                    if (host != null && !host.isEmpty() && port != null && port > 0) {
                        Proxy proxy = buildProxyFromHostPort(type, host, port);
                        requestFactory.setProxy(proxy);
                        log.info("RestTemplate 代理已启用: {}:{} (type={})", host, port, type);
                    } else {
                        log.info("RestTemplate 代理配置启用，但 host/port 无效，已忽略");
                    }
                }
            } catch (Exception e) {
                log.warn("设置代理失败，将不使用代理: {}", e.getMessage());
            }
        } else {
            log.info("RestTemplate 未启用代理");
        }

        return builder.requestFactory(() -> requestFactory).build();
    }
}


