package au.com.devnull.graalson;

import au.com.devnull.graalson.trax.GraalsonResult;
import au.com.devnull.graalson.trax.GraalsonSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author wozza
 */
public class GraalsonTransformerTest {

    @Test
    public void testTransformer() throws TransformerConfigurationException, TransformerException, IOException {

        System.setProperty("javax.xml.transform.TransformerFactory", "au.com.devnull.graalson.trax.GraalsonTransformerFactory");

        Map<String, Object> config = new HashMap<>();
        config.put("spaces", Integer.valueOf(4));
        JsonWriterFactory wfactory = Json.createWriterFactory(config);
        JsonWriter jwriter = wfactory.createWriter(new PrintWriter(System.out));
        JsonReader jreader = Json.createReader(ClassLoader.getSystemClassLoader().getResourceAsStream("default.json"));

        GraalsonSource template = new GraalsonSource("template1.js");
        GraalsonSource source = new GraalsonSource(jreader);
        GraalsonResult result = new GraalsonResult(jwriter);

        TransformerFactory.newInstance().newTemplates(template).newTransformer().transform(source, result);

        assertNotNull(result.getValue());
    }

}
