package de.exware.nobuto;

/**
 * A Class that contains information about an Dependency to an other Project.
 */
public class Dependency
{
    private String projectname;
    private String version;
    
    public Dependency(String projectname, String version)
    {
        super();
        this.projectname = projectname;
        this.version = version;
    }

    public String getProjectname()
    {
        return projectname;
    }

    public String getVersion()
    {
        return version;
    }
    
    protected void setVersion(String version)
    {
        this.version = version;
    }
}
