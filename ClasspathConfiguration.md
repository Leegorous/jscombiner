# Introduction #

Why and how to use the classpath configuration (file)


# Details #

If your js files are all under the same directory, you won't need the classpath configuration file.

But if you get something like this




you must tell **jscombiner** about where are your scripts.

That's easy.

Create a file name config.js with content like this:
```
/**
 * @classpath relative/path/to/another/classpath
 */
```