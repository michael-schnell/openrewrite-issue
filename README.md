# openrewrite-issue

## Issue
The following should remove the import, but it does not:

```java
maybeRemoveImport("org.joda.time.format.DateTimeFormatterBuilder");
```

See https://github.com/openrewrite/rewrite-migrate-java/issues/262

