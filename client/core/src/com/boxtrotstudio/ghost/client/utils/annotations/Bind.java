package com.boxtrotstudio.ghost.client.utils.annotations;

import java.lang.annotation.*;

/**
 * Indicates that this method or parameter is bindable. When this annotation is bound to
 * a parameter, then the value is saved. When this annotation is bound to a method, then
 * the value is retrieved and returned. The method should have the same type as the
 * property bound.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Bind {
    String defaultValue() default "";

    String[] properties() default {};

    String returnProperty() default "";
}
