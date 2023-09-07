import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.IOException;

import de.exware.nobuto.utils.Utilities;


public class Build extends de.exware.nobuto.java.JavaBuilder
{
    private static final String CLASSES_DIR = "out";
    private static final String TMP = "tmp";
    private static final String DISTRIBUTION_DIR = "dist";
    
    public Build()
    {
        super("de.exware.nobuto");
    }
    
    @Override
    public void clean() throws IOException
    {
        verbosePrint(1, "Cleaning up");
        Utilities.delete(CLASSES_DIR);
        Utilities.delete(DISTRIBUTION_DIR);
        Utilities.delete(TMP);
    }

    @Override
    public void compile() throws Exception
    {
        addSources("source/java");

        Utilities.copy("source/java", CLASSES_DIR, true);
        
        super.compile();
        Utilities.delete(CLASSES_DIR + "/" + "nobuto.jar");
        Utilities.delete(CLASSES_DIR + "/" + "Build.class");
        Utilities.copy("version.txt", CLASSES_DIR, true);
        jar("build/nobuto.jar", CLASSES_DIR, "resources/MANIFEST.MF");
        Utilities.copy("build/classes/Build.class", CLASSES_DIR, true);
    }
}
