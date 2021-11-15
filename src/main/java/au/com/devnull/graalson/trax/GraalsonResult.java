package au.com.devnull.graalson.trax;

import au.com.devnull.graalson.GraalsonStructure;
import au.com.devnull.graalson.GraalsonValue;
import javax.json.JsonWriter;
import javax.xml.transform.Result;
import org.graalvm.polyglot.Value;

/**
 *
 * @author wozza
 */
public class GraalsonResult implements Result {

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
        this.structure = new GraalsonStructure(value);
        if (this.jwriter != null) {
            jwriter.write(this.structure);
        }
    }

    public Value getValue() {
        return value;
    }

    public GraalsonStructure getStructure() {
        return structure;
    }
}
