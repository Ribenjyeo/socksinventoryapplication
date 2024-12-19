package ru.socks.inventory.aop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LogManager.getLogger(LoggingAspect.class);

    @Before("execution(* ru.socks.inventory.controller.*.*(..))")
    public void logRequest(JoinPoint joinPoint) {
        log.debug("Request method: {}", joinPoint.getSignature().getName());

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            String parameterName = parameters[i].getName();
            Object paramValue = args[i];
            log.debug("{}: {}", parameterName, paramValue);
        }
    }

    @AfterReturning(value = "execution(* ru.socks.inventory.controller.*.*(..))", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        log.debug("Response method: {}", joinPoint.getSignature().getName());
        log.debug("Response data: {}", result);
    }

    @AfterThrowing(value = "execution(* ru.socks.inventory.controller.*.*(..))", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        log.error("Exception occurred in method: {}", joinPoint.getSignature().getName());
        log.error("Exception message: {}", exception.getMessage());
    }
}
