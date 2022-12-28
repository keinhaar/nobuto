package de.exware.nobuto.eclipse;


import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import de.exware.nobuto.java.JavaBuilder;
import de.exware.nobuto.maven.Maven;
import de.exware.nobuto.utils.Utilities;

abstract public class AbstractPluginBuild extends JavaBuilder
{
    protected String projectname;
    private Maven maven = new Maven();
    private String distributionDir = "dist";
    private String classesDir = "out";
    private String tmpDir = "tmp";
    private String eclipseURL = "https://download.eclipse.org/releases/2021-12/202112081000";
    private String updateSite = "update-site";

    public AbstractPluginBuild(String projectname)
    {
        this.projectname = projectname;
    }
    
    public AbstractPluginBuild(String projectname, String eclipseURL)
    {
        this.projectname = projectname;
        this.eclipseURL = eclipseURL;
    }
    
    @Override
    public void dist() throws Exception
    {
        checkTools();
        File distDir = new File(distributionDir );
        distDir.mkdirs();
        File classesDir = new File(this.classesDir + "/" + getProjectname());
        classesDir.mkdirs();
        File tmpmaven = new File(tmpDir, "maven");
        tmpmaven.mkdirs();

        setOutputFolder(classesDir.getPath());

        copyLibs();
        copyResources();
        
        compile();
        
        copyIcons();
        copyEclipseFiles(classesDir);
        
        File target = new File(tmpDir, "make-jar");
        target.mkdirs();
        Utilities.copy(classesDir, target, true);
        File manifestFile = new File(getProjectDir() + "/META-INF/MANIFEST.MF");
        String jarname = distributionDir + "/" + getProjectname() + "_" + getVersion() + ".jar";
        if(manifestFile.exists())
        {
            Utilities.copy(manifestFile, tmpDir, true);
            manifestFile = new File(tmpDir, "MANIFEST.MF");
            Utilities.replaceInFile(manifestFile.getPath(), "UTF-8", "Bundle-Version: .*",
                "Bundle-Version: " + getVersion());
            jar(jarname, target.getPath(), manifestFile.getPath());
        }
        else
        {
            jar(jarname, target.getPath(), null);
        }
        Utilities.copy(jarname, updateSite + "/" + getType() + "s", true);
    }
    
    protected void copyEclipseFiles(File classesDir) throws IOException
    {
        verbosePrint(1, "Copying plugin files.");
        File pluginProps = new File(getProjectDir() + "/plugin.properties");
        if(pluginProps.exists())
        {
            Utilities.copy(pluginProps, classesDir, true);
        }
        File pluginXML = new File(getProjectDir() + "/plugin.xml");
        if(pluginXML.exists())
        {
            Utilities.copy(pluginXML, classesDir, true);
        }
        File featureXML = new File(getProjectDir() + "/feature.xml");
        if(featureXML.exists())
        {
            Utilities.copy(featureXML, classesDir, true);
        }
        File featureProps = new File(getProjectDir() + "/feature.properties");
        if(featureProps.exists())
        {
            Utilities.copy(featureProps, classesDir, true);
        }
    }
    
    @Override
    public void compile() throws Exception
    {
        addSources(getProjectDir() + "/src");
        super.compile();
    }
    
    private void copyIcons() throws IOException
    {
        File iconsDir = new File(getProjectDir() + "/icons");
        if(iconsDir.exists())
        {
            verbosePrint(1, "Copying icons");
            File target = new File(tmpDir, "make-jar/icons");
            target.mkdirs();
            Utilities.copy(iconsDir, target, true);
        }
    }

    private void copyLibs() throws IOException
    {
        verbosePrint(1, "Copying libs");
        File libsDir = new File(getProjectDir() + "/libs");
        if(libsDir.exists())
        {
            File target = new File(tmpDir, "make-jar/libs");
            target.mkdirs();
            Utilities.copy(libsDir, target, true);
        }
        libsDir = new File(getProjectDir() + "/lib");
        if(libsDir.exists())
        {
            File target = new File(tmpDir, "make-jar/lib");
            target.mkdirs();
            Utilities.copy(libsDir, target, true);
        }
    }

    private void copyResources() throws IOException
    {
        File libsDir = new File(getProjectDir() + "/resources");
        if(libsDir.exists())
        {
            verbosePrint(1, "Copying resources");
            File target = new File(tmpDir, "make-jar/resources");
            target.mkdirs();
            Utilities.copy(libsDir, target, true);
        }
    }

    protected void addSiblingJar(String name)
    {
        verbosePrint(2, "Add sibling plugin to classpath: " + name);
        File pluginsDir = new File(distributionDir);
        File lib = findJarInPlugins(pluginsDir, name);
        addClasspathItem(lib.getAbsolutePath());
    }

    protected void addMavenJarToClasspath(String groupID, String artifactId, String version) throws IOException
    {
        File lib = maven.get(groupID, artifactId, version, tmpDir + "/maven");
        if(lib == null)
        {
            throw new IOException("Library " + artifactId + " not found");
        }
        addClasspathItem(lib.getPath());
    }

    protected void addEclipseJarToClasspath(String plugin) throws IOException
    {
        File file = new File(tmpDir, "eclipse/plugins/" + plugin);
        if(file.exists() == false)
        {
            file.getParentFile().mkdirs();
            String purl = eclipseURL + "/plugins/" + plugin;
            verbosePrint(1, "Downloading eclipse jar: " + purl);
            URL url = new URL(purl);
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            Utilities.copy(in, out);
            in.close();
            out.close();
        }
        addClasspathItem(file.getPath());
    }

    public String getProjectDir()
    {
        return getProjectname();
    }
    
    public String getType()
    {
        return "plugin";
    }

    public String getDistributionDir()
    {
        return distributionDir;
    }

    public void setDistributionDir(String distributionDir)
    {
        this.distributionDir = distributionDir;
    }

    public String getClassesDir()
    {
        return classesDir;
    }

    public void setClassesDir(String classesDir)
    {
        this.classesDir = classesDir;
    }

    public String getTmpDir()
    {
        return tmpDir;
    }

    public void setTmpDir(String tmpDir)
    {
        this.tmpDir = tmpDir;
    }

    public String getEclipseURL()
    {
        return eclipseURL;
    }

    public void setEclipseURL(String eclipseURL)
    {
        this.eclipseURL = eclipseURL;
    }

    public String getUpdateSite()
    {
        return updateSite;
    }

    public void setUpdateSite(String updateSite)
    {
        this.updateSite = updateSite;
    }

    public String getProjectname()
    {
        return projectname;
    }

    public Maven getMaven()
    {
        return maven;
    }

    public void setMaven(Maven maven)
    {
        this.maven = maven;
    }
}
