package au.com.devnull.graalson.trax;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.xml.transform.Source;

/**
 *
 * @author wozza
 */
public class GraalsonSource implements Source, JsonStructure {

    private String systemId;
    org.graalvm.polyglot.Source source;
    private JsonStructure structure;

    public GraalsonSource(String source) throws IOException {
        this(org.graalvm.polyglot.Source.newBuilder("js",
                new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(source)),
                ClassLoader.getSystemClassLoader().getResource(source).toExternalForm()).build());
    }

    public GraalsonSource(String name, String source) throws IOException {
        this(org.graalvm.polyglot.Source.newBuilder("js",
                new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(source)),
                name).build());
    }

    public GraalsonSource(String name, Reader reader) throws IOException {
        this(org.graalvm.polyglot.Source.newBuilder("js", reader, name).build());
    }

    public GraalsonSource(org.graalvm.polyglot.Source source) {
        this.source = source;
    }

    public GraalsonSource(JsonReader jreader) {
        structure = jreader.read();
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    JsonStructure getJsonStructure() {
        return structure;
    }

    @Override
    public ValueType getValueType() {
        return structure.getValueType();
    }

    @Override
    public boolean isEmpty() {

        if (this.structure != null) {
            switch (structure.getValueType()) {
                case ARRAY:
                    return ((JsonArray) structure).isEmpty();
                case OBJECT:
                    return ((JsonObject) structure).isEmpty();
                default:
                    return true;
            }
        }
        if (this.source != null) {
            if (this.source.getLength() > 0) {
                return false;
            }
        }
        return true;
    }

}
