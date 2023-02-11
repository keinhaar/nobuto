package de.exware.nobuto.java;

import static de.exware.nobuto.utils.Utilities.runCommand;
import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import de.exware.nobuto.Builder;
import de.exware.nobuto.Dependency;
import de.exware.nobuto.Main;
import de.exware.nobuto.utils.Utilities;

/**
 * Default Java Builder.
 * @author martin
 *
 */
public class JavaBuilder extends Builder
{
    private List<String> sourceFolders = new ArrayList<>();
    private List<String> classpath = new ArrayList<>();
    private String outputFolder = "out"; 
    private String targetVersion = "8";
    private String sourceVersion = "8";
    
    public JavaBuilder() 
    {
        addClasspathItem("build/nobuto.jar");
    }

    /**
     * Adds an Folder to the sourceFolders for compilation.
     * @param sourceFolder
     */
    public void addSources(String sourceFolder)
    {
        sourceFolders.add(sourceFolder);
    }
    
    /**
     * Add an file ot directory to the classpath for compilation
     * @param cpItem
     */
    public void addClasspathItem(String cpItem)
    {
        classpath.add(cpItem);
    }
    
    public void clearClassPath()
    {
        classpath.clear();
    }
    
    public void removeClassPathItem(String cpItem)
    {
        classpath.remove(cpItem);
    }
    
    /**
     * Checks for the existance of javac compiler.
     */
    @Override
    protected void checkTools() throws IOException, InterruptedException
    {
        checkTool("javac", "-version");
    }
    
    /**
     * Scans all sourceFolders for java files, and compiles them.
     * Additional files in the sourcefolders will be copied to the output directory.
     */
    @Override
    public void compile() throws Exception
    {
        super.compile();
        for(int i=0;i<getDependencies().size();i++)
        {
            Dependency dep = getDependencies().get(i);
            if(dep instanceof JavaDependency)
            {
                ((JavaDependency) dep).addToClasspath(this);
            }
        }
        for(int x=0;x<sourceFolders.size();x++)
        {
            String sourceFolder = sourceFolders.get(x);
            Stack<String> paths = new Stack<>();
            paths.add(sourceFolder);
            List<String> sourceFiles = new ArrayList<>();
            while(paths.isEmpty() == false)
            {
                File f = new File(paths.pop());
                File[] files = f.listFiles();
                for(int i=0;files != null && i<files.length;i++)
                {
                    File sf = files[i];
                    if(sf.isDirectory())
                    {
                        paths.push(sf.getAbsolutePath());
                    }
                    else
                    {
                        if(sf.getName().toLowerCase().endsWith(".java"))
                        {
                            String path = f.getAbsolutePath();
                            path = path.substring(new File(sourceFolder).getAbsolutePath().length()+1);
                            String sourcepath = sourceFolder + "/" + path + "/" + sf.getName();
                            String output = outputFolder + "/" + path + "/" + sf.getName();
                            output = output.replace(".java", ".class");
                            File out = new File(output);
                            if(out.lastModified() < sf.lastModified())
                            {
                                sourceFiles.add(sourcepath);
                                verbosePrint(2, "Adding to compiled classes: " + sourcepath);
                            }
                        }
                        else
                        {
                            String path = f.getAbsolutePath();
                            path = path.substring(new File(sourceFolder).getAbsolutePath().length()+1);
                            String sourcepath = sourceFolder + "/" + path + "/" + sf.getName();
                            String output = outputFolder + "/" + path + "/" + sf.getName();
                            File out = new File(output);
                            if(out.lastModified() < sf.lastModified())
                            {
                                out.getParentFile().mkdirs();
                                Utilities.copy(sf, out, true);
                                verbosePrint(1, "Copying resource file: " + sourcepath);
                            }
                        }
                    }
                }
            }
            if(sourceFiles.size() > 0)
            {
                verbosePrint(1, "Compiling " + sourceFolder);
                compile(sourceFiles);
            }
        }
    }
    
