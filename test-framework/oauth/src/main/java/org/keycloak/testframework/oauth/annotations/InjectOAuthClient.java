package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectOAuthClient {

    String realmRef() default "";

    String clientRef() default "";
    String clientId() default "";
    String clientSecret() default "";

}
