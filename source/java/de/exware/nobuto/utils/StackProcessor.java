package de.exware.nobuto.utils;

import java.util.Comparator;
import java.util.Stack;

abstract public class StackProcessor<T>
{
    protected Stack<T> stack = new Stack<>();
    protected boolean sortChildren;
    protected Comparator<T> comparator;
    
    public StackProcessor()
    {
    }

    public StackProcessor(boolean sortChildren)
    {
        this.sortChildren = sortChildren;
    }

    /**
     * Starts immediatly with the processing. Calling process is not required.
     * @param root
     * @param sortChildren
     */
    public StackProcessor(T root, boolean sortChildren)
    {
        this.sortChildren = sortChildren;
        process(root);
    }

    @SafeVarargs
    public final void process(T ... roots)
    {
        stack.clear();
        for(int i=0;i<roots.length;i++)
        {
            stack.push(roots[i]);
        }
        while(stack.isEmpty() == false)
        {
            T element = stack.pop();
            addChilds(element);
            processElement(element);
        }
    }

    /**
     * Do what needs to be done with a single Element.
     * @param element
     */
    protected abstract void processElement(T element);

    /**
     * Subclass should add children to the Stack here.
     * @param parent
     */
    protected abstract void addChilds(T parent);

    public Comparator<T> getComparator()
    {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator)
    {
        this.comparator = comparator;
    }
}
