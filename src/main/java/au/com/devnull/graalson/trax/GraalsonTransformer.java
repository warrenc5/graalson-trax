package au.com.devnull.graalson.trax;

import au.com.devnull.graalson.GraalsonProvider;
import au.com.devnull.graalson.GraalsonStructure;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.json.JsonStructure;
import javax.json.spi.JsonProvider;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 *
 * @author wozza
 */
public class GraalsonTransformer extends Transformer implements Templates {

    private Properties properties;
    private ErrorListener errorListener;
    private URIResolver resolver;
    private GraalsonSource source;
    private Map<String, Object> parameters = new HashMap<>();
    private static final GraalsonProvider provider;

    static {
        synchronized (GraalsonTransformer.class) {
            provider = (GraalsonProvider) JsonProvider.provider();
        }
    }

    public GraalsonTransformer(ErrorListener errorListener, URIResolver resolver, GraalsonSource source) {
        this(errorListener, resolver);
        this.source = source;
    }

    public GraalsonTransformer(ErrorListener errorListener, URIResolver resolver) {
        this.properties = properties;
        this.errorListener = errorListener;
        this.resolver = resolver;
    }

    static Context getPolyglotContext() {
        return provider.getPolyglotContext();
    }

    @Override
    public void transform(Source input, Result result) throws TransformerException {
        if (!(source instanceof GraalsonSource)) {
            throw new TransformerConfigurationException("source must be graalson source");
        }
        if (!(result instanceof GraalsonResult)) {
            throw new TransformerConfigurationException("result must be graalson result");
        }

        JsonStructure value = ((GraalsonSource) input).getJsonStructure();
        Object javaValue = provider.toValue(value);
        getPolyglotContext().getBindings("js").putMember("_", javaValue);

        Value resultValue = getPolyglotContext().eval(((GraalsonSource) this.source).source);

        if (!resultValue.hasMembers() || resultValue.isNull()) {
            resultValue = getPolyglotContext().getBindings("js").getMember("$");
        }

        ((GraalsonResult) result).setValue(resultValue);
    }

    @Override
    public void setParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    @Override
    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    @Override
    public void clearParameters() {
        this.parameters.clear();
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
    public void setOutputProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Properties getOutputProperties() {
        return this.properties;
    }

    @Override
    public void setOutputProperty(String name, String value) throws IllegalArgumentException {
        properties.put(name, value);
    }

    @Override
    public String getOutputProperty(String name) throws IllegalArgumentException {
        return this.properties.getProperty(name);
    }

    @Override
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        this.errorListener = listener;
    }

    @Override
    public ErrorListener getErrorListener() {
        return this.errorListener;
    }

    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return this;
    }
}
