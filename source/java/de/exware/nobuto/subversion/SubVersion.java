package de.exware.nobuto.subversion;

import static de.exware.nobuto.utils.Utilities.runCommand;
import static de.exware.nobuto.utils.Utilities.runCommandReadOutput;
import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.IOException;
import java.util.List;

import de.exware.nobuto.utils.OperatingSystem;
import de.exware.nobuto.utils.Utilities;

public class SubVersion
{
    private String executable = "svn";
    private static final SubVersion defaultInstance = new SubVersion();
    
    
    public SubVersion()
    {
        if(OperatingSystem.isWindows())
        {
            executable += ".exe";
        }
    }
    
    public List<String> list(String repoURL) throws IOException, InterruptedException    
    {
        return runCommandReadOutput(executable, "list", repoURL);
    }

    /**
     * Commit the current Project.
     * @param message
     * @throws IOException
     * @throws InterruptedException
     */
    public void commit(String dir, String message) throws IOException, InterruptedException    
    {
        verbosePrint(1, "Commit Subversion: " + dir);
        int ret = runCommand(dir, true, executable, "commit", "-m", message);
        handleResult(ret, "commit failed");
    }

    /**
     * Add all files to the current Project.
     * @throws IOException
     * @throws InterruptedException
     */
    public void addAll(String dir, String path) throws IOException, InterruptedException    
    {
        verbosePrint(1, "Add all files to Subversion: " + dir);
        int ret = runCommand(dir, true, executable, "add", "--force", path);
        handleResult(ret, "add failed");
    }

    /**
     * Create a Branch
     * @throws IOException
     * @throws InterruptedException
     */
    public void branch(String source, String target, String message) throws IOException, InterruptedException    
    {
        verbosePrint(1, "Branch Subversion: " + source + " --> " + target);
        int ret = runCommand(true, executable, "copy", source, target, "-m", message);
        handleResult(ret, "branch failed");
    }

    /**
     * Update the Project
     * @throws IOException
     * @throws InterruptedException
     */
    public void update(String dir) throws IOException, InterruptedException    
    {
        verbosePrint(1, "Update Subversion: " + dir);
        int ret = runCommand(dir, true, executable, "update");
        handleResult(ret, "update failed");
    }

    public boolean checkPathExists(String repoURL) throws IOException, InterruptedException    
    {
        return runCommand(false, executable, "list", repoURL) == 0;
    }

    private void handleResult(int retCode, String message) throws IOException
    {
        if(retCode != 0)
        {
            throw new IOException(message);
        }
    }
    
    public String getExecutable()
    {
        return executable;
    }

    public void setExecutable(String executable)
    {
        this.executable = executable;
    }

    public static void checkTools() throws IOException, InterruptedException
    {
        Utilities.checkTool(defaultInstance.executable, "--version");
    }

    public static SubVersion getDefaultinstance()
    {
        return defaultInstance;
    }
}
