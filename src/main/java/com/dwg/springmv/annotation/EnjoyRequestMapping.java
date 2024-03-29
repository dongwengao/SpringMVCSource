package com.dwg.springmv.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyRequestMapping {

    String value() default "";
}
