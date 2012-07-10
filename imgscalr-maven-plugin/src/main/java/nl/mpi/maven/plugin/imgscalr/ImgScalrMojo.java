package nl.mpi.maven.plugin.imgscalr;

import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

/**
 * Goal that scales a set of image files.
 * 
 * A simple wrapper around the scaling functionality of imgscalr, the Java Image Scaling Library
 * The configuration allows specification of a target width and/or height. If only one is provided, the image will be scaled
 * preserving the aspect ratio. The plugin will attempt to scale all files in the specified fileset, so be careful to only
 * include files of supported image formats. An output directory has to be specified as well.
 *
 * @see http://www.thebuzzmedia.com/software/imgscalr-java-image-scaling-library/ website of the imgscalr library
 * @goal scale
 *
 * @phase process-resources
 */
public class ImgScalrMojo extends AbstractMojo {

    /**
     * @parameter
     * @required
     */
    private FileSet files;
    /**
     * Location of the file.
     * @parameter
     * @required
     */
    private File outputDirectory;
    /**
     * @parameter
     */
    private int targetWidth = -1;
    /**
     * @parameter
     */
    private int targetHeight = -1;

    /**
     *
     * @goal scale
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
	assertOutputDirectory();

	final Mode resizeMode = getResizeMode();
	final List filesToScale = getFilesToScale();

	for (Object fileToScale : filesToScale) {
	    if (fileToScale instanceof File) {
		try {
		    scaleFile((File) fileToScale, resizeMode);
		} catch (IOException ioEx) {
		    throw new MojoExecutionException("IOException while trying to scale file", ioEx);
		}
	    } else {
		throw new MojoExecutionException("Encountered non-file in file list");
	    }
	}
    }

    private void assertOutputDirectory() throws MojoExecutionException {
	if (!outputDirectory.exists()) {
	    getLog().debug("Creating directory " + outputDirectory.toString());
	    outputDirectory.mkdirs();
	} else if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
	    throw new MojoExecutionException("Specified output directory already exists as file");
	}
    }

    protected void scaleFile(File file, Mode resizeMode) throws MojoExecutionException, IOException {
	final File outputFile = new File(outputDirectory, file.getName());

	getLog().debug("Reading file: " + file.toString());
	BufferedImage sourceImage = ImageIO.read(file);

	BufferedImage targetImage;
	if (shouldScale(resizeMode, sourceImage.getWidth(), sourceImage.getHeight())) {
	    getLog().info("Scaling image: " + file.toString());
	    targetImage = scaleImage(sourceImage, resizeMode);
	} else {
	    getLog().info("Not scaling image smaller than target: " + file.toString());
	    targetImage = sourceImage;
	}
	getLog().debug("Writing to file: " + outputFile.toString());
	boolean writeResult = ImageIO.write(targetImage, getExtension(file).toLowerCase(), outputFile);
	if (!writeResult) {
	    throw new MojoExecutionException("Failed to write scaled version of" + file.toString());
	}
    }

    private BufferedImage scaleImage(BufferedImage sourceImage, Mode resizeMode) throws IllegalArgumentException, ImagingOpException {
	// Library requires width and heigth > 0 (even if one of them gets ignored)
	final int width = (targetWidth > 0) ? targetWidth : 1;
	final int height = (targetHeight > 0) ? targetHeight : 1;

	return Scalr.resize(sourceImage, resizeMode, width, height);
    }

    protected boolean shouldScale(Mode resizeMode, int sourceWidth, int sourceHeight) {
	switch (resizeMode) {
	    case FIT_TO_HEIGHT:
		return sourceHeight > targetHeight;
	    case FIT_TO_WIDTH:
		return sourceWidth > targetWidth;
	    default:
		return sourceHeight != targetHeight || sourceWidth != targetWidth;
	}
    }

    protected String getExtension(File file) {
	String fileName = file.getName();
	int extensionIndex = fileName.lastIndexOf('.');
	if (extensionIndex >= 0 && extensionIndex + 1 < fileName.length()) {
	    return fileName.substring(extensionIndex + 1);
	} else {
	    return "";
	}
    }

    private Mode getResizeMode() throws MojoExecutionException {
	if (targetWidth < 0) {
	    if (targetHeight > 0) {
		return Mode.FIT_TO_HEIGHT;
	    } else {
		throw new MojoExecutionException("Width or height (or both) should be greater than 0");
	    }
	}

	if (targetHeight < 0) {
	    return Mode.FIT_TO_WIDTH;
	}
	return Mode.FIT_EXACT;
    }

    protected List getFilesToScale() throws MojoExecutionException {
	File inputDirectory = new File(files.getDirectory());
	String includes = implodeFileList(files.getIncludes());
	String excludes = implodeFileList(files.getExcludes());
	try {
	    return FileUtils.getFiles(inputDirectory, includes, excludes);
	} catch (IOException ex) {
	    throw new MojoExecutionException("IOException while reading input files", ex);
	}
    }

    private String implodeFileList(List files) throws MojoExecutionException {
	if (files.size() > 0) {
	    StringBuilder list = new StringBuilder(files.size() * 2 - 1);
	    for (int i = 0; i < files.size(); i++) {
		Object item = files.get(i);
		if (item instanceof String) {
		    list.append((String) item);
		} else if (item instanceof File) {
		    list.append(((File) item).getName());
		} else {
		    throw new MojoExecutionException("Cannot process include of type " + item.getClass().toString());
		}
		if (i < files.size() - 1) {
		    list.append(",");
		}
	    }
	    return list.toString();
	} else {
	    return null;
	}
    }

    /**
     * @param files the files to set
     */
    protected void setFiles(FileSet files) {
	this.files = files;
    }

    /**
     * @param outputDirectory the outputDirectory to set
     */
    protected void setOutputDirectory(File outputDirectory) {
	this.outputDirectory = outputDirectory;
    }

    /**
     * @param targetWidth the targetWidth to set
     */
    protected void setTargetWidth(int targetWidth) {
	this.targetWidth = targetWidth;
    }

    /**
     * @param targetHeight the targetHeight to set
     */
    protected void setTargetHeight(int targetHeight) {
	this.targetHeight = targetHeight;
    }
}
