package com.duck.explore.idempotency.service;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

public class IdempotencyKeyResolver {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public String resolveKey(ProceedingJoinPoint joinPoint, String spelExpression, String headerName) {
        if (StringUtils.hasText(spelExpression)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();

            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                    joinPoint.getTarget(), method, args, nameDiscoverer);

            Object value = parser.parseExpression(spelExpression).getValue(context);
            if (value != null && StringUtils.hasText(value.toString())) {
                return value.toString();
            }
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String headerValue = request.getHeader(headerName);
            if (StringUtils.hasText(headerValue)) {
                return headerValue;
            }
        }

        throw new IllegalArgumentException(
                "Unable to resolve Idempotency Key. Provide a valid SpEL expression or HTTP header '" + headerName + "'");
    }
}