package com.gym.crm.service.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistenceTx {
    boolean readOnly() default false;

    int timeout() default -1;

    String[] rollbackFor() default {};
}
