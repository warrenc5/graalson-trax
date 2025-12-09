package au.com.devnull.graalson.trax;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;

/**
 *
 * @author wozza
 */
public class GraalsonTransformerFactory  extends javax.xml.transform.TransformerFactory{

    private URIResolver resolver;
    private ErrorListener errorListener;
    private Map<String,Object> attributes = new HashMap<>();
    private Map<String,Boolean> features = new HashMap<>();

    @Override
    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        if(!(source instanceof GraalsonSource)){
            throw new TransformerConfigurationException("source must be graalson");
        }
        return new GraalsonTransformer(errorListener,resolver,(GraalsonSource) source);
    }

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return new GraalsonTransformer(errorListener, resolver);
    }

    @Override
    public Templates newTemplates(Source source) throws TransformerConfigurationException {

        if(!(source instanceof GraalsonSource)){
            throw new TransformerConfigurationException("source must be graalson");
        }
        return new GraalsonTransformer(errorListener,resolver,(GraalsonSource) source);
    }

    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset) throws TransformerConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public URIResolver getURIResolver() {
        return this.resolver;
    }

    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        this.features.put(name, value);
    }

    @Override
    public boolean getFeature(String name) {
        return this.features.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public void setErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    public ErrorListener getErrorListener() {
        return this.errorListener;
    }
    
}
