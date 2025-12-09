package au.com.devnull.graalson;

import au.com.devnull.graalson.trax.GraalsonResult;
import au.com.devnull.graalson.trax.GraalsonSource;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.json.JSONException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

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

        Source template = new GraalsonSource("template1.js");
        Source source = new GraalsonSource(jreader);
        Result result = new GraalsonResult(jwriter);

        long then = System.nanoTime();
        int i = 0;
        int n = 1;
        
        for (i = 0; i < n; i++) {
            TransformerFactory.newInstance().newTemplates(template).newTransformer().transform(source, result);
        }

        System.out.println((System.nanoTime() - then) / n);
        assertNotNull(((GraalsonResult) result).getValue());

        String expected = new Scanner(GraalsonTransformerTest.class.getResourceAsStream("/expected.json")).useDelimiter("\\Z").next();
        String actual = writer.getBuffer().toString();
        System.out.println("actual -->" + actual +"<--");

        assertEquals(expected.length(), actual.length());
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT_ORDER);
    }

}
