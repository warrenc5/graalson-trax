package au.com.devnull.graalson.trax;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import au.com.devnull.graalson.trax.GraalsonTransformerFactory.JsonMode;
import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * Unit tests for the four structured operations in GraalsonTransformer:
 * JSON_DIFF, JSON_MERGE, JSON_PATCH_DIFF, and JSON_PATCH_APPLY.
 *
 * <ul>
 *   <li>JSON_DIFF produces an RFC 7396 merge patch; JSON_MERGE applies it.</li>
 *   <li>JSON_PATCH_DIFF produces an RFC 6902 stepwise patch; JSON_PATCH_APPLY applies it.</li>
 * </ul>
 *
 * Test data layout:
 * <ul>
 *   <li>{@code operations/diff/} – files for JSON_PATCH_DIFF &amp; JSON_PATCH_APPLY (RFC 6902)</li>
 *   <li>{@code operations/merge/} – files for JSON_DIFF &amp; JSON_MERGE (RFC 7396)</li>
 * </ul>
 *
 * @author wozza
 */
public class GraalsonTransformerOperationTest {

    @BeforeAll
    static void setup() {
        GraalsonTransformerFactory.useJavaxXmlTransformTransformerFactory();
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private String transform(JsonMode mode, String operand1Resource, String operand2Resource) throws Exception {
        Source operand1 = new GraalsonSource(operand1Resource);
        Source operand2 = new GraalsonSource(operand2Resource);
        return doTransform(mode, operand1, operand2);
    }

    private String transform(JsonMode mode, JsonStructure operand1, JsonStructure operand2) throws Exception {
        Source source1 = new GraalsonSource(operand1);
        Source source2 = new GraalsonSource(operand2);
        return doTransform(mode, source1, source2);
    }

    private String doTransform(JsonMode mode, Source operand1, Source operand2) throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriterFactory wfactory = Json.createWriterFactory(Collections.emptyMap());
        JsonWriter jwriter = wfactory.createWriter(writer);
        Result result = new GraalsonResult(jwriter);

        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(GraalsonTransformerFactory.JSON_MODE_ATTRIBUTE, mode);
        factory.newTemplates(operand1).newTransformer().transform(operand2, result);

        return writer.toString();
    }

    private JsonStructure readResource(String path) {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        assertNotNull(is, "Resource not found: " + path);
        try (var reader = Json.createReader(is)) {
            return reader.read();
        }
    }

    private String readResourceAsString(String path) {
        return readResource(path).toString();
    }

    // ── JSON_DIFF (RFC 7396 – compute merge patch) ──────────────────────

    @Test
    void testDiff() throws Exception, JSONException {
        String output = transform(
            JsonMode.JSON_DIFF,
            "operations/merge/merge_orig.json",
            "operations/merge/merge_result.json"
        );

        JSONAssert.assertEquals(
            readResourceAsString("operations/merge/merge_patch.json"),
            output,
            JSONCompareMode.STRICT_ORDER
        );
    }

    // ── JSON_MERGE (RFC 7396 – apply merge patch) ───────────────────────

    @Test
    void testMerge() throws Exception, JSONException {
        String output = transform(
            JsonMode.JSON_MERGE,
            "operations/merge/merge_patch.json",
            "operations/merge/merge_orig.json"
        );

        JSONAssert.assertEquals(
            readResourceAsString("operations/merge/merge_result.json"),
            output,
            JSONCompareMode.STRICT_ORDER
        );
    }

    // ── JSON_PATCH_DIFF (RFC 6902 – compute stepwise patch) ─────────────

    @Test
    void testPatchDiff() throws Exception, JSONException {
        String output = transform(
            JsonMode.JSON_PATCH_DIFF,
            "operations/patch/default.json",
            "operations/patch/default_1.json"
        );

        JSONAssert.assertEquals(
            readResourceAsString("operations/patch/patch_diff.json"),
            output,
            JSONCompareMode.STRICT_ORDER
        );
    }

    // ── JSON_PATCH_APPLY (RFC 6902 – apply stepwise patch) ──────────────

    @Test
    void testPatchApply() throws Exception, JSONException {
        String output = transform(
            JsonMode.JSON_PATCH_APPLY,
            "operations/patch/patch_diff.json",
            "operations/patch/default.json"
        );

        JSONAssert.assertEquals(
            readResourceAsString("operations/patch/default_1.json"),
            output,
            JSONCompareMode.STRICT_ORDER
        );
    }

    // ── round-trip: JSON_DIFF → JSON_MERGE ──────────────────────────────

    @Test
    void testDiffThenMergeRoundTrip() throws Exception, JSONException {
        JsonStructure original = readResource("operations/merge/merge_orig.json");
        JsonStructure expected = readResource("operations/merge/merge_result.json");

        // Step 1 – compute the merge patch from orig → result
        String diffOutput = transform(JsonMode.JSON_DIFF, original, expected);
        JsonStructure mergePatch = Json.createReader(new StringReader(diffOutput)).read();

        // Step 2 – apply that merge patch back to the original
        String mergeOutput = transform(JsonMode.JSON_MERGE, mergePatch, original);

        JSONAssert.assertEquals(expected.toString(), mergeOutput, JSONCompareMode.STRICT_ORDER);
    }

    // ── round-trip: JSON_PATCH_DIFF → JSON_PATCH_APPLY ──────────────────

    @Test
    void testPatchDiffThenApplyRoundTrip() throws Exception, JSONException {
        JsonStructure original = readResource("operations/patch/default.json");
        JsonStructure expected = readResource("operations/patch/default_1.json");

        // Step 1 – compute the RFC 6902 patch from original → expected
        String patchOutput = transform(JsonMode.JSON_PATCH_DIFF, original, expected);
        JsonStructure patch = Json.createReader(new StringReader(patchOutput)).read();

        // Step 2 – apply that patch back to the original
        String applyOutput = transform(JsonMode.JSON_PATCH_APPLY, patch, original);

        JSONAssert.assertEquals(expected.toString(), applyOutput, JSONCompareMode.STRICT_ORDER);
    }
}
