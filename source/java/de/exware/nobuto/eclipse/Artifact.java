package de.exware.nobuto.eclipse;

import org.w3c.dom.Element;

import de.exware.nobuto.utils.W3CDomUtils;

public class Artifact
{
    private Element artifact;
    
    public Artifact(Element artifact)
    {
        this.artifact = artifact;
    }
    
    protected Element getElement()
    {
        return artifact;
    }
    
    public void addProperty(String name, String value)
    {
        Element properties = W3CDomUtils.getOrCreate(artifact, "properties");
        Element prop = W3CDomUtils.addElement(properties, "property");
        prop.setAttribute("name", name);
        prop.setAttribute("value", value);
    }
}
