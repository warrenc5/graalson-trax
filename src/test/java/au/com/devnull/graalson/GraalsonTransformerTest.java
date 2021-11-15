package au.com.devnull.graalson;

import au.com.devnull.graalson.trax.GraalsonResult;
import au.com.devnull.graalson.trax.GraalsonSource;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.json.JSONException;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author wozza
 */
public class GraalsonTransformerTest {

    @Test
    public void testTransformer() throws TransformerConfigurationException, TransformerException, IOException, JSONException {

        System.setProperty("javax.xml.transform.TransformerFactory", "au.com.devnull.graalson.trax.GraalsonTransformerFactory");

        Map<String, Object> config = new HashMap<>();
        config.put("spaces", Integer.valueOf(4));

        StringWriter writer = new StringWriter();
        JsonWriterFactory wfactory = Json.createWriterFactory(config);
        //JsonWriter jwriter = wfactory.createWriter(new PrintWriter(System.out));
        JsonWriter jwriter = wfactory.createWriter(writer);

        JsonReader jreader = Json.createReader(ClassLoader.getSystemClassLoader().getResourceAsStream("default.json"));

        GraalsonSource template = new GraalsonSource("template1.js");
        GraalsonSource source = new GraalsonSource(jreader);
        GraalsonResult result = new GraalsonResult(jwriter);

        long then = System.nanoTime();
        int i = 0;
        int n = 1;
        for (i = 0; i < n; i++) {
            TransformerFactory.newInstance().newTemplates(template).newTransformer().transform(source, result);
        }

        System.out.println((System.nanoTime() - then) / n);
        assertNotNull(result.getValue());

        String expected = new Scanner(GraalsonTransformerTest.class.getResourceAsStream("/expected.json")).useDelimiter("\\Z").next();
        String actual = writer.getBuffer().toString();
        System.out.println("actual " + actual);

        JSONAssert.assertEquals(expected, actual, true);
    }

}
