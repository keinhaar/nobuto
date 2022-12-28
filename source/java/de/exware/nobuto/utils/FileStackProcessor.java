package de.exware.nobuto.utils;

import java.io.File;
import java.util.Arrays;

/**
 * Process File Hierarchy based on a Stack
 * @author martin
 *
 */
abstract public class FileStackProcessor extends StackProcessor<File>
{
    @Override
    protected void addChilds(File parent)
    {
        File[] children = parent.listFiles();
        if(sortChildren)
        {
            Arrays.sort(children, comparator);
        }
        if(children != null)
        {
            for(int i=0;i<children.length;i++)
            {
                stack.push(children[i]);
            }
        }
    }
}