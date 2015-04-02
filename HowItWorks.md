# 简介 #

介绍 jscombiner 所支持的资源管理方式，如何处理脚本依赖。

# 工作原理 #
## 资源 ##

Jscombiner 是一个从资源管理的角度出发去设计的工具，它可以帮助你更好地管理你的 JavaScript 资源文件。

里面会涉及到一些从 Java 搬过来的概念：**文件或类 (class)**，**包 (package)**，**类路径 (cp: short for class path)**。

  * 文件或类：单个 JavaScript 文件
  * 包：用于容纳 _类_ 的目录，_包_ 可以嵌套
  * 类路径：是一些目录，作为查找 _类_ 的起点

以下面的资源结构为例
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

scripts 作为 _类路径_，pkg 是称为 pkg 的包

这里总共包含 4 个类： config, app, pkg.a, pkg.b
(这里只处理 js 文件，就不一一带上后缀了)

当然，全部文件放在一个目录里面也是完全可以的，并不限制资源的管理风格。

## 依赖 ##
这里的依赖是指一个文件的解释执行之前需要先执行的另一个文件。

为了能够自描述依赖关系，在文件的头部加入包含特定形式的注释是种不错的做法。
如在 _app.js_ 里:
```
/**
 * @import pkg.*
 */
// any code here
```
通过声明的方式，来表示 app.js 需要引入 pkg.`*`，而 pkg.a 和 pkg.b 可能并无依赖关系。

Jscombiner 就是通过读取分析文件头部的特定形式来确定文件之间的依赖关系的。它能够自动理顺依赖关系，并按要求的方式进行输出。

唯一需要注意的问题是，最终脚本是以合符依赖声明的顺序进行输出，所以是不支持循环依赖的，比如 a 依赖 b，b 依赖 a，这样它就无法分辨出到底谁在前谁在后了。

使用 jscombiner，你需要做的就是声明好文件之间的依赖关系，其它都交给 jscombiner 吧。

## 调用 ##
无论是通过 jsc-taglib 还是 java 直接调用，都是十分简洁方便的。

## 输出 ##
用 outputType 可以指定输出形式，默认是输出为 script 标签列表，即 tags。

目前支持三个 outputType

  * **array** 只包含脚本路径的 json array
  * **tags** script 标签列表，可以直接嵌入到 HTML 页面里
  * **file** 将所有文件按顺序合并至指定文件，并产生一个指向该文件的 script 标签