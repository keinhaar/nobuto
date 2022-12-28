package de.exware.nobuto.maven;

import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.exware.nobuto.utils.Utilities;

/**
 * A simple class to download maven artifacts.
 * Defaults to https://repo.maven.apache.org/maven2/
 * @author martin
 *
 */
public class Maven
{
    private List<String> repositories = new ArrayList<>();
    private static final Maven defaultInstance = new Maven();
    
    public Maven()
    {
        repositories.add("https://repo.maven.apache.org/maven2/");
    }

    public void addRepository(String repo)
    {
        repositories.add(repo);
    }
    
    /**
     * Gets an Artifact from one of the defined Repositories and stores it in a local folder.
     * @param groupId
     * @param artifactId
     * @param version
     * @param localFolder
     * @throws IOException
     */
    public File get(String groupId, String artifactId, String version, String localFolder) throws IOException
    {
        File file = new File(localFolder +"/" + artifactId + "-" + version + ".jar");
        if(file.exists())
        {
            return file;
        }
        else
        {
            for(int i=0;i<repositories.size();i++)
            {
                String repo = repositories.get(i);
                groupId = groupId.replace('.', '/');
                URL url = new URL(repo + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar");
                try
                {
                    BufferedInputStream in = new BufferedInputStream(url.openStream());
                    verbosePrint(1, "Downloading from maven repo: " + url);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    Utilities.copy(in, out);
                    in.close();
                    out.close();
                    return file;
                }
                catch(IOException ex)
                {
                    if(i+1 == repositories.size())
                    {
                        throw ex;
                    }
                }
            }
        }
        throw new IOException("No such file, or connection not possible: " + file);
    }

    public static Maven getDefaultinstance()
    {
        return defaultInstance;
    }
}
