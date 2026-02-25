package au.com.devnull.graalson.trax;

import jakarta.json.JsonException;
import jakarta.json.JsonWriter;

/**
 *
 * @author wozza
 */
sealed interface WritableStructure permits GraalsonResult, GraalsonSource {

    public void writeStructure(JsonWriter writer) throws JsonException;

}
