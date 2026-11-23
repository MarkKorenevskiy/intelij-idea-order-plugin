package com.example.annotations;

import java.lang.annotation.*;

/** Stand-in for Spring's @Order: the plugin matches any annotation named "Order". */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Order {
    int value() default Integer.MAX_VALUE;
}