    /**
     * Compile a single file.
     * @param filename
     * @throws Exception
     */
    public void compile(String filename) throws Exception
    {
        List<String> filenames = new ArrayList<>();
        filenames.add(filename);
        compile(filenames);
    }
    
    /**
     * Default compile method. Compiles all classes in the specified source folders.
     * @param filenames
     * @throws Exception
     */
    public void compile(List<String> filenames) throws Exception
    {
        writeCompileOptions(filenames);
        int ret = runCommand("javac"
            , "@.compileoptions"
            );
        if(ret != 0)
        {
            throw new RuntimeException("Compile failed");
        }
    }
    
    /**
     * Writes the required compile parameters.
     * @param filenames
     * @throws Exception
     */
    protected void writeCompileOptions(List<String> filenames) throws Exception
    {
        String path = buildPath(sourceFolders);
        String cpath = buildPath(classpath);
        path = path + File.pathSeparator + cpath;
        File file = new File(".compileoptions");
        file.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write("-d " + outputFolder + "\r\n");
        if(Main.verboseLevel < 2)
        {
            writer.write("-Xlint:none\r\n");
            writer.write("-Xlint:-removal\r\n");
            writer.write("-Xlint:-deprecation\r\n");
        }
        writer.write("-cp " + path + "\r\n");
        writer.write("-target " + targetVersion + "\r\n");
        writer.write("-source " + sourceVersion + "\r\n");
        for(int i=0;i<filenames.size();i++)
        {
            writer.write(filenames.get(i) + "\r\n");
        }
        writer.close();
    }

    /**
     * Run any other JAVA command.
     * @throws Exception 
     */
    public void runJava(String classname, String ... parameters) throws Exception
    {
        writeJavaOptions(classname, parameters);
        int ret = runCommand("java"
            , "@.javaoptions"
            );
        if(ret != 0)
        {
            throw new RuntimeException("Running JAVA command failed");
        }
    }
    
    /**
     * Writes the required java parameters for subcommands
     * @param classname Name of the class containing a main method.
     * @throws Exception
     */
    protected void writeJavaOptions(String classname, String ... parameters ) throws Exception
    {
        String cpath = buildPath(classpath);
        String path = cpath;
        File file = new File(".javaoptions");
//        file.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write("-cp " + path + "\r\n");
        writer.write(classname + "\r\n");
        for(int i=0;i<parameters.length;i++)
        {
            writer.write(parameters[i] + "\r\n");
        }
        writer.close();
    }

    public String getOutputFolder()
    {
        return outputFolder;
    }

    /**
     * Sets the Folder, where compiled classes will end up.
     * @param outputFolder
     */
    public void setOutputFolder(String outputFolder)
    {
        this.outputFolder = outputFolder;
    }

    /**
     * Unpacking of a jar file
     * 
     * @param filename
     *            the jar file that will be unpacked
     * @param targetDir
     *            the directory where the contents are moved to
     * @throws Exception
     */
    public static void unjar(String filename, File targetDir) throws IOException
    {
        JarFile jfile = new JarFile(filename);
        Enumeration<JarEntry> enumeration = jfile.entries();
        targetDir.mkdirs();
        while (enumeration.hasMoreElements())
        {
            JarEntry jentry = enumeration.nextElement();
            if (jentry.isDirectory() == false)
            {
                File file = new File(targetDir, jentry.getName());

                // erzeugen aller parentdirectories;
                file.mkdirs();
                file.delete();
                byte[] data = Utilities.readAll(jfile.getInputStream(jentry));
                FileOutputStream out = new FileOutputStream(file);
                out.write(data);
                out.close();
            }
        }
        jfile.close();
    }

