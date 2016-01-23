# Usage

The issue about taglib classes is that they are instanciated and controlled by the web container. Since the web container does not know about your Spring configuration, the Spring dependency injection mechanisms do not work in a taglib class.

`commons-taglib` creates a proxy class for each taglib class. That proxy class fetches a reference to Spring, and uses Spring to create a new instance of your taglib bean when required. Tag parameters are automatically passed into the taglib bean as properties.

This all works more or less transparently. You only need to define your taglib beans using annotations, make sure that `commons-taglib` is in your classpath while `javac` is invoked, and everything else is taken care of automatically.

## Package Info

The entire tag library definition is controlled by annotations. `commons-taglib` will generate the required tag proxies and the TLD file from these annotations automatically, during the compilation process.

A tag library is defined by an annotated `package-info.java` file in the package that will contain the tag classes. An example `package-info.java` file would look like this (I left out the `import` statements for brevity):

```java
@TagLib(tlibversion = "1.0", shortname = "mytaglib", uri = "http://example.com/taglib/mytaglib")
@TagInfo("My tag library")
package com.example.taglib
```

The `@TagLib` annotation is required. It states that the package contains a tag library, and it also gives parameters for the TLD file to be generated. The `@TagInfo` annotation is optional and adds further information to make the TLD file more readable.

And that's it... We defined that the `com.example.taglib` package is a tag library and contains tag classes. Now let's see how we add tags to the tag library.

## Tag Classes

Each tag is represented by a single class. Again, we will use annotations to define how the tag is constructed:

```java
package com.example.taglib

@Tag(type = IterationTag.class)
@TagInfo("A tag that prints a famous text.")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HelloWorldTag extends TagSupport {

  @Override
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().append("Hello World");
    } catch (IOException ex) {
      throw new JspException(ex);
    }
  }

}
```

The `@Tag` annotation is required. It states that this is a tag class. Since no explicit tag name is given, the generated tag will be called "helloWorld". Of course you can choose another tag name, by setting the name with the `@Tag` annotation.

The class is `IterationTag` typed because `TagSupport` is an implementation of `IterationTag`. Sadly this information cannot be acquired at compile time, so we need to add the type manually to the `@Tag` annotation. It will be used by `commons-taglib` to select the proxy implementation that meets the API of your class.

`@TagInfo` is again an optional annotation, which helps reading the generated TLD file.

`@Component` and `@Scope` are Spring annotations. As you see, the tag class is a Spring bean! Note that tag classes _must_ be prototype scoped. This is because the web container may create and use several instances of the tag class concurrently. If you forget to define the prototype scope, you will get an error from `commons-taglib` at runtime.

Now, when you compile the `HelloWorldTag.java` and `package-info.java` files, you will get the compiled `HelloWorldTag.class`, a generated proxy class and a `taglib.tld` file with all tag definitions. Congratulations! You have just built a Spring enabled tag library!

As mentioned above, the `HelloWorldTag` is a full-featured Spring bean. You can use all the Spring features like dependency injection here.

## Parameters

Tags would be rather useless without the possibility to pass parameters. And - you probably guessed it already - we use annotations again to define the tag parameters:

```java
package com.example.taglib

@Tag(type = IterationTag.class)
@TagInfo("A tag that prints a famous text.")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HelloWorldTag extends TagSupport {

  private String message;

  @TagParameter(required = true)
  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().append(message);
    } catch (IOException ex) {
      throw new JspException(ex);
    }
  }

}
```

What we have defined here is a tag parameter called "message", which is required and is of type `String`. When you compile this class, you will see that the `taglib.tld` now contains the respective parameter definition. On runtime, the tag proxy takes care to forward the parameter to your setter method.

Note that `@TagParameter` only supports annotation on setter methods. The method must accept exactly one parameter and must not throw any checked exception. The parameter name is always extracted from the name of the method.

## TryCatchFinally

If your tag library implements the `TryCatchFinally` interface, you need to set an appropriate flag at the `@Tag` annotation:

```java
package com.example.taglib

@Tag(type = IterationTag.class, tryCatchFinally = true)
@TagInfo("A tag that prints a famous text.")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HelloWorldTag extends TagSupport implements TryCatchFinally {

  public void doCatch(Throwable t) throws Throwable {
    // your code here...
  }

  public void doFinally() {
    // your code here...
  }

}
```

If you forget the `tryCatchFinally` flag, the `doCatch()` and `doFinally()` methods will not be invoked!

## Different Bean Names

`commons-taglib` assumes that your tag class bean is named as if it is annotated with a plain `@Component` annotation. If you decide to give your tag class bean a different name, the proxy will be unable to find your bean unless you state the name:

```java
package com.example.taglib

@Tag(type = IterationTag.class, bean = "holaMundoTag")
@TagInfo("A tag that prints a famous text.")
@Component("holaMundoTag")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HelloWorldTag extends TagSupport {
  // your code here...
}
```

As you see, the bean is named `holaMundoTag` at the `@Component` annotation, so you must also provide an appropriate `bean` attribute at the `@Tag` annotation, to avoid a runtime error. This also applies if you use `bean.xml` files instead of annotations, and state a special name there.

## Multiple Taglibs

By default, `commons-taglib` will generate a TLD file called `META-INF/taglib.tld`. This is fine as long as there is only one tag library in your jar file.

For multiple tag libraries, you can provide the `@TagLib` annotation with a custom file name for your tld:

```java
@TagLib(tlibversion = "1.0", shortname = "anothertaglib", uri = "http://example.com/taglib/anothertaglib",
  tld = "META-INF/anothertaglib.tld")
@TagInfo("Another tag library")
package com.example.anothertaglib
```

## Finding ancestor tags

Sometimes it is necessary to find an ancestor tag, for example if a nested parameter tag tries to set its parameter on the enclosing tag. Usually you would use `TagSupport.findAncestorWithClass()` for this purpose. Anyhow this method is not aware of proxied tags, since it only sees the proxy instance and not the target behind it. `commons-taglib` offers an own method for this purpose, called `TaglibUtils.findAncestorWithType()`, which can be quite used as a drop-in replacement.

## Custom Servlets

The taglib proxies need a reference to a Spring `BeanFactory` in order to fetch the taglib class bean. The only way is to pass it is via a page or servlet attribute.

If your servlet is derived from `org.springframework.web.servlet.FrameworkServlet`, you don't need to do anything because an attribute is already set there, which is automagically found by the taglib proxy.

Anyhow if you use a self-made servlet, you need to set the `BeanFactory` instance in an attribute (I suggest an application scoped attribute), and pass the attribute name to the taglib proxy using the `@BeanFactoryReference` annotation. You can set this annotation either at the class definition of the tag class, or at the package info for the entire tag library.

```java
@TagLib(tlibversion = "1.0", shortname = "mytaglib", uri = "http://example.com/taglib/mytaglib")
@TagInfo("My tag library")
@BeanFactoryReference("myBeanFactory")
package com.example.taglib
```

In the example above, the taglib proxies will search for the "`myBeanFactory`" attribute in the page, request, session (if valid) and application scope. If an attribute with that name was not found (or did not contain a valid `BeanFactory`), an exception will be thrown.
