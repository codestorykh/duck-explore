package com.duck.explore.idempotency.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    String key() default "";

    long ttl() default 60;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}