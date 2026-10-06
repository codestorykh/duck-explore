package com.duck.explore.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class LocalizationConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.of("km"),       // Khmer
                Locale.KOREAN,         // Korean ("ko")
                Locale.JAPANESE,       // Japanese ("ja")
                Locale.SIMPLIFIED_CHINESE // Chinese Simplified ("zh_CN")
        ));
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Looks for ValidationMessages.properties on the classpath
        messageSource.setBasename("classpath:ValidationMessages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);

        // Explicitly disable fail-fast so ALL constraints across ALL fields execute
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.validator.fail_fast", "false");
        bean.setValidationPropertyMap(properties);

        return bean;
    }
}