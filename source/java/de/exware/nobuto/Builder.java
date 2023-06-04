package de.exware.nobuto;

import static de.exware.nobuto.utils.Utilities.runCommand;
import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.exware.nobuto.utils.FileStackProcessor;
import de.exware.nobuto.utils.Utilities;

/**
 * Basic Builder Class. 
 * A Builder is responsible for compiling sources and may also create binary artifacts like jar files.
 * @author martin
 */
abstract public class Builder
{
    private List<Dependency> dependencies = new ArrayList<>();
    private String version;
    
    public Builder()
    {
        File versionFile = new File("version.txt");
        if(versionFile.exists())
        {
            List<String> lines;
            try
            {
                lines = Files.readAllLines(versionFile.toPath());
                if(lines.size() == 0)
                {
                    throw new IllegalStateException("version.txt is empty.");
                }
                version = lines.get(0).trim();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            version = "0.0.1";
        }
        if(version.contains(".qualifier"))
        {
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
            String qualifier = "." + df.format(new Date());
            version = version.replace(".qualifier", qualifier);
        }
    }
    
    /**
     * Default target. Will call checkTools() and compile()
     * @throws Exception
     */
    public void dist() throws Exception
    {
        checkTools();
        compile();
        verbosePrint(1, "DIST");
    }

    /**
     * A method where you will place Tools to be checked for existance.
     * @throws InterruptedException 
     * @throws IOException 
     */
    protected abstract void checkTools() throws IOException, InterruptedException;

    /**
     * Default compile method does nothing, except print the line COMPILE.
     * @throws Exception
     */
    public void compile() throws Exception
    {
        verbosePrint(1, "COMPILE");
    }
    
    /**
     * Method to check for a tool.
     * If the command returns zero, then the check is OK.
     * Example checkTool("javac", "-version");
     * @param commandAndParams
     * @throws IOException
     * @throws InterruptedException
     */
    public void checkTool(String ... commandAndParams) throws IOException, InterruptedException
    {
        Utilities.checkTool(commandAndParams);
    }
    
    /**
     * Build a path out of an list of Strings. This is System dependend. On Windows it will use another 
     * pathSeparator then on UNIX.
     * @param paths
     * @return
     */
    public String buildPath(List<String> paths)
    {
        StringBuilder sb = new StringBuilder(); 
        for(int i=0;i<paths.size();i++)
        {
            if(i > 0)
            {
                sb.append(File.pathSeparator);
            }
            sb.append(paths.get(i));
        }
        return sb.toString();
    }

    /**
     * Returns the Version of the Tool from the version.txt File in the main folder.
     * Defaults to "0.0.1"
     * @return
     */
    public String getVersion()
    {
        return version;
    }
    
    /**
     * Call nobuto.sh on a subdirectory.
     * @param directory
     * @param buildclass
     * @param target
     * @throws IOException
     * @throws InterruptedException
     */
    public void buildSubproject(String directory) throws IOException, InterruptedException
    {
        buildSubproject(directory, "build/Build", Main.target);
    }
    
    /**
     * Call nobuto.sh on a subdirectory.
     * @param directory
     * @param buildclass
     * @param target
     * @throws IOException
     * @throws InterruptedException
     */
    public void buildSubproject(String directory, String buildclass, String target) throws IOException, InterruptedException
    {
        verbosePrint(0, "Running sub project: " + directory);
        int ret = runCommand(directory, true, "sh", "nobuto.sh", "-t", target, "-c", buildclass);
        if(ret != 0)
        {
            throw new RuntimeException("Subcommand failed");
        }
    }
    
    /**
     * Displays a message to the user, and reads input from user.
     * If in Headless mode, then it will be read from the commandline. Otherwise an InputDialog will be shown.
     * @param echoText
     * @return
     * @throws IOException
     */
    public String readInput(String echoText) throws IOException
    {
        return Utilities.readInput(echoText);
    }
    
    /**
     * Replaces a pattern in a File. The File is not allowed to be larger then 1.000.000 bytes.
     * @param file the file, where the replacement is made.
     * @param charset the characterSet used to read and write the file.
     * @param pattern a regular expression to be replaced in the whole file.
     * @param replacement the replacement Text.
     * @throws IOException
     */
    public void replaceInFile(String file, String charset, String pattern, String replacement) throws IOException
    {
        Utilities.replaceInFile(file, charset, pattern, replacement);
    }
    
    public boolean isUpToDate(File target, File ... files)
    {
        final boolean[] upToDate = new boolean[1];
        FileStackProcessor fsp = new FileStackProcessor()
        {
            @Override
            protected void processElement(File file)
            {
                if(file.lastModified() < target.lastModified())
                {
                    upToDate[0] = false;
                }
            }
        };
        fsp.process(files);
        return upToDate[0];
    }

    public boolean isUpToDate(String target, String ... files)
    {
        File t = new File(target);
        File[] f = new File[files.length];
        for(int i=0;i<files.length;i++)
        {
            f[i] = new File(files[i]);
        }
        return isUpToDate(t, f);
    }

    protected void setVersion(String version)
    {
        this.version = version;
    }
    
    protected void writeVersion() throws IOException
    {
        FileWriter writer = new FileWriter(new File("version.txt"));
        writer.write(version);
        writer.close();
    }
    
    public void addDependency(String project, String version)
    {
        Dependency dep = new Dependency(project, version);
        dependencies.add(dep);
    }

    public void addDependency(Dependency dep)
    {
        dependencies.add(dep);
    }

    public List<Dependency> getDependencies()
    {
        return dependencies;
    }
    
    public void clean() throws IOException
    {
    }
}
