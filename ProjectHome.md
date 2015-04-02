当你的项目里有一堆 javascript 文件，在开发的时候想保持以最简单的方式来管理这些脚本，运行调试的时候它们保持原样，上线的时候还希望它们能压缩好，jscombiner 就是那样一个工具。

## How to use ? (怎么使用) ##
首先需要引入依赖，最简单的是使用 maven 的方式。
```
<dependency>
  <groupId>net.leegorous</groupId>
  <artifactId>jscombiner</artifactId>
  <version>0.4-SNAPSHOT</version>
</dependency>
```
遗憾的是现在 jar 并没有存在于公开 maven repo 当中，所以需要手动 build 一下，方法见下面。

假设存在如下的资源结构：
```
web_app
|    index.jsp
|
\---scripts
    |   config.js
    |   app.js
    |
    \---pkg
            a.js
            b.js
```
单个 js 文件视作一个类 (class)，scripts 目录被称为是 cp  (classpath : 类路径)。其中 app.js 会依赖 pkg.`*`

有两种使用方式。

一种是使用 jsc taglib，如果在使用的是 jsp，就像下面这样
```
<%@taglib prefix="jsc" uri="/jsc-tags" %>
<html>
  <body>
  <jsc:scripts mode="dev" cp="scripts" output="/generated/brick">
    app
  </jsc:scripts>
  ...
```
它就会输出
```
<html>
  <body>
  <script src="scripts/app.js" language="JavaScript" type="text/javascript"></script>
  <script src="scripts/pkg/a.js" language="JavaScript" type="text/javascript"></script>
  <script src="scripts/pkg/b.js" language="JavaScript" type="text/javascript"></script>
  ...
```

第二种方式是在 java 代码中。
```
JSC jsc = new JSC();
// 如果没有 servlet 容器，可以自定义 PathResolver
jsc.setPathResolver(new ServletPathResolver( servletContext )); 
jsc.process("scripts", "app", "tags", null);
```
process 返回结果:
```
<script src="scripts/app.js" language="JavaScript" type="text/javascript"></script>
<script src="scripts/pkg/a.js" language="JavaScript" type="text/javascript"></script>
<script src="scripts/pkg/b.js" language="JavaScript" type="text/javascript"></script>
```

## How to build ? (怎么打包) ##
  1. checkout source code
```
svn checkout http://jscombiner.googlecode.com/svn/trunk/ jscombiner
```
  1. build with maven
```
mvn install
```

## [How it works ? （工作原理)](HowItWorks.md) ##



---


This project is evolving and more interesting.

The **Jscombiner (JavaScript Combiner)** is a helper tool which will help you to manage your javascripts.

Imagine this case. You got a javascript project directory like this:
```
web_app
|    index.jsp
|
\---scripts
    |   config.js
    |   app.js
    |
    \---pkg
            a.js
            b.js
```

Here the _config.js_ is a optional file which provide configuration information.
_app.js_ is kind of main application script.

Inside _app.js_:
```
/**
 * @import pkg.*
 */
// any code here
```

_pkg/a.js_ and _pkg/b.js_ are some kind of functions or classes scripts.

Then, what do you expect in the index.jsp?
```
<html>
<head>
<script src="scripts/app.js" language="JavaScript" type="text/javascript"></script>
<script src="scripts/pkg/a.js" language="JavaScript" type="text/javascript"></script>
<script src="scripts/pkg/b.js" language="JavaScript" type="text/javascript"></script>
...
```

And what about adding a _pkg/c.js? You can update index.jsp by hand._

But what about you got dozens of scripts, dozens of applications and you have to update one or two of them in the complicate hierarchy?

To avoid this issue, many developer keep a small volume but long scripts. But this is not good to manage your code and collaborate.

**Jscombiner** will help you get through of this. See the **@import** in _app.js_ comment. All you should do is adding dependencies description. Then, let **Jscombiner** do the rest using a tag-lib:
```
<%@taglib prefix="jsc" uri="/jsc-tags" %>
<html><head>
<jsc:scripts path="app.js" />
...
```

Also, **Jscombiner** has a command line tool to combiner your scripts into a big script.

**Jscombiner** is so beta, examples and docs are on the road. But if you are eager, just let me know via leegorous[at](at.md)gmail.com or raise an issue.

Saving your time is saving the world.

Enjoy!


---

Below is the old description, it works all the time.

Managing javascripts is not a nice job and it will get worse as your scripts grow quickly. JSC (JavaScriptCombiner) comes to set you free on it.

You can organise your scripts as you want, write down the configuration and let JSC do the rest.

JSC can assemble your scripts into one js file, generate an agent page and testCase pages ([JsUnit](http://sourceforge.net/projects/jsunit/) test page) according as the configuration file.

JSC is developed in Java.

在开发JavaScript项目中，管理js是一个比较令人头疼的问题，特别是当项目快速发展，脚本越来越长，越来越多时，增删修改在所难免时，管理问题凸显。

为此制作这个小工具来让我们可以以所希望的粒度来分布和组织脚本文件，但又不会使得管理他们成为一种负担。

设想我们有很多脚本，这些脚本有些可以独立成模块的，但有些则不好独立，但是在管理上，做好功能区分是更加有利的，于是开始拆分这些脚本为单独的脚本。又或者我们希望脚本是以类（跟Java的类概念相近，但是是prototype型的）为单元的，那么将一个类独立成一个文件管理起来会更加方便。拆分成大量的独立脚本之后，又如何组织起来呢？按照最基本的办法，就是写大量的脚本引用标记，又或者像某些脚本导入框架所使用的方法一样进行动态导入。然而问题是当脚本数量比较多时，手动进行比较费事失事，客户端需要装载大量小脚本，而且脚本是顺序执行的，也就是说有依赖关系的，搞清关系这个本身也比较麻烦。这个就是JavaScriptCombiner（简称jscombiner，或者JSC）的最初的开发目的。

JSC最基本的功能就是安装配置文件，读取脚本，组织好其依赖关系之后，合并成一个大脚本（未压缩）。而JSC怎么知道应该如何去处理那些关系呢？答案是脚本需要提供其依赖信息，例如加入形如下面的注释：
```
/**
 * @import net.leegorous.jsc.*;
 */
```

但是可以预见的是，每次修改脚本都需要重新组装一次脚本，这个有点不好，于是为其增加一种模式，agent模式，专门用于调试的。其原理就是生成一个agent页面，读取并组织好关系后不是将脚本写入agent页面，而是将每个脚本相对于agent页面的引用标记写入。外部页面以一个iframe包含（其实也可以不包含而直接使用）agent页，这样在脚本发生修改之后就不需要重新合成，因为每一次装载都是直接调用小脚本本身，这在（本地）调试时几乎看不出有影响。

再就是测试脚本的需求，JSC提供了对Edward Hieatt开发的[JsUnit](http://sourceforge.net/projects/jsunit/)的支持。使用者只需要像JUnit一样为testCase建立一个脚本，脚本里面包含所需要的依赖信息，在配置文件里面需要指明jsUnitCore.js所在的目录并且指明testCases所在的位置，JSC就能够为每个testCase脚本生成可以在[JsUnit](http://sourceforge.net/projects/jsunit/)中运行的测试页。

JSC的功能目前还比较简单，但是在实际使用过程当中，觉得它真的可以节省不少劳动时间，于是拿出来大家分享一下。:P