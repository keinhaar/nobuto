# nobuto
The "No build tool build tool".
## What is Nobuto
Nobuto is a pure JAVA build System to build all kinds of JAVA Appilcations. It is fully debuggable, and everything is done in Java. No properties, no XML, no scripting, just pure JAVA.
With this early version it supports 
- Java compilation
- jar building
- Subversion branching, commit, add, listing...
- Maven Downloads
- Eclipse Repository creation

## How to use
To use nobuto in your projects you need some simple steps.
- Create a **build** folder
- copy the **nobuto.jar** into the build folder
- copy the **nobuto.sh/bat** files into the root folder of yout project
- create a **Build.java** inside of the build folder.
- run nobuto.sh

Each public method in the Build class is a valid build target. Per default the **dist** target is executed. If you want another target, you can spcify that with **-t TARGETNAME**.
For example: To clean your project you would use **nobuto.sh -t clean**.
Using the parameter **-v** will add more output to the console. Every additional **v** will add even more output. For example **-vvv** will output every compiled file.

Super simple build file for just compiling:
<pre>
public class Build
{
  public void compile()
  {
    addSource("source/java");
    super.compile();
  }
}
</pre>

## Compile
The full distribution consists of the nobuto.jar file in the build folder, and the nobuto.sh/bat scripts. So you won't need to compile it yourself, but if you want to, it is builded like any other nobuto enabled application just by calling "nobuto.sh". The resulting jar will end up in the build directory.

## Why
If you are not new to JAVA development, you may have already seen some different build systems in your carreer. 

At least the newer ones all pretent to be faster, have better dependency management, are easier to configure and all kinds of other "SUPER" features. 
But at the end, you always need to learn a new syntax, learn about bugs, avoid problems with dependencies a.s.o.

We beleave, that every of the "features" can be done as easily in JAVA, if you have the appropriate Libraries. This has 2 main advantages. 

1. Errors in your build "script" (a Java source file) syntax are directly checked by the compiler.
2. The build is fully debuggable. That's also true for nobuto itself, because we give you the full source code together with the binaries.
