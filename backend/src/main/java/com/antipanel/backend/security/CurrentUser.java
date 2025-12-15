package com.antipanel.backend.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to inject the current authenticated user.
 * Wraps @AuthenticationPrincipal for cleaner controller method signatures.
 *
 * Usage:
 * <pre>
 * @GetMapping("/me")
 * public UserResponse getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
 *     return userService.getById(currentUser.getUserId());
 * }
 * </pre>
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
