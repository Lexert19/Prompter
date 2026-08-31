package com.example.promptengineering.annotation;

import com.example.promptengineering.model.ActionType;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
  ActionType action();
  String target() default "";
  String details() default "";
}