    /**
     * Creates a jar File.
     * @param filename the name of the resulting jar.
     * @param sourceDir the directory that will be in the jar. The directory itself is not included. only the contents
     * @param manifestfile The manifest file. null if you do not have an manifest.
     * @throws IOException
     */
    public static void jar(String filename, String sourceDir, String manifestfile) throws IOException
    {
        File manifest = null;
        if(manifestfile != null)
        {
            manifest = new File(manifestfile);
        }
        jar(filename, new File(sourceDir), manifest);
    }

    /**
     * Creates a jar File.
     * @param filename the name of the resulting jar.
     * @param sourceDir the directory that will be in the jar. The directory itself is not included. only the contents
     * @param manifestfile The manifest file. null if you do not have an manifest.
     * @throws IOException
     */
    public static void jar(String filename, String sourceDir, String manifestfile, FileFilter filter) throws IOException
    {
        File manifest = null;
        if(manifestfile != null)
        {
            manifest = new File(manifestfile);
        }
        jar(filename, new File(sourceDir), manifest, filter);
    }

    /**
     * Creates a jar File.
     * @param filename the name of the resulting jar.
     * @param sourceDir the directory that will be in the jar. The directory itself is not included. only the contents
     * @param manifestfile The manifest file. null if you do not have an manifest.
     * @throws IOException
     */
    public static void jar(String filename, File sourceDir, File manifestfile) throws IOException
    {
        jar(filename, sourceDir, manifestfile, null);
    }
    
    /**
     * Creates a jar File.
     * @param filename the name of the resulting jar.
     * @param sourceDir the directory that will be in the jar. The directory itself is not included. only the contents
     * @param manifestfile The manifest file. null if you do not have an manifest.
     * @throws IOException
     */
    public static void jar(String filename, File sourceDir, File manifestfile, FileFilter filter) throws IOException
    {
        JarOutputStream jout;
        if (manifestfile == null)
        {
            jout = new JarOutputStream(new FileOutputStream(filename));
        }
        else
        {
            FileInputStream in = new FileInputStream(manifestfile);
            Manifest mf = new Manifest(in);
            in.close();
            File fmf = new File(sourceDir, "/META-INF/MANIFEST.MF");
            fmf.delete();
            jout = new JarOutputStream(new FileOutputStream(filename), mf);
        }
        jar(jout, sourceDir, sourceDir.toString().length() + 1, filter);
        jout.finish();
        jout.flush();
        jout.close();
    }

    /**
     * 
     * @param jout
     * @param dir
     * @param cut gives the position, where the pathname in the jar will start.
     * @throws IOException
     */
    private static void jar(JarOutputStream jout, File dir, int cut, FileFilter filter) throws IOException
    {
        File files[] = dir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
            {
                jar(jout, files[i], cut, filter);
            }
            else
            {
                if(files[i].getName().equals("nobuto.jar") 
                    || (filter != null && filter.accept(files[i]) == false))
                {
                    continue;
                }
                FileInputStream fin = new FileInputStream(files[i]);
                byte data[] = Utilities.readAll(fin);
                fin.close();
                String filename = files[i].toString().substring(cut);
                filename = filename.replace("\\", "/");
                JarEntry ze = new JarEntry(filename);
                verbosePrint(3, "Added to jar: " + filename);
                jout.putNextEntry(ze);
                jout.write(data);
                jout.closeEntry();
            }
        }
    }

    /**
     * Finds the specified jar file in an plugins Folder by searching for the name.
     * Searches just for a jar beginning with the given name. The name may include a version number
     * or parts of it. 
     * @param name
     * @return
     */
    public File findJarInPlugins(File pluginsDir, String name)
    {
        File jarFile = null;
        Stack<File> files = new Stack<>();
        files.add(pluginsDir);
        while(files.isEmpty() == false)
        {
            File f = files.pop();
            if(f.isDirectory())
            {
                File[] childs = f.listFiles();
                files.addAll(Arrays.asList(childs));
            }
            else
            {
                if(f.getName().startsWith(name))
                {
                    jarFile = f;
                    break;
                }
            }
        }
        return jarFile;
    }
    
}

