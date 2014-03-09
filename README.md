uos
========

uOS is a lighweight middleware developed by the [UnBiquitous](http://www.unbiquitous.org) research group.


uos_core
========

**uos.core** the core module of the uOS Middleware. You can say that it's the middleware itself. You can runn only the core and have access to all features that the middleware provides. But I trully advise you to look for complementary modules that makes your life easier.


Using the middleware
-----------

To use the middleware just add it to your `pom.xml` as follows.

```xml
<dependencies>
		<dependency>
			<groupId>org.unbiquitous</groupId>
			<artifactId>uos-core</artifactId>
			<version>3.0.0</version>
		</dependency>
</dependencies>
```

Don't forget to include our repository on your list.

```xml
<repositories>
		<repository>
			<id>ubiquitos</id>
			<url>http://ubiquitos.googlecode.com/svn/trunk/src/Java/maven/</url>
		</repository>
</repositories>
```

Documentation
-----------

Further information can be found [here]() but it's only related to the current ongoing branch 3.1.0. So, if you want to give it a try just go fork, clone and checkout =]
