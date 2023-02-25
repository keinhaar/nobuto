package de.exware.nobuto.eclipse;

import org.w3c.dom.Element;

import de.exware.nobuto.utils.W3CDomUtils;

public class Unit
{
    private Element unit;
    private Element provides;
    
    public Unit(Element unit, Element provides)
    {
        this.unit = unit;
        this.provides = provides;
    }
    
    protected Element getElement()
    {
        return unit;
    }
    
    protected Element getProvides()
    {
        return provides;
    }

    public void addProperty(String name, String value)
    {
        Element properties = W3CDomUtils.getOrCreate(unit, "properties");
        Element prop = W3CDomUtils.addElement(properties, "property");
        prop.setAttribute("name", name);
        prop.setAttribute("value", value);
    }
    
    public void addLicense(String uri, String url, String text)
    {
        Element licenses = W3CDomUtils.getOrCreate(unit, "licenses");
        Element license = W3CDomUtils.addElement(licenses, "license");
        license.setAttribute("uri", uri);
        license.setAttribute("url", url);
        license.setTextContent(text);
    }
    
    public void addCopyright(String text)
    {
        Element copyright = W3CDomUtils.getOrCreate(unit, "copyright");
        copyright.setTextContent(text);
    }
    
    public void addRequired(String namespace, String name, String range)
    {
        Element properties = W3CDomUtils.getOrCreate(unit, "requires");
        Element prop = W3CDomUtils.addElement(properties, "required");
        prop.setAttribute("namespace", namespace);
        prop.setAttribute("name", name);
        prop.setAttribute("range", range);
    }    
}
