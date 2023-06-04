package de.exware.nobuto.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class W3CDomUtils
{
    public static String getAttribute(Element el,String name)
    {
        String value = null;
        if(el.hasAttribute(name))
        {
            value = el.getAttribute(name);
        }
        return value;
    }
    
    public static Document read(URL url) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream in = url.openStream();
        Document doc = builder.parse(in);
        in.close();
        return doc;
    }
    
    public static Document read(String file) throws ParserConfigurationException, IOException, SAXException
    {
        return read(new File(file));
    }
    
    public static Document read(File file) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        Document doc = builder.parse(in);
        in.close();
        return doc;
    }
    
    public static Document createDocument() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        return doc;
    }
    
    public static Element addElement(Element parent, String childname)
    {
        Element el = parent.getOwnerDocument().createElement(childname);
        parent.appendChild(el);
        return el;
    }
    
    public static Node selectSingleNode(Node node,String xpath) throws XPathExpressionException
    {
        Node retnode = null;
        List<Node> nodes = selectNodes(node, xpath);
        if(nodes != null && nodes.size() > 0)
        {
            retnode = nodes.get(0);
        }
        return retnode;
    }
    
    public static List<Node> nodeListToList(NodeList nlist)
    {
        List<Node> nodes = new ArrayList<>();
        for(int i=0;i<nlist.getLength();i++)
        {
            nodes.add(nlist.item(i));
        }
        return nodes;
    }
    
    public static List<Node> selectNodes(Node node, String xpath) throws XPathExpressionException
    {
        XPath path = XPathFactory.newInstance().newXPath();
        XPathExpression expr = path.compile(xpath);
        NodeList nlist = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
        List<Node> list = nodeListToList(nlist);
        return list;
    }
    
    public static List<Node> getChildsByName(Node node, String name)
    {
        List<Node> ret = new ArrayList<>();
        NodeList list = node.getChildNodes();
        for(int i=0;i<list.getLength();i++)
        {
            Node n = list.item(i);
            if(n.getNodeName().equals(name))
            {
                ret.add(n);
            }
        }
        return ret;
    }

    public static void write(Document doc, File file) throws IOException, TransformerException
    {
        DOMSource source = new DOMSource(doc);
        FileWriter writer = new FileWriter(file);
        StreamResult result = new StreamResult(writer);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(source, result);        
    }

    public static Element getOrCreate(Element node, String tag)
    {
        Element element = null;
        try
        {
            element = (Element) W3CDomUtils.selectSingleNode(node, tag);
            if(element == null)
            {
                element = W3CDomUtils.addElement(node, tag);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return element;
    }
}
