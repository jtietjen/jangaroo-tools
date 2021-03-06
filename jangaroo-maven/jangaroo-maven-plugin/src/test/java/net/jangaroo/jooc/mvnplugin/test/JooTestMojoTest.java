package net.jangaroo.jooc.mvnplugin.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 */
public class JooTestMojoTest extends TestCase {
  JooTestMojo jooTestMojo;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    jooTestMojo = new JooTestMojo();
  }

  public void testSkip() throws MojoExecutionException, MojoFailureException {
    jooTestMojo.skip = true;
    // skip skips everything so no error expected
    jooTestMojo.execute();
  }

  public void testSkipTests() throws MojoExecutionException, MojoFailureException {
    jooTestMojo.skipTests = true;
    // skip skips everything so no error expected
    jooTestMojo.execute();
  }

  public void testNoTestsAvailable() throws MojoExecutionException, MojoFailureException, IOException {
    File f = File.createTempFile("JooTestMojoTest", "");
    Assert.assertTrue(f.delete());
    Assert.assertTrue(f.mkdirs());

    jooTestMojo.testSourceDirectory = f;
    jooTestMojo.testResources = new ArrayList<Resource>();
    jooTestMojo.execute();
    Assert.assertTrue(f.delete());
  }

  public void testEvalTestOutputSuccess() throws MojoExecutionException, MojoFailureException, IOException, SAXException, ParserConfigurationException {
    String testResult = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
            "<testsuite errors=\"0\" failures=\"0\" name=\"com.coremedia.ui.data::BeanImplTest\" tests=\"21\" time=\"2814\"></testsuite>";
    jooTestMojo.evalTestOutput(testResult);
  }

  public void testEvalTestOutputFailure() throws MojoExecutionException, MojoFailureException, IOException, SAXException, ParserConfigurationException {
    String testResult = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
            "<testsuite errors=\"0\" failures=\"1\" name=\"com.coremedia.ui.data::BeanImplTest\" tests=\"21\" time=\"2814\"></testsuite>";
    try {
      jooTestMojo.evalTestOutput(testResult);
    } catch (MojoFailureException e) {
      return;
    }
    fail("Should reach that point (testing for exception)");
  }

  public void testEvalTestOutputFailureIgnoreFail() throws MojoExecutionException, MojoFailureException, IOException, SAXException, ParserConfigurationException {
    String testResult = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
            "<testsuite errors=\"0\" failures=\"1\" name=\"com.coremedia.ui.data::BeanImplTest\" tests=\"21\" time=\"2814\"></testsuite>";
    try {
      jooTestMojo.testFailureIgnore = true;
      jooTestMojo.evalTestOutput(testResult);
    } catch (MojoFailureException e) {
      fail("Shouldn't fail since testFailureIgnore=true");
    }
  }


}
