package nl.mpi.maven.plugin.imgscalr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.maven.model.FileSet;
import org.imgscalr.Scalr.Mode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImgScalrMojoTest {

    private ImgScalrMojo instance;

    @Before
    public void setUp() {
	instance = new ImgScalrMojo();
    }

    /**
     * Test of scaleFile method, of class ImgScalrMojo.
     */
    @Test
    public void testScaleFileFixedWidth() throws Exception {
	final File outputDir = createTempDir();
	File image = getFileForResource("/images/image.png");

	instance.setOutputDirectory(outputDir);
	instance.setTargetWidth(5);
	instance.scaleFile(image, Mode.FIT_TO_WIDTH);

	File outputFile = new File(outputDir, "image.png");
	assertTrue(outputFile.exists());

	BufferedImage resultFile = ImageIO.read(outputFile);
	assertEquals(5, resultFile.getWidth());
    }

    /**
     * Test of scaleFile method, of class ImgScalrMojo.
     */
    @Test
    public void testScaleFileFixedHeight() throws Exception {
	final File outputDir = createTempDir();
	File image = getFileForResource("/images/image.gif");

	instance.setOutputDirectory(outputDir);
	instance.setTargetHeight(5);
	instance.scaleFile(image, Mode.FIT_TO_HEIGHT);

	File outputFile = new File(outputDir, "image.gif");
	assertTrue(outputFile.exists());

	BufferedImage resultFile = ImageIO.read(outputFile);
	assertEquals(5, resultFile.getHeight());
    }

    /**
     * Test of scaleFile method, of class ImgScalrMojo.
     */
    @Test
    public void testScaleFileExactFit() throws Exception {
	final File outputDir = createTempDir();
	File image = getFileForResource("/images/image.jpg");

	instance.setOutputDirectory(outputDir);
	instance.setTargetHeight(5);
	instance.setTargetWidth(5);
	instance.scaleFile(image, Mode.FIT_EXACT);

	File outputFile = new File(outputDir, "image.jpg");
	assertTrue(outputFile.exists());

	BufferedImage resultFile = ImageIO.read(outputFile);
	assertEquals(5, resultFile.getHeight());
	assertEquals(5, resultFile.getWidth());
    }

    /**
     * Test of scaleFile method, of class ImgScalrMojo.
     */
    @Test
    public void testScaleFileSmallerThanTarget() throws Exception {
	final File outputDir = createTempDir();
	File image = getFileForResource("/images/image.jpg");

	instance.setOutputDirectory(outputDir);
	instance.setTargetWidth(500);
	instance.scaleFile(image, Mode.FIT_TO_WIDTH);

	File outputFile = new File(outputDir, "image.jpg");
	assertTrue(outputFile.exists());

	BufferedImage originalFile = ImageIO.read(image);
	BufferedImage resultFile = ImageIO.read(outputFile);
	assertEquals(originalFile.getWidth(), resultFile.getWidth());
    }

    private File getFileForResource(String name) throws URISyntaxException {
	URI imageResource = getClass().getResource(name).toURI();
	File file = new File(imageResource);
	return file;
    }

    private File createTempDir() throws IOException {
	File outputDir = File.createTempFile("imgScalrMojoTest", "");
	if (!outputDir.delete()) {
	    throw new IOException("Could not delete temp file");
	}
	if (!outputDir.mkdir()) {
	    throw new IOException("Could not create temp dir");
	}
	return outputDir;
    }

    /**
     * Test of shouldScale method, of class ImgScalrMojo.
     */
    @Test
    public void testShouldScale() {
	instance.setTargetWidth(100);
	boolean result = instance.shouldScale(Mode.FIT_TO_WIDTH, 150, 0);
	assertTrue(result);
	result = instance.shouldScale(Mode.FIT_TO_WIDTH, 50, 0);
	assertFalse(result);

	instance.setTargetHeight(100);
	result = instance.shouldScale(Mode.FIT_TO_HEIGHT, 0, 150);
	assertTrue(result);
	result = instance.shouldScale(Mode.FIT_TO_WIDTH, 0, 50);
	assertFalse(result);

	result = instance.shouldScale(Mode.FIT_EXACT, 150, 150);
	assertTrue(result);
	result = instance.shouldScale(Mode.FIT_EXACT, 50, 150);
	assertTrue(result);
	result = instance.shouldScale(Mode.FIT_EXACT, 150, 50);
	assertTrue(result);
	result = instance.shouldScale(Mode.FIT_EXACT, 99, 99);
	assertTrue(result);
	result = instance.shouldScale(Mode.FIT_EXACT, 100, 100);
	assertFalse(result);
    }

    /**
     * Test of getExtension method, of class ImgScalrMojo.
     */
    @Test
    public void testGetExtension() {
	File file = new File("test.jpg");
	String result = instance.getExtension(file);
	assertEquals("jpg", result);

	file = new File("test.jpg.png");
	result = instance.getExtension(file);
	assertEquals("png", result);

	file = new File("test.");
	result = instance.getExtension(file);
	assertEquals("", result);

	file = new File("test");
	result = instance.getExtension(file);
	assertEquals("", result);
    }

    /**
     * Test of getFilesToScale method, of class ImgScalrMojo.
     */
    @Test
    public void testGetFilesToScale() throws Exception {
	final FileSet fileSet = createFileSet();
	instance.setFiles(fileSet);

	List filesToScale = instance.getFilesToScale();
	assertEquals(3, filesToScale.size());
    }

    /**
     * Test of getFilesToScale method, of class ImgScalrMojo.
     */
    @Test
    public void testGetFilesToScaleIncludes() throws Exception {
	final FileSet fileSet = createFileSet();

	fileSet.addInclude("image.png");
	List filesToScale = instance.getFilesToScale();
	assertEquals(1, filesToScale.size());

	fileSet.addInclude("image.gif");
	filesToScale = instance.getFilesToScale();
	assertEquals(2, filesToScale.size());
    }

    /**
     * Test of getFilesToScale method, of class ImgScalrMojo.
     */
    @Test
    public void testGetFilesToScaleExcludes() throws Exception {
	final FileSet fileSet = createFileSet();
	instance.setFiles(fileSet);

	fileSet.addExclude("image.png");
	List filesToScale = instance.getFilesToScale();
	assertEquals(2, filesToScale.size());

	fileSet.addExclude("image.gif");
	filesToScale = instance.getFilesToScale();
	assertEquals(1, filesToScale.size());
    }

    /**
     * Test of getFilesToScale method, of class ImgScalrMojo.
     */
    @Test
    public void testGetFilesToScaleIncludeWildcards() throws Exception {
	final FileSet fileSet = createFileSet();

	fileSet.addInclude("*.png");
	List filesToScale = instance.getFilesToScale();
	assertEquals(1, filesToScale.size());
	
	fileSet.addInclude("*.png");
	filesToScale = instance.getFilesToScale();
	assertEquals(1, filesToScale.size());
	
	fileSet.addInclude("*.jpg");
	filesToScale = instance.getFilesToScale();
	assertEquals(2, filesToScale.size());

	fileSet.addInclude("*.bmp");
	filesToScale = instance.getFilesToScale();
	assertEquals(2, filesToScale.size());
    }
    
    

    /**
     * Test of getFilesToScale method, of class ImgScalrMojo.
     */
    @Test
    public void testGetFilesToScaleIncludeRegex() throws Exception {
	final FileSet fileSet = createFileSet();

	fileSet.addInclude("%regex[(?i).*\\.jpg$]");
	List filesToScale = instance.getFilesToScale();
	assertEquals(1, filesToScale.size());
	
	fileSet.addInclude("%regex[(?i).*\\.PNG$]");
	filesToScale = instance.getFilesToScale();
	assertEquals(2, filesToScale.size());
	
	
	fileSet.addInclude("%regex[image.*$]");
	filesToScale = instance.getFilesToScale();
	assertEquals(3, filesToScale.size());
    }

    private FileSet createFileSet() throws URISyntaxException {
	final FileSet fileSet = new FileSet();
	fileSet.setDirectory(getFileForResource("/images").getAbsolutePath());
	instance.setFiles(fileSet);
	return fileSet;
    }
}
