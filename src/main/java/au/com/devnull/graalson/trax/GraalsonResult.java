package au.com.devnull.graalson.trax;

import au.com.devnull.graalson.GraalsonProvider;
import au.com.devnull.graalson.GraalsonStructure;
import jakarta.json.JsonException;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.spi.JsonProvider;
import javax.xml.transform.Result;
import org.graalvm.polyglot.Value;

/**
 *
 * @author wozza
 */
public class GraalsonResult implements Result, WritableStructure {

    private String systemId;
    private Value value;
    private JsonWriter jwriter;
    private GraalsonStructure structure;

    public GraalsonResult(JsonWriter jwriter) {
        this.jwriter = jwriter;
    }

    public GraalsonResult() {
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    void setValue(Value resultValue) {
        this.value = resultValue;
        if (this.jwriter != null) {
            JsonValue jsonValue = ((GraalsonProvider) JsonProvider.provider()).createValue(resultValue);
            jwriter.write((GraalsonStructure)jsonValue);
            jwriter.close();
        }
    }

    public Value getValue() {
        return value;
    }

    public GraalsonStructure getStructure() {
        return structure;
    }

    @Override
    public void writeStructure(JsonWriter writer) throws JsonException {
        writer.write(this.structure);
    }
}
