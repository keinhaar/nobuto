package de.exware.nobuto.utils;

import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import de.exware.nobuto.Main;

public class Utilities
{
    /**
     * reads the complete inputstream and returns an byte array, whos size
     * exactly matches the length of the stream. 
     * 
     * @param in the InputStream that should be read completely
     * @return the byte array containing the inputstreams data
     */
    public static byte[] readAll(InputStream in) throws IOException
    {
        BufferedInputStream bin = new BufferedInputStream(in);
        int length = 10000;
        if(length <= 0) length = 1; // length is not allowed to be 0 or smaller.
        byte[] buffer = new byte[length];
        int count = bin.read(buffer,0,length);
        int pos = 0;
        while(count != -1)
        {
            pos += count;
            if(pos == length)
            {
                length *= 2;
                buffer = (byte[])resizeArray(buffer,length);
            }
            count = bin.read(buffer,pos,length - pos);
            if(count == 0) //Give time to get new Data
            {
                try
                {
                    Thread.currentThread().wait(100);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        buffer = (byte[])resizeArray(buffer,pos);
        return buffer;
    }

    /**
     * this method will resize the given array to the new size. Caution!!! if
     * the new Size is smaller then the old array, then you might loose data in
     * the new array.
     */
    public static Object resizeArray(Object array, int newSize)
    {
        Class<?> type = array.getClass().getComponentType();
        Object tmp = Array.newInstance(type, newSize);
        int length = (Array.getLength(array) < newSize ? Array.getLength(array) : newSize);
        System.arraycopy(array, 0, tmp, 0, length);
        return tmp;
    }

    /**
     * Copy a File or directory. In case of an directory, only the contents will be copied. 
     * 
     * @param file1 will be copied to file 2
     * @param file2 name of the copy
     * @throws IOException if file1 does not exist, or file2 already exists.
     */
    public static void copy(File file1, File file2) throws IOException
    {
        copy(file1, file2, false);
    }

    /**
     * Copy a File or directory. In case of an directory, only the contents will be copied. 
     * 
     * @param file1 will be copied to file 2
     * @param file2 name of the copy
     * @param overwrite if true, existing file will be replaced
     * @throws IOException if file1 does not exist, or file2 already exists.
     */
    public static void copy(String file1, String file2, boolean overwrite) throws IOException
    {
        copy(new File(file1), new File(file2), overwrite);
    }
    
    /**
     * Copy a File or directory. In case of an directory, only the contents will be copied. 
     * 
     * @param file1 will be copied to file 2
     * @param file2 name of the copy
     * @throws IOException if file1 does not exist, or file2 already exists.
     */
    public static void copy(String file1, String file2) throws IOException
    {
        copy(new File(file1), new File(file2), false);
    }
    
    /**
     * Copy a File or directory. In case of an directory, only the contents will be copied. 
     * 
     * @param file1 will be copied to file 2
     * @param file2 name of the copy
     * @param overwrite if true, existing file will be replaced
     * @throws IOException if file1 does not exist, or file2 already exists.
     */
    public static void copy(File file1, String file2, boolean overwrite) throws IOException
    {
        copy(file1, new File(file2), overwrite);
    }
    
    /**
     * Copy a File or directory. In case of an directory, only the contents will be copied. 
     * 
     * @param file1 will be copied to file 2
     * @param file2 name of the copy
     * @param overwrite if true, existing file will be replaced
     * @throws IOException if file1 does not exist, or file2 already exists.
     */
    public static void copy(String file1, File file2, boolean overwrite) throws IOException
    {
        copy(new File(file1), file2, overwrite);
    }
    
    /**
     * Copy a File or directory. In case of an directory, only the contents will be copied. 
     * 
     * @param file1 will be copied to file 2
     * @param file2 name of the copy
     * @param overwrite if true, existing file will be replaced
     * @throws IOException if file1 does not exist, or file2 already exists.
     */
    public static void copy(File file1, File file2, boolean overwrite) throws IOException
    {
        if (file2.isFile() && file2.exists() && (overwrite == false))
        {
            throw new IOException("File " + file2.getCanonicalPath() + " exists");
        }
        if (file1.isDirectory())
        {
            String[] files = file1.list();
            if (file2.exists() == false)
            {
                file2.mkdirs();
            }
            for (int i = 0; i < files.length; i++)
            {
                File f1 = new File(file1, files[i]);
                File f2 = new File(file2, files[i]);
                copy(f1, f2, overwrite);
            }
        }
        else
        {
            if(file2.isDirectory())
            {
                file2 = new File(file2, file1.getName());
            }
            InputStream in = new BufferedInputStream(new FileInputStream(file1));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file2));
            copy(in, out);
            in.close();
            out.close();
            file2.setLastModified(file1.lastModified());
            verbosePrint(3, "Copied: " + file1 + " --> " + file2);
        }
    }

    /**
     * Copy the contents of in Stream to out, until in reaches EOF.
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buf = new byte[65536];
        int count = in.read(buf);
        while (count >= 0)
        {
            out.write(buf, 0, count);
            count = in.read(buf);
        }
    }

    /**
     * Deletes the File. If the File is a Directory, then all Files in that
     * directory, and its subdirectories will be deletet.
     * 
     * @param file
     *            The File or Directory to be deleted.
     */
    public static void delete(String file) throws IOException
    {
        delete(new File(file), true);
    }

    /**
     * Deletes the File. If the File is a Directory, then all Files in that
     * directory, and its subdirectories will be deletet.
     * 
     * @param file
     *            The File or Directory to be deleted.
     */
    public static void delete(File file) throws IOException
    {
        delete(file, true);
    }

    /**
     * Deletes the File.
     * 
     * @param file The File or Directory to be deleted.
     * @param rekursiv if true, the also all subdirectories and files in a folder will be deleted.
     */
    public static void delete(File file, boolean rekursiv) throws IOException
    {
        delete(file, rekursiv, null);
    }
    
    /**
     * Deletes the File.
     * 
     * @param file The File or Directory to be deleted.
     * @param rekursiv if true, the also all subdirectories and files in a folder will be deleted.
     */
    public static void delete(File file, boolean rekursiv, FileFilter filter) throws IOException
    {
        if (file.exists() == false)
        {
            return;
        }
        if (file.isDirectory() && rekursiv)
        {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++)
            {
                File f = new File(file, files[i]);
                delete(f, rekursiv, filter);
            }
            if(file.list().length == 0)
            {
                file.delete();
                verbosePrint(3, "Deleted: " + file.getAbsolutePath());
            }
        }
        else
        {
            if(filter == null || filter.accept(file))
            {
                file.delete();
                verbosePrint(3, "Deleted: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Runs an external Program and returns the standard output if the command exited with zero.
     * @param command
     * @param args
     * @return list of Strings with the command output.
     * @throws IOException if the command failed.
     * @throws InterruptedException 
     */
    public static List<String> runCommandReadOutput(String dir, String ... commandAndParams) throws IOException, InterruptedException
    {
        List<String> ret = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder(commandAndParams);
        if(dir != null)
        {
            pb.directory(new File(dir));
        }
        Process p = pb.start();
        readOutput(p, ret, null);
        int exitCode = p.waitFor();
        if(exitCode != 0)
        {
            throw new IOException("Command did not complete normally");
        }
        return ret;
    }
    
    /**
     * Runs an external Program and returns the return value.
     * This is used to check the existance of tools like svn or javac a.s.o.
     * May be called like this: checkTool("javac", "-version");
     * If 0 is returned, the the tool is in the path.
     * Other usage would be to call the compiler.
     * @param command
     * @param args
     * @param dir The Directory where the process is run.
     * @return
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static int runCommand(String dir, boolean outputInherit, String ... commandAndParams) throws IOException, InterruptedException
    {
        ProcessBuilder pb = new ProcessBuilder(commandAndParams);
        if(dir != null)
        {
            pb.directory(new File(dir));
        }
        if(outputInherit)
        {
            pb.inheritIO();
        }
        Process p = pb.start();
        readOutput(p, null, null);
        return p.waitFor();
    }
    
    public static int runCommand(String ... commandAndParams) throws IOException, InterruptedException
    {
        return runCommand(null, true, commandAndParams);
    }    
    
    public static int runCommand(boolean inheritOutput, String ... commandAndParams) throws IOException, InterruptedException
    {
        return runCommand(null, inheritOutput, commandAndParams);
    }    
    
    public static void readOutput(Process process, List<String> output, List<String> errorOutput) throws IOException
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String out = input.readLine();
        String err = error.readLine();
        while (out != null || err != null)
        //read both streams to avoid process blocking if output
        //is not read from one stream.
        {
            if (out != null && output != null)
            {
                output.add(out);
            }
            if (err != null && errorOutput != null)
            {
                errorOutput.add(err);
            }
            out = input.readLine();
            err = error.readLine();
        }
    }

    /**
     * Reads the Input from Commandline, if in Headless mode. Otherwise a Dialog will pop up.
     * @param echoText
     * @return
     * @throws IOException
     */
    public static String readInput(String echoText) throws IOException
    {
        String in;
        if(GraphicsEnvironment.isHeadless())
        {
            System.out.print(echoText);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))
            {
                @Override
                public void close() throws IOException
                {
                }
            };
            in = reader.readLine();
            reader.close();
        }
        else
        {
            in = JOptionPane.showInputDialog(echoText);
        }
        return in;
    }
    
    /**
     * Replaces a pattern in a File. The File is not allowed to be larger then 1.000.000 bytes.
     * @param file the file, where the replacement is made.
     * @param charset the characterSet used to read and write the file.
     * @param pattern a regular expression to be replaced in the whole file.
     * @param replacement the replacement Text.
     * @throws IOException
     */
    public static void replaceInFile(String file, String charset, String pattern, String replacement) throws IOException
    {
        Charset cset = Charset.forName(charset); 
        Path path = Paths.get(file);
        if(Files.exists(path))
        {
            if(Files.size(path) > 1000000)
            {
                throw new IOException("File to large");
            }
            byte[] data = Files.readAllBytes(path);
            String str = new String(data, cset);
            String newStr = str.replaceAll(pattern, replacement);
            if(str.equals(newStr))
            {
                verbosePrint(1, "Nothing to replace in " + path.toAbsolutePath() + ": " + pattern + " -> " + replacement);
            }
            else
            {
                verbosePrint(1, "Replacing in " + path.toAbsolutePath() + ": " + pattern + " -> " + replacement);
                BufferedWriter writer = Files.newBufferedWriter(path, cset);
                writer.write(newStr);
                writer.close();
            }
        }
        else
        {
            verbosePrint(0, "Nothing replaced, because file does not exist: " + file);
        }
    }
    
    public static String readTextFile(String file) throws IOException
    {
        return readTextFile(file, "UTF-8");
    }
    
    public static String readTextFile(String file, String charset) throws IOException
    {
        Charset cset = Charset.forName(charset); 
        Path path = Paths.get(file);
        byte[] data = Files.readAllBytes(path);
        String str = new String(data, cset);
        return str;
    }
    
    /**
     * Prints a message to the console, f the verbose level is higher or equal to the current verboseLevel. 
     * @param verboseLevel
     * @param text
     */
    public static void verbosePrint(int verboseLevel, String text)
    {
        if(Main.verboseLevel >= verboseLevel)
        {
            System.out.println(text);
        }
    }

    /**
     * Runs the given command and throws an exception if the returned value is not 0.
     * @param commandAndParams
     * @throws IOException
     * @throws InterruptedException
     */
    public static void checkTool(String ... commandAndParams) throws IOException, InterruptedException
    {
        int ret = 0;
        Exception ex = null;
        try
        {
            ret = runCommand(null, false, commandAndParams);
        }
        catch(Exception ex2)
        {
            ex = ex2;
        }
        if(ret != 0 || ex != null)
        {
            throw new IOException("The Tool '" + commandAndParams[0] + "' could not be found, or did not return zero", ex);
        }
    }
    
    /**
     * Compares two Version numbers. The Version numbers must be numeric
     * numbers separated by ".". Examples are "1", "1.2", "3.4.2" a.s.o.
     * @param version
     * @param version2
     * @return -1 if version2 < version, 0 if version2 == version and 1 if version2 > version.
     */
    public static int compareVersions(String version, String version2)
    {
        int ret = 0;
        String[] s1 = version.split("\\.");
        String[] s2 = version2.split("\\.");
        int length = s1.length > s2.length ? s1.length : s2.length;
        int[] v1 = new int[length];
        int[] v2 = new int[length];
        for (int i = 0; i < s1.length; i++)
        {
            v1[i] = Integer.parseInt(s1[i]);
        }
        for (int i = 0; i < s2.length; i++)
        {
            v2[i] = Integer.parseInt(s2[i]);
        }
        for(int i=0;i<length;i++)
        {
            if(v1[i] < v2[i])
            {
                ret = 1;
                break;
            }
            else if(v1[i] > v2[i])
            {
                ret = -1;
                break;
            }
        }
        return ret;
    }
    
    public static final void echo(String text)
    {
        System.out.println(text);
    }
    
    public static final void echo(String text, String file) throws IOException
    {
        echo(text, new File(file), false);
    }
    
    public static final void echo(String text, String file, boolean append) throws IOException
    {
        echo(text, new File(file), append);
    }
    
    public static final void echo(String text, File file, boolean append) throws IOException
    {
        FileOutputStream out = new FileOutputStream(file, append);
        out.write(text.getBytes("UTF-8"));
        out.close();
    }

    /**
     * Creates a ZIP File.
     * @param filename the name of the resulting zip.
     * @param sourceDir the directory that will be in the zip. The directory itself is not included. only the contents
     * @throws IOException
     */
    public static void zip(String filename, String sourceDir) throws IOException
    {
        zip(filename, new File(sourceDir));
    }

    /**
     * Creates a ZIP File.
     * @param filename the name of the resulting ZIP.
     * @param sourceDir the directory that will be in the ZIP. The directory itself is not included. only the contents
     * @throws IOException
     */
    public static void zip(String filename, String sourceDir, FileFilter filter) throws IOException
    {
        zip(filename, new File(sourceDir), filter);
    }

    /**
     * Creates a ZIP File.
     * @param filename the name of the resulting ZIP.
     * @param sourceDir the directory that will be in the ZIP. The directory itself is not included. only the contents
     * @throws IOException
     */
    public static void zip(String filename, File sourceDir) throws IOException
    {
        zip(filename, sourceDir, null);
    }
    
    /**
     * Creates a ZIP File.
     * @param filename the name of the resulting ZIP.
     * @param sourceDir the directory that will be in the ZIP. The directory itself is not included. only the contents
     * @throws IOException
     */
    public static void zip(String filename, File sourceDir, FileFilter filter) throws IOException
    {
        ZipOutputStream zout;
        zout = new ZipOutputStream(new FileOutputStream(filename));
        zip(zout, sourceDir, sourceDir.toString().length() + 1, filter);
        zout.finish();
        zout.flush();
        zout.close();
    }

    /**
     * 
     * @param zout
     * @param dir
     * @param cut gives the position, where the pathname in the zip will start.
     * @throws IOException
     */
    private static void zip(ZipOutputStream zout, File dir, int cut, FileFilter filter) throws IOException
    {
        File files[] = dir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
            {
                zip(zout, files[i], cut, filter);
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
                ZipEntry ze = new ZipEntry(filename);
                verbosePrint(3, "Added to zip: " + filename);
                zout.putNextEntry(ze);
                zout.write(data);
                zout.closeEntry();
            }
        }
    }

    public static final void concat(File target, File ... filesToConcat) throws IOException
    {
        FileOutputStream fout = new FileOutputStream(target, true);
        for(int i=0;i<filesToConcat.length;i++)
        {
            verbosePrint(2, "Concatenating " + filesToConcat[i] + " to " + target);
            FileInputStream fin = new FileInputStream(filesToConcat[i]);
            copy(fin, fout);
            fin.close();
        }
        fout.close();
    }
    
}
