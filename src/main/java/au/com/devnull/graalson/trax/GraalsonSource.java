package au.com.devnull.graalson.trax;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import javax.xml.transform.Source;

public class GraalsonSource implements Source, JsonStructure, WritableStructure {

    private String systemId;
    org.graalvm.polyglot.Source source;
    private JsonStructure structure;

    public GraalsonSource(org.graalvm.polyglot.Source source) {
        this.source = source;
    }

    public GraalsonSource(String name, Reader reader) throws IOException {
        this(org.graalvm.polyglot.Source.newBuilder("js", reader, name).build());
    }

    public GraalsonSource(String resourcePath) throws IOException {

        this(getResourceSystemId(resourcePath), getResourceAsStreamOrThrow(resourcePath));
    }

    public GraalsonSource(String name, InputStream is) throws IOException {
        this(name, new InputStreamReader(is));
    }

    public GraalsonSource(String name, String resourcePath) throws IOException {
        this(name, getResourceAsStreamOrThrow(resourcePath));
    }

    public GraalsonSource(JsonReader jreader) {
        structure = jreader.read();
    }

    private static InputStream getResourceAsStreamOrThrow(String resourcePath) throws IOException {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return is;
    }

    private static String getResourceSystemId(String resourcePath) throws IOException {
        java.net.URL url = ClassLoader.getSystemClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return url.toExternalForm();
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

    @Override
    public void writeStructure(JsonWriter writer) throws JsonException {
        writer.write(this.structure);
    }
}
