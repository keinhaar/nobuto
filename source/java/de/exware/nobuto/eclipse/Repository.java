package de.exware.nobuto.eclipse;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.exware.nobuto.utils.W3CDomUtils;

public class Repository
{
    private Document contentXMLDoc;
    private Document artifactsXMLDoc;
    private Element units;
    
    public Repository()
    {
        try
        {
            contentXMLDoc = W3CDomUtils.createDocument();
            Element repo = contentXMLDoc.createElement("repository");
            contentXMLDoc.appendChild(repo);
            repo.setAttribute("type", "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository");
            repo.setAttribute("version", "1");
            repo.setAttribute("name", "repo");
            units = W3CDomUtils.addElement(repo, "units");

            artifactsXMLDoc = W3CDomUtils.createDocument();
            repo = artifactsXMLDoc.createElement("repository");
            artifactsXMLDoc.appendChild(repo);
            repo.setAttribute("type", "org.eclipse.equinox.p2.artifact.repository.simpleRepository");
            repo.setAttribute("version", "1");
            repo.setAttribute("name", "repo");
            
            Element mappings = W3CDomUtils.addElement(repo, "mappings");
            Element rule = W3CDomUtils.addElement(mappings, "rule");
            rule.setAttribute("filter", "(& (classifier=osgi.bundle) (format=packed))");
            rule.setAttribute("output", "${repoUrl}/plugins/${id}_${version}.jar.pack.gz");
            rule = W3CDomUtils.addElement(mappings, "rule");
            rule.setAttribute("filter", "(& (classifier=osgi.bundle))");
            rule.setAttribute("output", "${repoUrl}/plugins/${id}_${version}.jar");
            rule = W3CDomUtils.addElement(mappings, "rule");
            rule.setAttribute("filter", "(& (classifier=binary))");
            rule.setAttribute("output", "${repoUrl}/binary/${id}_${version}");
            rule = W3CDomUtils.addElement(mappings, "rule");
            rule.setAttribute("filter", "(& (classifier=org.eclipse.update.feature) (format=packed))");
            rule.setAttribute("output", "${repoUrl}/features/${id}_${version}.jar.pack.gz");
            rule = W3CDomUtils.addElement(mappings, "rule");
            rule.setAttribute("filter", "(& (classifier=org.eclipse.update.feature))");
            rule.setAttribute("output", "${repoUrl}/features/${id}_${version}.jar");
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }
    
    public Artifact addArtifact(String classifier, String id, String version)
    {
        Element artifacts = W3CDomUtils.getOrCreate(artifactsXMLDoc.getDocumentElement(), "artifacts");
        Element artifact = W3CDomUtils.addElement(artifacts, "artifact");
        artifact.setAttribute("classifier", classifier);
        artifact.setAttribute("id", id);
        artifact.setAttribute("version", version);

        return new Artifact(artifact);
    }
    
    public Unit addCategoryUnit(String id, String version, String name, String description)
    {
        Unit unit = addUnit(id, version);
        
        unit.addProperty("org.eclipse.equinox.p2.type.category", "true");
        unit.addProperty("org.eclipse.equinox.p2.name", name);
        unit.addProperty("org.eclipse.equinox.p2.description", description);

        return unit;
    }
    
    public Unit addPluginUnit(String id, String version)
    {
        Unit unit = addUnit(id, version);
        Element update = W3CDomUtils.addElement(unit.getElement(), "update");
        update.setAttribute("id", id);
        update.setAttribute("range", "[0.0.0," + version + ")");
        update.setAttribute("severity", "0"); //just to avoid warning message in errorlog.

        Element artifacts = W3CDomUtils.addElement(unit.getElement(), "artifacts");
        Element artifact = W3CDomUtils.addElement(artifacts, "artifact");
        artifact.setAttribute("classifier", "osgi.bundle");
        artifact.setAttribute("id", id);
        artifact.setAttribute("version", version);

        Element provided = W3CDomUtils.addElement(unit.getProvides(), "provided");
        provided.setAttribute("namespace", "osgi.bundle");
        provided.setAttribute("name", id);
        provided.setAttribute("version", version);

        provided = W3CDomUtils.addElement(unit.getProvides(), "provided");
        provided.setAttribute("namespace", "org.eclipse.equinox.p2.eclipse.type");
        provided.setAttribute("name", "bundle");
        provided.setAttribute("version", "1.0.0");

        Element touchpoint = W3CDomUtils.addElement(unit.getElement(), "touchpoint");
        touchpoint.setAttribute("id", "org.eclipse.equinox.p2.osgi");
        touchpoint.setAttribute("version", "1.0.0");
        
        return unit;
    }
    
    public Unit addFeatureUnit(String id, String version)
    {
        Unit unit = addUnit(id, version);
        Element update = W3CDomUtils.addElement(unit.getElement(), "update");
        update.setAttribute("id", id);
        update.setAttribute("range", "[0.0.0," + version + ")");
        
        Element artifacts = W3CDomUtils.addElement(unit.getElement(), "artifacts");
        Element artifact = W3CDomUtils.addElement(artifacts, "artifact");
        artifact.setAttribute("classifier", "org.eclipse.update.feature");
        artifact.setAttribute("id", id);
        artifact.setAttribute("version", version);

        Element provided = W3CDomUtils.addElement(unit.getProvides(), "provided");
        provided.setAttribute("namespace", "org.eclipse.update.feature");
        provided.setAttribute("name", id);
        provided.setAttribute("version", version);

        provided = W3CDomUtils.addElement(unit.getProvides(), "provided");
        provided.setAttribute("namespace", "org.eclipse.equinox.p2.eclipse.type");
        provided.setAttribute("name", "feature");
        provided.setAttribute("version", "1.0.0");

        Element touchpoint = W3CDomUtils.addElement(unit.getElement(), "touchpoint");
        touchpoint.setAttribute("id", "org.eclipse.equinox.p2.osgi");
        touchpoint.setAttribute("version", "1.0.0");

        return unit;
    }
    
    public Unit addUnit(String id, String version)
    {
        Element unit = W3CDomUtils.addElement(units, "unit");
        unit.setAttribute("id", id);
        unit.setAttribute("version", version);
        
        Element provides = W3CDomUtils.addElement(unit, "provides");
        Element provided = W3CDomUtils.addElement(provides, "provided");
        provided.setAttribute("namespace", "org.eclipse.equinox.p2.iu");
        provided.setAttribute("name", id);
        provided.setAttribute("version", version);

        return new Unit(unit, provides);
    }
    
    public void write(File dir) throws IOException, TransformerException
    {
        W3CDomUtils.write(contentXMLDoc, new File(dir, "content.xml"));
        W3CDomUtils.write(artifactsXMLDoc, new File(dir, "artifacts.xml"));
    }
}
