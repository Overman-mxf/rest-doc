package restdoc.client.restweb;

import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import restdoc.client.api.AgentImpl;
import restdoc.client.api.ServerProperties;
import restdoc.client.restweb.context.EndpointsListener;
import restdoc.client.restweb.handler.ExportApiHandler;
import restdoc.client.restweb.handler.InvokerApiHandler;
import restdoc.client.restweb.handler.ReportClientInfoHandler;

/**
 * @author Maple
 * @see org.springframework.context.ApplicationContext
 * @see BeanDefinitionRegistryPostProcessor
 */
@EnableConfigurationProperties(value = {AgentConfigurationProperties.class})
@Configuration
public class EnvConfiguration {

    @Bean(name = "restWebAgentImpl")
    public AgentImpl agent(AgentConfigurationProperties configurationProperties) {
        return new AgentImpl(new ServerProperties() {
            @Override
            public String host() {
                return configurationProperties.getHost();
            }

            @Override
            public int port() {
                return configurationProperties.getPort();
            }

            @Override
            public String service() {
                return configurationProperties.getService();
            }
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public ExportApiHandler exportApiHandler(AgentConfigurationProperties configurationProperties,
                                             EndpointsListener endpointsListener,
                                             Environment environment) {
        return new ExportApiHandler(configurationProperties, endpointsListener, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public EndpointsListener endpointsListener(Environment environment) {
        return new EndpointsListener(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public InvokerApiHandler invokerApiHandler(RestWebInvokerImpl invoker) {
        return new InvokerApiHandler(invoker);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestWebInvokerImpl restWebInvoker(Environment environment, RestTemplate restTemplate) {
        return new RestWebInvokerImpl(environment, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    @ConditionalOnMissingBean
    public ReportClientInfoHandler reportClientInfoHandler(AgentConfigurationProperties configurationProperties,
                                                           Environment environment) {
        return new ReportClientInfoHandler(configurationProperties, environment);
    }
}
