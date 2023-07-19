package org.fuin.openrewrite.issue;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

public class ReplaceJodaAppendSecondofDayRecipe extends Recipe {

    private static final String ORG_JODA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER = "org.joda.time.format.DateTimeFormatterBuilder";
    private static final MethodMatcher APPEND_SECOND_OF_DAY_INT =
            new MethodMatcher(ORG_JODA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER + " appendSecondOfDay(int)");
    private static final String JAVA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER = "java.time.format.DateTimeFormatterBuilder";

    @Override
    public String getDisplayName() {
        return "Replace Joda 'DateTimeFormatterBuilder.appendSecondOfDay(int)'";
    }

    @Override
    public String getDescription() {
        return "Replaces the method call '" + ORG_JODA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER + "#appendSecondOfDay(int)' with "
               + "'" + JAVA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER + "#appendValue(ChronoField, int)'.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("JODA");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(5);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new NoMissingTypes(), new JavaIsoVisitor<>() {
            private final JavaTemplate newAppendValue =
                    JavaTemplate.builder("#{any()}.appendValue(ChronoField.SECOND_OF_DAY, #{any(int)})")
                            .imports("java.time.temporal.ChronoField").build();


            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method,
                                                            ExecutionContext executionContext) {
                if (APPEND_SECOND_OF_DAY_INT.matches(method)) {
                    maybeRemoveImport(ORG_JODA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER);
                    maybeAddImport(JAVA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER, false);
                    maybeAddImport("java.time.temporal.ChronoField");

                    doAfterVisit(new ChangeType(
                            ORG_JODA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER,
                            JAVA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER,
                            true).getVisitor());

                    J.MethodInvocation apply = newAppendValue.apply(getCursor(),
                            method.getCoordinates().replace(),
                            method.getSelect(),
                            method.getArguments().get(0));
                    return apply
                            .withMethodType(method.getMethodType().withDeclaringType(JavaType.ShallowClass.build(JAVA_TIME_FORMAT_DATE_TIME_FORMATTER_BUILDER)))
                            .withName(method.getName().withSimpleName("appendValue"));
                }
                return super.visitMethodInvocation(method, executionContext);
            }

        });
    }

}
