package com.dwg.springmv.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyAutowired {

    String value() default "";
}
