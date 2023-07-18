package org.fuin.openrewrite.issue;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class TestRecipeReplaceJodaAppendSecondofDay implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplaceJodaAppendSecondofDayRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("joda"));
    }

    @Test
    void testRecipe1() {
        rewriteRun(
                java("""
                            import org.joda.time.format.DateTimeFormatterBuilder;
                                
                            class Test {
                                    
                                private DateTimeFormatterBuilder bla(int intArg) {
                                    DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder();
                                    return dtfb.appendSecondOfDay(intArg);
                                }
                                    
                            }
                            """,
                        """
                            import java.time.format.DateTimeFormatterBuilder;
                            import java.time.temporal.ChronoField;
                                
                            class Test {
                                    
                                private DateTimeFormatterBuilder bla(int intArg) {
                                    DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder();
                                    return dtfb.appendValue(ChronoField.SECOND_OF_DAY, intArg);
                                }
                                    
                            }
                            """
                )
        );
    }

    @Test
    void testRecipe2() {
        rewriteRun(
                java("""
                            import org.joda.time.format.DateTimeFormatterBuilder;
                                
                            class Test {
                                    
                                private DateTimeFormatterBuilder bla(int intArg) {
                                    return new DateTimeFormatterBuilder().appendSecondOfDay(intArg);
                                }
                                    
                            }
                            """,
                        """
                            import java.time.format.DateTimeFormatterBuilder;
                            import java.time.temporal.ChronoField;
                                
                            class Test {
                                    
                                private DateTimeFormatterBuilder bla(int intArg) {
                                    return new DateTimeFormatterBuilder().appendValue(ChronoField.SECOND_OF_DAY, intArg);
                                }
                                    
                            }
                            """
                )
        );
    }

}
