package de.exware.nobuto;

import java.io.File;
import java.lang.reflect.Method;

import de.exware.nobuto.java.JavaBuilder;

/**
 * Main Method for the nobuto Build.
 * Reads the Arguments and compiles the Build Java File if needed, and executes it.
 * @author martin
 */
public class Main
{
    public static String target;
    public static int verboseLevel = 0;
    public static String[] args;
    
    public static void main(String[] args) throws Exception
    {
        Main.args = args;
        System.out.println("JAVA Version: " + System.getProperty("java.version"));
        System.out.println("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
            + " " + System.getProperty("os.arch"));
        
        int h1 = getArgumentIndex(args, "--help");
        int h2 = getArgumentIndex(args, "-help");
        if(h1 >= 0 || h2 >= 0)
        {
            System.out.println("Nobuto Build System.");
            System.out.println("usage: nobuto.sh [-t|--target <TARGET>]");
            return;
        }
        
        setVerboseLevel(args);
        target = getArgument(args, "-t");
        if(target == null)
        {
            target = getArgument(args, "--target");
        }
        if(target == null)
        {
            target = "dist";
        }
        String clazzname = getArgument(args, "-c");
        String buildSourceDir = getArgument(args, "-d");
        if(clazzname == null)
        {
            buildSourceDir = "build";
            File sf = new File(buildSourceDir , "Build.java");
            if(sf.exists())
            {
                System.out.println("Trying default: " + sf.toString());
                clazzname = "Build";
            }
            else
            {
                System.out.println("No Default. Fallback to JavaBuilder");
                clazzname = JavaBuilder.class.getName();
            }
        }
        if(clazzname != JavaBuilder.class.getName())
        {
            File sf = new File(buildSourceDir, clazzname + ".java");
            compileBuild(buildSourceDir, clazzname, sf);
        }
        Class<?> clazz = Class.forName(clazzname);
        Method method = clazz.getMethod(target, (Class[])null);
        System.out.println("Executing target '" + target + "' in Class '" + clazz.getName() + "'");
        Builder b = (Builder) clazz.getDeclaredConstructor().newInstance();
        method.invoke(b, (Object[])null);
    }
    
    private static void setVerboseLevel(String[] args)
    {
        for(int i=0;i<args.length;i++)
        {
            if(args[i].startsWith("-v"))
            {
                verboseLevel = args[i].length() - 1;
            }
        }
    }

    /**
     * Compiles the custom build file.
     * @param clazzname 
     * @param buildSourceDir 
     * @param clazzname
     * @throws Exception
     */
    private static void compileBuild(String buildSourceDir, String clazzname, File clazzfile) throws Exception
    {
        if(System.getProperty("nobuildcompile") != null)
        {
            System.out.println("No Build compile");
        }
        else
        {
            System.out.println("Compile Builder Class");
            JavaBuilder bj = new JavaBuilder("");
            bj.setOutputFolder(buildSourceDir + "/classes");
            bj.addSources(buildSourceDir);
            bj.addClasspathItem("build/nobuto.jar");
            bj.compile(clazzfile.getPath());
        }
    }

    /**
     * Gibt den ersten Parameter zu einer Kommandozeilen Option zurück. Wird auf der Kommandozeile "-x param" angegeben,
     * dann liefert der aufruf getArgument(args,"-x") den String "param" zurück. Wenn kein Parameter angegeben ist, dann
     * wird eine Warnung auf der Konsole ausgegeben.
     * 
     * @param args
     *            String Array mit der Kommandozeile
     * @param arg
     *            Die zurückzuliefernde Option
     * @return Der erste Parameter der auf "arg" folgt oder null.
     */
    public static String getArgument(String[] args, String arg)
    {
        int i = getArgumentIndex(args, arg);
        if (i >= 0)
        {
            if (i + 1 < args.length)
            {
                String str = getArgumentAt(args, i + 1);
                return str;
            }
        }
        return null;
    }

    /**
     * Gibt das Argument am angegebenen Index zurück.
     * 
     * @param args
     *            String Array mit der Kommandozeile
     * @param index
     *            Der index des Argumentes
     */
    public static String getArgumentAt(String[] args, int index)
    {
        return args[index];
    }

    /**
     * Gibt den Index der angegebenen Option zurück.
     * 
     * @param args
     *            String Array mit der Kommandozeile
     * @param arg
     *            Die zurückzuliefernde Option
     * @return index der Option "arg"
     */
    public static int getArgumentIndex(String[] args, String arg)
    {
        if (args == null) 
        {
            return -1;
        }
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(arg))
            {
                return i;
            }
        }
        return -1;
    }


}
