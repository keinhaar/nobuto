package de.exware.nobuto.utils;

/**
 * Simple class to check for OS
 * @author martin
 *
 */
public enum OperatingSystem
{
    WINDOWS
    ,UNIX
    ,MAC
    ,SOLARIS
    ;
    
    private static OperatingSystem os = null;
    
    public static OperatingSystem getOperatingSystem()
    {
        if(os == null)
        {
            String osname = System.getProperty("os.name").toLowerCase();
            os = osname.indexOf("win") >= 0 ? WINDOWS :
                osname.indexOf("mac") >= 0 ? MAC :
                    (osname.indexOf("nix") >= 0 || osname.indexOf("nux") >= 0) ? UNIX :
                        osname.indexOf("sunos") >= 0 ? SOLARIS : null;                 
        }
        return os;
    }
    
    /**
     * @return true, if OS is an Windows.
     */
    public static boolean isWindows()
    {
        return getOperatingSystem() == WINDOWS;
    }

    /**
     * @return true, if OS is an MAC.
     */
    public static boolean isMac()
    {
        return getOperatingSystem() == MAC;        
    }

    /**
     * @return true, if OS is an UNIX.
     */
    public static boolean isUnix()
    {
        return getOperatingSystem() == UNIX;
    }

    /**
     * @return true, if OS is an Solaris.
     */
    public static boolean isSolaris()
    {
        return getOperatingSystem() == SOLARIS;
    }
}
