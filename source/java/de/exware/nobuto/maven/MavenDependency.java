package de.exware.nobuto.maven;

import java.io.File;
import java.io.IOException;

import de.exware.nobuto.java.JavaBuilder;
import de.exware.nobuto.java.JavaDependency;

public class MavenDependency extends JavaDependency
{
    private String groupId;
    
    public MavenDependency(String groupId, String projectname, String version)
    {
        super(projectname, version);
        this.groupId = groupId;
    }
    
    @Override
    public void addToClasspath(JavaBuilder builder) throws Exception
    {
        super.addToClasspath(builder);
        Maven maven = Maven.getDefaultinstance();
        File file = new File("tmp/maven");
        file.mkdirs();
        File lib = maven.get(groupId, getProjectname(), getVersion(), "tmp/maven");
        if(lib == null)
        {
            throw new IOException("Library " + getProjectname() + " not found");
        }
        builder.addClasspathItem(lib.getPath());
    }
}
