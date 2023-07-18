package org.fuin.openrewrite.issue;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.NoMissingTypes;
import org.openrewrite.java.tree.J;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

public class ReplaceJodaAppendSecondofDayRecipe extends Recipe {

    private static final MethodMatcher APPEND_SECOND_OF_DAY_INT =
            new MethodMatcher("org.joda.time.format.DateTimeFormatterBuilder appendSecondOfDay(int)");

    @Override
    public String getDisplayName() {
        return "Replace Joda 'DateTimeFormatterBuilder.appendSecondOfDay(int)'";
    }

    @Override
    public String getDescription() {
        return "Replaces the method call 'org.joda.time.format.DateTimeFormatterBuilder#appendSecondOfDay(int)' with "
                + "'java.time.format.DateTimeFormatterBuilder#appendValue(ChronoField, int)'.";
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
        return Preconditions.check(new NoMissingTypes(), new JavaIsoVisitor<ExecutionContext>() {

            private final JavaTemplate newAppendValue =
                    JavaTemplate.builder("#{any()}.appendValue(ChronoField.SECOND_OF_DAY, #{any(int)})")
                            .imports("java.time.temporal.ChronoField").build();

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method,
                                                            ExecutionContext executionContext) {
                if (APPEND_SECOND_OF_DAY_INT.matches(method)) {
                    method = newAppendValue.apply(getCursor(), method.getCoordinates().replace(), method.getSelect(),
                            method.getArguments().get(0));
                    maybeRemoveImport("org.joda.time.format.DateTimeFormatterBuilder");
                    maybeAddImport("java.time.format.DateTimeFormatterBuilder", false);
                    maybeAddImport("java.time.temporal.ChronoField");
                    return method;
                }
                return super.visitMethodInvocation(method, executionContext);
            }

        });
    }

}
