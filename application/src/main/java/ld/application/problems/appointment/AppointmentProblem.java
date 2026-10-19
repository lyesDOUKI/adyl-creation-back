package ld.application.problems.appointment;

import ld.spring.web.lib.ProblemCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ProblemCode
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppointmentProblem {
    AppointmentErrorCode value();
}
