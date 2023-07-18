# openrewrite-issue

## Issue
The following should remove the import, but it does not:

```java
maybeRemoveImport("org.joda.time.format.DateTimeFormatterBuilder");
```

