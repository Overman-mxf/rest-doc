package restdoc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.util.MimeType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.MediaTypeExpression;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import restdoc.remoting.data.ApiEmptyTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入客户端所有的API 并且生成文档
 */
public class EndpointsListener implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(EndpointsListener.class);

    private List<ApiEmptyTemplate> emptyApiTemplates;

    private final Environment environment;

    public EndpointsListener(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = event.getApplicationContext()
                .getBean(RequestMappingHandlerMapping.class)
                .getHandlerMethods();

        // Get global contextPath
        String contextPath = environment.getProperty("server.servlet.context-path", "");

        // Must be flatmap handler method
        // because of The one handler method has many request pattern

        // final report the emptyApiTemplates to remoting server
        this.emptyApiTemplates = handlerMethods.entrySet()
                .stream()
                .flatMap(hm -> {
                    RequestMappingInfo requestMappingInfo = hm.getKey();
                    HandlerMethod handlerMethod = hm.getValue();

                    return requestMappingInfo.getPatternsCondition()
                            .getPatterns()
                            .stream()
                            .filter(pattern -> !"/error".equals(pattern))
                            .map(pattern -> String.join("", contextPath, pattern))
                            .map(pattern -> {
                                ApiEmptyTemplate emptyTemplate = new ApiEmptyTemplate();
                                emptyTemplate.setSupportMethod(requestMappingInfo.getPatternsCondition().getPatterns()
                                        .toArray(new String[0]));

                                emptyTemplate.setFunction(handlerMethod.toString());
                                emptyTemplate.setPattern(pattern);
                                emptyTemplate.setController(handlerMethod.getBeanType().toString());

                                emptyTemplate.setConsumer(
                                        requestMappingInfo.getConsumesCondition()
                                                .getExpressions()
                                                .stream()
                                                .filter(MediaTypeExpression::isNegated)
                                                .map(MediaTypeExpression::getMediaType)
                                                .map(MimeType::getType)
                                                .toArray(String[]::new)
                                );

                                emptyTemplate.setProduces(requestMappingInfo.getProducesCondition()
                                        .getExpressions()
                                        .stream()
                                        .filter(MediaTypeExpression::isNegated)
                                        .map(MediaTypeExpression::getMediaType)
                                        .map(MimeType::getType)
                                        .toArray(String[]::new));

                                emptyTemplate.setUriVarFields(Arrays.stream(pattern.split("/"))
                                        .filter(snippet -> snippet.matches("^[\\{][a-zA-Z]+[0-9A-Za-z]*[\\}]$"))
                                        .map(snippet -> snippet.replaceFirst("\\{", "")
                                                .replaceAll("\\}", ""))
                                        .toArray(String[]::new));

                                return emptyTemplate;
                            });
                }).collect(Collectors.toList());

        log.info("RESTDOC-CLIENT collect api empty templates {} ", emptyApiTemplates);
    }

    public List<ApiEmptyTemplate> getEmptyApiTemplates() {
        return emptyApiTemplates;
    }
}
