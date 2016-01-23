# Migration Guide

## From v1.0 to v2.0

In commons-taglib v1.0, `@TagParameter` had to be annotated to a field. It was magically filled before tag methods were invoked. However, it was a poor design choice, and also caused some strange runtime issues on Tomcats with parallel execution.

When migrating to v2.0, you need to define setter methods for all your parameter fields, and move the `@TagParameter` annotation to the respective setter method.

`@TagParameter` has no `name` attribute any more. The parameter name is always deduced from the setter method name. If you have used the `name` attribute, just name your setter method accordingly.

The new `@TagParameter` design offers some advantages. In your setter method, you can do more complex stuff than just setting a field's value. Also, the complexity of the proxy class to pass the parameter to the tag implementation has been greatly reduced that way.
