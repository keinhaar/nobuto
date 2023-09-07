package de.exware.nobuto.java;

import static de.exware.nobuto.utils.Utilities.verbosePrint;

import de.exware.nobuto.Dependency;

public class JavaDependency extends Dependency
{
    private String path;
    
    public JavaDependency(String projectname, String version, String path)
    {
        super(projectname, version);
        this.path = path;
    }
    
    public JavaDependency(String projectname, String version)
    {
        super(projectname, version);
    }

    public void addToClasspath(JavaBuilder builder) throws Exception
    {
        builder.addClasspathItem(path);
        verbosePrint(1, "Added Classpath Item " + path + " in version " + getVersion());
    }
}
