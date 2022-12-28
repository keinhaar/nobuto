package de.exware.nobuto.java;

import de.exware.nobuto.Dependency;

public class JavaDependency extends Dependency
{
    public JavaDependency(String projectname, String version)
    {
        super(projectname, version);
    }
    
    public void addToClasspath(JavaBuilder builder) throws Exception
    {
    }
}
