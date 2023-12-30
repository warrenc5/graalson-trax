package au.com.devnull.graalson;

import au.com.devnull.graalson.trax.GraalsonResult;
import au.com.devnull.graalson.trax.GraalsonSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
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
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author wozza
 */
public class WireMockHarTransformer {

    @Test
    public void testTransformer() throws TransformerConfigurationException, TransformerException, IOException, JSONException {

        System.setProperty("javax.xml.transform.TransformerFactory", "au.com.devnull.graalson.trax.GraalsonTransformerFactory");

        Map<String, Object> config = new HashMap<>();
        config.put("spaces", Integer.valueOf(4));

        StringWriter writer = new StringWriter();
        JsonWriterFactory wfactory = Json.createWriterFactory(config);
        //JsonWriter jwriter = wfactory.createWriter(new PrintWriter(System.out));
        JsonWriter jwriter = wfactory.createWriter(writer);

        JsonReader jreader = Json.createReader(ClassLoader.getSystemClassLoader().getResourceAsStream("wiremock_requests.json"));

        Source template = new GraalsonSource("wiremock-har-template.js");
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

        String actual = writer.getBuffer().toString();
        try (FileWriter fileWriter = new FileWriter(new File("./target/wiremock-har.json"))) {
            fileWriter.write(writer.getBuffer().toString());
        }
        System.out.println("actual " + actual);
    }
}
