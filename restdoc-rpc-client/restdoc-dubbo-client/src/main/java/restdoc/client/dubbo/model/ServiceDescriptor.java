package restdoc.client.dubbo.model;

import org.apache.dubbo.common.utils.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * ServiceModel and ServiceMetadata are to some extend duplicated with each other.
 * We should merge them in the future.
 */
public class ServiceDescriptor {
    private final String serviceName;
    private final Class<?> serviceInterfaceClass;
    // to accelerate search
    private final Map<String, List<MethodDescriptor>> methods = new HashMap<>();
    private final Map<String, Map<String, MethodDescriptor>> descToMethods = new HashMap<>();

    public ServiceDescriptor(Class<?> interfaceClass) {
        this.serviceInterfaceClass = interfaceClass;
        this.serviceName = interfaceClass.getName();
        initMethods();
    }

    private void initMethods() {
        Method[] methodsToExport = this.serviceInterfaceClass.getMethods();
        for (Method method : methodsToExport) {
            method.setAccessible(true);

            List<MethodDescriptor> methodModels = methods.computeIfAbsent(method.getName(), (k) -> new ArrayList<>(1));
            methodModels.add(new MethodDescriptor(method));
        }

        methods.forEach((methodName, methodList) -> {
            Map<String, MethodDescriptor> descMap = descToMethods.computeIfAbsent(methodName, k -> new HashMap<>());
            methodList.forEach(methodModel -> descMap.put(methodModel.getParamDesc(), methodModel));

//            Map<Class<?>[], MethodModel> typesMap = typeToMethods.computeIfAbsent(methodName, k -> new HashMap<>());
//            methodList.forEach(methodModel -> typesMap.put(methodModel.getParameterClasses(), methodModel));
        });
    }

    public String getServiceName() {
        return serviceName;
    }

    public Class<?> getServiceInterfaceClass() {
        return serviceInterfaceClass;
    }

    public Set<MethodDescriptor> getAllMethods() {
        Set<MethodDescriptor> methodModels = new HashSet<>();
        methods.forEach((k, v) -> methodModels.addAll(v));
        return methodModels;
    }

    /**
     * Does not use Optional as return type to avoid potential performance decrease.
     *
     * @param methodName
     * @param params
     * @return
     */
    public MethodDescriptor getMethod(String methodName, String params) {
        Map<String, MethodDescriptor> methods = descToMethods.get(methodName);
        if (CollectionUtils.isNotEmptyMap(methods)) {
            return methods.get(params);
        }
        return null;
    }

    /**
     * Does not use Optional as return type to avoid potential performance decrease.
     *
     * @param methodName
     * @param paramTypes
     * @return
     */
    public MethodDescriptor getMethod(String methodName, Class<?>[] paramTypes) {
        List<MethodDescriptor> methodModels = methods.get(methodName);
        if (CollectionUtils.isNotEmpty(methodModels)) {
            for (MethodDescriptor descriptor : methodModels) {
                if (Arrays.equals(paramTypes, descriptor.getParameterClasses())) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    public List<MethodDescriptor> getMethods(String methodName) {
        return methods.get(methodName);
    }

}