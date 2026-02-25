package au.com.devnull.graalson.trax;

import static au.com.devnull.graalson.GraalsonValue.toValue;

import au.com.devnull.graalson.GraalsonProvider;
import au.com.devnull.graalson.trax.GraalsonTransformerFactory.JsonMode;
import jakarta.json.JsonArray;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
class GraalsonTransformer extends Transformer implements Templates {

    private Properties properties;
    private ErrorListener errorListener;
    private URIResolver resolver;
    private GraalsonSource source;
    private JsonMode mode;
    private Map<String, Object> parameters = new HashMap<>();
    private static final GraalsonProvider provider;

    static {
        synchronized (GraalsonTransformer.class) {
            provider = (GraalsonProvider) JsonProvider.provider();
        }
    }

    public GraalsonTransformer(
        ErrorListener errorListener,
        URIResolver resolver,
        GraalsonSource source,
        JsonMode mode
    ) {
        this(errorListener, resolver, mode);
        this.source = source;
    }

    public GraalsonTransformer(ErrorListener errorListener, URIResolver resolver, GraalsonSource source) {
        this(errorListener, resolver, source, null);
    }

    public GraalsonTransformer(ErrorListener errorListener, URIResolver resolver, JsonMode mode) {
        this(errorListener, resolver);
        this.mode = mode;
    }

    public GraalsonTransformer(ErrorListener errorListener, URIResolver resolver) {
        this.errorListener = errorListener;
        this.resolver = resolver;
    }

    static Context getPolyglotContext() {
        return provider.getPolyglotContext();
    }

    @Override
    public void transform(Source input, Result result) throws TransformerException {
        if (!(input instanceof GraalsonSource)) {
            throw new TransformerConfigurationException("source must be graalson source");
        }
        if (!(result instanceof GraalsonResult)) {
            throw new TransformerConfigurationException("result must be graalson result");
        }

        JsonStructure inputOperand = ((GraalsonSource) input).getJsonStructure();
        synchronized (getPolyglotContext()) {
            if (mode != null && mode != JsonMode.JSON_TRANSFORM) {
                handleStructuredOperation(inputOperand, (GraalsonResult) result);
            } else {
                handleTransform(inputOperand, (GraalsonResult) result);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleTransform(JsonStructure value, GraalsonResult result) throws TransformerException {
        Context ctx = getPolyglotContext();
        Map<String, Object> config = (Map<String, Object>) provider.getConfigInUse();
        Map<String, Object> configSnapshot = new HashMap<>(config);
        try {
            Object javaValue = toValue(value);
            ctx.getBindings("js").putMember("_", javaValue);
            ctx.getBindings("js").putMember("$$", config);

            Value resultValue = ctx.eval(this.source.source);

            if (!resultValue.hasMembers() || resultValue.isNull()) {
                resultValue = ctx.getBindings("js").getMember("$");
            }

            result.setValue(resultValue);
        } finally {
            // Restore config to pre-transform state so template mutations don't leak
            config.clear();
            config.putAll(configSnapshot);

            Value bindings = ctx.getBindings("js");
            if (bindings.hasMember("$")) {
                try {
                    bindings.removeMember("$");
                } catch (UnsupportedOperationException ignored) {}
            }
            if (bindings.hasMember("_")) {
                try {
                    bindings.removeMember("_");
                } catch (UnsupportedOperationException ignored) {}
            }
        }
    }

    private void handleStructuredOperation(JsonStructure operand2, GraalsonResult result) throws TransformerException {
        JsonStructure operand1 = this.source.getJsonStructure();
        if (operand1 == null) {
            throw new TransformerException(mode + " operation requires a first operand with JSON structure");
        }

        JsonValue outputValue;

        switch (mode) {
            case JSON_DIFF: {
                //rfc7369
                outputValue = provider.createMergeDiff(operand1, operand2).toJsonValue();
                break;
            }
            case JSON_MERGE: {
                //rfc7369
                outputValue = provider.createMergePatch(operand1).apply(operand2);
                break;
            }
            case JSON_PATCH_DIFF: {
                //rfc6902 uses stepwise operations
                outputValue = provider.createDiff(operand1, operand2).toJsonArray();
                break;
            }
            case JSON_PATCH_APPLY: {
                //rfc6902 uses stepwise operations
                if (!(operand1 instanceof JsonArray patchArray)) {
                    throw new TransformerException(
                        "JSON_PATCH_APPLY requires the first operand to be a JSON array, got " + operand2.getValueType()
                    );
                }
                outputValue = provider.createPatch(patchArray).apply(operand2);
                break;
            }
            default:
                throw new TransformerException("Unsupported JSON mode: " + mode);
        }

        if (outputValue == null) {
            throw new TransformerException(mode + " output was null");
        }

        result.setValue(toValue(outputValue));
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
