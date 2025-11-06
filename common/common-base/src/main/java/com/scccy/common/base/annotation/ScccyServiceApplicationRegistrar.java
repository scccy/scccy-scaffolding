package com.scccy.common.base.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 微服务应用自动配置注册器
 * <p>
 * 通过反射自动获取启动类所在的服务模块，动态配置 Repository 和 Entity 的扫描路径
 * <p>
 * 此注册器会自动为启动类所在的模块配置：
 * <ul>
 *     <li>JPA Repository 扫描路径：com.scccy.service.{serviceName}.dao.repository</li>
 *     <li>Entity 扫描路径：com.scccy.service.{serviceName}.domain.jpa</li>
 * </ul>
 * <p>
 * 这样新建服务时，只需要在启动类上使用 @ScccyServiceApplication 注解，
 * 就可以自动配置 Repository 和 Entity 的扫描路径，无需手动修改配置文件。
 *
 * @author scccy
 */
public class ScccyServiceApplicationRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 默认构造函数
     */
    public ScccyServiceApplicationRegistrar() {
    }

    private static final String BASE_PACKAGE = "com.scccy.service";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取被注解的类（启动类）的完整类名
        String className = importingClassMetadata.getClassName();
        String packageName = getPackageName(className);

        // 从包路径中提取服务名
        // 例如：com.scccy.service.demo -> demo
        String serviceName = extractServiceName(packageName);

        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalStateException(
                    String.format("无法从启动类包路径中提取服务名: %s，期望格式: %s.{serviceName}", className, BASE_PACKAGE)
            );
        }

        // 构建 Repository 和 Entity 的包路径
        String repositoryPackage = String.format("%s.%s.dao.repository", BASE_PACKAGE, serviceName);
        String entityPackage = String.format("%s.%s.domain.jpa", BASE_PACKAGE, serviceName);

        // 动态注册 Entity 扫描
        registerEntityScan(registry, entityPackage);

        // 动态注册 JPA Repository 扫描
        registerJpaRepositories(registry, repositoryPackage);
        
        // Feign 客户端扫描已通过 @EnableFeignClients 注解自动配置，无需手动注册
    }

    /**
     * 动态注册 Entity 扫描
     */
    private void registerEntityScan(BeanDefinitionRegistry registry, String basePackage) {
        try {
            EntityScanPackages.register(registry, basePackage);
        } catch (Exception e) {
            System.err.println("警告: 无法动态注册 Entity 扫描 (" + basePackage + ")，将使用默认配置: " + e.getMessage());
        }
    }

    /**
     * 动态注册 JPA Repository 扫描
     * 只注册当前启动服务的 Repository 包路径
     */
    private void registerJpaRepositories(BeanDefinitionRegistry registry, String basePackage) {
        try {
            // 使用 ClassPathScanningCandidateComponentProvider 扫描当前服务的 Repository
            org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider scanner =
                    new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);
            
            // 扫描所有继承自 Repository 的接口
            scanner.addIncludeFilter(new org.springframework.core.type.filter.AssignableTypeFilter(
                    org.springframework.data.repository.Repository.class));
            
            // 扫描当前服务的 Repository 包路径下的所有 Repository 接口
            for (org.springframework.beans.factory.config.BeanDefinition candidate :
                    scanner.findCandidateComponents(basePackage)) {
                
                String beanClassName = candidate.getBeanClassName();
                if (beanClassName != null && !registry.containsBeanDefinition(beanClassName)) {
                    // 为每个 Repository 接口创建 Bean 定义
                    org.springframework.beans.factory.support.BeanDefinitionBuilder builder =
                            org.springframework.beans.factory.support.BeanDefinitionBuilder
                                    .genericBeanDefinition(
                                            org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean.class);
                    
                    // 设置 Repository 接口类型
                    try {
                        Class<?> repositoryInterface = Class.forName(beanClassName);
                        builder.addConstructorArgValue(repositoryInterface);
                    } catch (ClassNotFoundException e) {
                        // 如果类加载失败，使用类名字符串
                        builder.addConstructorArgValue(beanClassName);
                    }
                    
                    builder.setLazyInit(true);
                    
                    // 注册 Bean 定义
                    String beanName = beanClassName.substring(beanClassName.lastIndexOf('.') + 1);
                    beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
                    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
                }
            }
        } catch (Exception e) {
            System.err.println("警告: 无法动态注册 JPA Repository 扫描 (" + basePackage + 
                    ")，将使用默认配置。错误: " + e.getMessage());
            // 不抛出异常，让 Spring Boot 的默认自动配置来处理
        }
    }

    /**
     * 从完整类名中提取包名
     */
    private String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return className.substring(0, lastDotIndex);
    }

    /**
     * 从包路径中提取服务名
     * 例如：com.scccy.service.demo -> demo
     * com.scccy.service.system -> system
     * com.scccy.service.demo.controller -> demo (如果有多级包结构，取第一级)
     */
    private String extractServiceName(String packageName) {
        if (packageName == null || !packageName.startsWith(BASE_PACKAGE + ".")) {
            return null;
        }

        String remaining = packageName.substring(BASE_PACKAGE.length() + 1);
        if (remaining.isEmpty()) {
            return null;
        }

        // 提取第一个包名作为服务名
        int dotIndex = remaining.indexOf('.');
        if (dotIndex == -1) {
            return remaining;
        }
        return remaining.substring(0, dotIndex);
    }
}

