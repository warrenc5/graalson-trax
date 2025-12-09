package au.com.devnull.graalson.trax;

import jakarta.json.JsonException;
import jakarta.json.JsonWriter;

/**
 *
 * @author wozza
 */
public interface WritableStructure {

    public void writeStructure(JsonWriter writer) throws JsonException;

}
