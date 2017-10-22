package cn.patterncat.converter;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

/**
 * Created by patterncat on 2017-10-22.
 */
@Configuration
@ConditionalOnClass({DocumentConverter.class})
@ConditionalOnProperty(
        prefix = "jodconverter",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
@EnableConfigurationProperties(JodConverterProperties.class)
public class ConvertOfficeAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertOfficeAutoConfiguration.class);

    private final JodConverterProperties properties;

    public ConvertOfficeAutoConfiguration(final JodConverterProperties properties) {
        this.properties = properties;
    }

    @Bean
    public PooledOfficeFactory pooledDriverFactory(){
        PooledOfficeFactory factory = new PooledOfficeFactory(properties);
        return factory;
    }

    @Bean(destroyMethod = "close") //default is close() or shutdown()
    public OfficePool officePool(){
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(properties.getPoolMaxTotal());
        poolConfig.setMinIdle(properties.getPoolMinIdle()); //init pool size
        poolConfig.setMaxIdle(properties.getPoolMaxIdle());
//        poolConfig.setTestOnBorrow(true);
//        poolConfig.setTestOnCreate();
//        poolConfig.setTestOnReturn();
//        poolConfig.setTestWhileIdle();
//        poolConfig.setBlockWhenExhausted();
//        poolConfig.setMaxWaitMillis();
//        poolConfig.setLifo();
//        poolConfig.setMinEvictableIdleTimeMillis();
//        poolConfig.setNumTestsPerEvictionRun();
//        poolConfig.setSoftMinEvictableIdleTimeMillis();
//        poolConfig.setEvictionPolicyClassName();
//        poolConfig.setFairness();
        return new OfficePool(pooledDriverFactory(),poolConfig);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public OfficeManager officeManager(OfficePool officePool) {
        OfficeManagerAdapter officeManagerAdapter = new OfficeManagerAdapter(officePool,properties);
        return officeManagerAdapter;
    }

    // Must appear after the OfficeManager bean creation. Do not reorder this class by name.
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OfficeManager.class)
    public DocumentConverter jodConverter(final OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
    }

    @Bean
    public CommandLineRunner preparePoolRunner(OfficePool pool){
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                if(properties.isPreparePool()){
                    StopWatch stopWatch = new StopWatch("prepare pool");
                    stopWatch.start();
                    LOGGER.info("start to prepare pool size:{}",properties.getPoolMinIdle());
                    pool.preparePool();
                    stopWatch.stop();
                    LOGGER.info("finish prepare pool size:{},cost:{}",properties.getPoolMinIdle(),stopWatch.prettyPrint());
                }
            }
        };
    }
}
