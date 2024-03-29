/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
**********************************************************************/
package com.ibm.bear.qa.spot.wb.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to manipulate files and directories on disk through java.io
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #copyDir(File,File)}: Copy all files from source directory to destination directory.</li>
 * <li>{@link #copyFile(File,File)}: Copy the given source file to the given destination directory.</li>
 * <li>{@link #copyFile(File,File,String)}: Copy the given source file to the given destination directory with a different</li>
 * <li>{@link #createDir(String)}: Return the</li>
 * <li>{@link #createDir(String,String)}: Return the</li>
 * <li>{@link #deleteFile(File)}: Delete the given file.</li>
 * <li>{@link #findFile(String,File,FileFilter)}: Find files with the given name in the given directory hiearchy using given filter.</li>
 * <li>{@link #findFileInAscendantHierarchy(String,String,FileFilter)}: Find files with the given name in the project directory or its ascendant hierarchy using given filter.</li>
 * <li>{@link #getDir(String)}: Return a file built from given directory path.</li>
 * <li>{@link #getDir(String,String)}: Return a file built from given parent dir and path.</li>
 * <li>{@link #getFile(String,String)}: Return a file built from given dir and path.</li>
 * <li>{@link #getFile(URL)}: Get the file from the given URL.</li>
 * <li>{@link #getUrl(File)}: Get the URL from the given file.</li>
 * <li>{@link #readFileContent(File)}: Read the content of the given file, assuming it's a text file.</li>
 * <li>{@link #replaceFile(File,String)}: Overwrite the given file with the given text.</li>
 * <li>{@link #rmdir(File)}: Delete an entire directory hierarchy including all files.</li>
 * <li>{@link #waitUntilFileExists(File,int)}: Verify if a file exists at the given path. If the file doesn't exist yet,</li>
 * <li>{@link #waitUntilFileExists(String,int)}: Verify if a file exists at the given path. If the file doesn't exist yet,</li>
 * <li>{@link #writeFile(File,String)}: Write the given file with the given text.</li>
 * </ul>
 * </p>
 */
public class FileUtil {

	private static final int BUFFER_SIZE = 8192;

/**
 * Copy all files from source directory to destination directory.
 *
 * @param sourceDir The source directory
 * @param destDir The destination directory
 * @throws IOException
 */
public static void copyDir(final File sourceDir, final File destDir)  throws IOException {
	if (!destDir.mkdirs()) {
		throw new IOException("Could not create " + destDir.getCanonicalPath()); //$NON-NLS-1$
	}
	File[] sourceFiles = sourceDir.listFiles();
	if (sourceFiles == null) return;
	for (File sourceFile: sourceFiles) {
		if (sourceFile.isDirectory() ) {
			copyDir(sourceFile, new File(destDir, sourceFile.getName()));
		} else {
			copyFile(sourceFile, destDir);
		}
	}
}

/**
 * Copy the given source file to the given destination directory.
 *
 * @param sourceFile The file to copy
 * @param destDir The directory where to copy the file
 * @return The copied file as a {@link File}
 * @throws IOException
 */
public static File copyFile(final File sourceFile, final File destDir) throws IOException {
	return copyFile(sourceFile, destDir, null);
}

/**
 * Copy the given source file to the given destination directory with a different
 * name.
 *
 * @param sourceFile The file to copy
 * @param destDir The directory where to copy the file
 * @param destFile The new file name
 * @return The copied file as a {@link File}
 * @throws IOException
 */
public static File copyFile(final File sourceFile, final File destDir, final String destFile) throws IOException {
    try(InputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
	    String destFileName = destFile == null ? sourceFile.getName() : destFile;
    	return createFile(destDir, destFileName, inputStream);
    }
}

/**
 * Return the {@link File} corresponding to the given path.
 * <p>
 * If the directory does not exist, then it creates it.
 * </p>
 * @param dirPath The path of the directory
 * @return The {@link File} corresponding to the directory or <code>null</code>
 * if it didn't exist and that was not possible to create it.
 */
public static File createDir(final String dirPath) {
	File dir = new File(dirPath);
	if (!dir.exists() && !dir.mkdirs()) {
		System.err.println("Cannot create directory '"+dirPath+"'.");
		dir = null;
	}
	return dir;
}

/**
 * Return the {@link File} corresponding to the given path and the sub-directory.
 * <p>
 * If the directories do not exist, then it creates them.
 * </p>
 * @param dirPath The path of the directory
 * @param subdirName The sub-directory name
 * @return The {@link File} corresponding to the directory or <code>null</code>
 * if it didn't exist and that was not possible to create it.
 */
public static File createDir(final String dirPath, final String subdirName) {
	File dir = createDir(dirPath);
	if (dir != null) {
		File subdir = new File(dir, subdirName);
		if (subdir.exists() || subdir.mkdirs()) {
			return subdir;
		}
		System.err.println("Cannot create sub-directory '"+subdirName+"' in '"+dirPath+"'.");
	}
	return null;
}

private static File createFile(final File destDir, final String destFileName, final InputStream inputStream) throws FileNotFoundException, IOException {
	File destFile = new File(destDir, destFileName);
	try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile))) {
		read(inputStream, outputStream);
	}
	return destFile;
}

/**
 * Delete the given file.
 * <p>
 * Note that if the deletion fails, it's performed again until success or five
 * consecutive failures.
 * </p>
 * @param file The file to delete
 * @throws IOException
 * @throws InterruptedException
 */
public static void deleteFile(final File file) throws IOException, InterruptedException {
	for (int i = 1, maxAttempt = 5; i <= maxAttempt; i++) {
		if (file.delete()) break;
		if (i == maxAttempt) {
			throw new IOException("Could not delete " + file.getCanonicalPath()); //$NON-NLS-1$
		}
		System.gc();
		System.runFinalization();
		Thread.sleep(250);
	}
}

/**
 * Find files with the given name in the given directory hiearchy using given filter.
 *
 * @param name The file name to be found
 * @param dir The directory in which the search will occur
 * @return The list of found files which might be empty if none
 * was found in the given directory entire hierarchy
 */
public static List<File> findFile(final String name, final File dir, final FileFilter filter) {
	List<File> foundFiles = new ArrayList<>();
	findFile(name, dir, filter, foundFiles);
	return foundFiles;
}

private static void findFile(final String name, final File dir, final FileFilter filter, final List<File> foundFiles) {
	File[] files = filter == null ? dir.listFiles() : dir.listFiles(filter);
	if (files != null) {
		for (File file : files) {
			if (name.equals(file.getName())) {
				foundFiles.add(file);
			}
			if (file.isDirectory()) {
				findFile(name, file, filter, foundFiles);
			}
		}
	}
}

/**
 * Find files with the given name in the project directory or its ascendant hierarchy using given filter.
 *
 * @param name The file name to be found
 * @param endDir The directory name in which the search will end. If <code>null</code>
 * then the search will occur until the system directory root
 * @return The list of found files which might be empty if none
 * was found in the given directory ascendant hierarchy
 */
public static List<File> findFileInAscendantHierarchy(final String name, final String endDir, final FileFilter filter) {
	List<File> foundFiles = new ArrayList<>();
	File currentDir = new File(System.getProperty("user.dir"));
	System.out.println("		  -> property user.dir="+currentDir);
	while (currentDir != null) {
		File[] files = filter == null ? currentDir.listFiles() : currentDir.listFiles(filter);
		if (files != null) {
			for (File file : files) {
				if (name.equals(file.getName())) {
					foundFiles.add(file);
				}
			}
		}
		if (currentDir.getName().equals(endDir)) {
			break;
		}
		currentDir = currentDir.getParentFile();
	}
	return foundFiles;
}

/**
 * Return a file built from given directory path.
 *
 * @param dir The directory path. It can be relative or absolute.
 * When given path is relative and the corresponding file does not exist,
 * then the <code>user.dir</code> will be used to find the corresponding directory.
 * @return The file after having checked that it actually exists and
 * that it's actually a directory
 * @throws RuntimeException If the file cannot be found on the local file system
 * or if it's not a directory.
 */
public static File getDir(final String dir) {
	return getDir(null, dir);
}

/**
 * Return a file built from given parent dir and path.
 *
 * @param parent The path of the parent directory.
 * Note that it will be ignored if given path is absolute. If it's <code>null</code>
 * when given path is relative, then the <code>user.dir</code> will be used to find
 * the corresponding file in case it's not found in the current project directory.
 * @param dir The directory path. It can be relative or absolute.
 * When given path is relative and the corresponding file does not exist,
 * then the <code>user.dir</code> will be used to find the corresponding directory.
 * @return The file after having checked that it actually exists and
 * that it's actually a directory
 * @throws RuntimeException If the file cannot be found on the local file system
 * or if it's not a directory.
 */
public static File getDir(final String parent, final String dir) {
	File fileDir = getFile(parent, dir);
	if (!fileDir.isDirectory()) {
		final String message = "The directory '"+fileDir.getAbsolutePath()+"' is not a directory!";
		System.err.println(message);
		throw new RuntimeException(message);
	}
	return fileDir;
}

/**
 * Return a file built from given dir and path.
 *
 * @param fileDir The dir from which the file is supposed to be located.
 * Note that it will be ignored if given path is absolute. If it's <code>null</code>
 * when given path is relative, then the <code>user.dir</code> will be used to find
 * the corresponding file in case it's not found in the current project directory.
 * @param filePath The path of the file. It can be either relative to the given directory
 * (or to the directory pointed by the <code>user.dir</code> System property) or
 * absolute (then given directory will be ignored)
 * @return The file after having checked that it actually exists
 * @throws RuntimeException If the file cannot be found on the local file system
 */
public static File getFile(final String fileDir, final String filePath) {

	// Get the file from arguments
    File file;
    if (filePath == null) {
    	if (fileDir == null) {
    		throw new IllegalArgumentException("Cannot find a file with both dir and path null!");
    	}
    	file = new File(fileDir);
    } else {
    	file = new File(fileDir, filePath);
    }

    // Try to infer a better location if the file is not found in a first attempt
	if (!file.exists()) {
		System.out.println("		  -> File "+file+" does not exist:");
		if (fileDir == null) {
			if (!file.isAbsolute()) {
				// Maybe a relative path?
				String userDir = System.getProperty("user.dir");
				System.out.println("			=> no fileDir specified, try to search from user.dir directory instead ("+userDir+")...");
				file = new File(userDir, filePath);
			}
		} else {
			// Try to ignore fileDir
			System.out.println("			=> try to ignore fileDir parameter...");
			file = new File(filePath);
		}
	}

	// Return the found file...
	if (file.exists()) {
		return file;
	}

	// ... or raise an error if the file is not found
	final String message = "The file '"+file.getAbsolutePath()+"' has not been found!";
	System.err.println(message);
	throw new RuntimeException(message);
}

/**
 * Get the file from the given URL.
 *
 * @param fileUrl The file URL
 * @return The file
 */
public static File getFile(final URL fileUrl) {
	if (fileUrl == null) {
		throw new IllegalArgumentException("File URL cannot be null.");
	}
	String fileAbsolutePath = fileUrl.getPath();
	return new File(fileAbsolutePath);
}

/**
 * Get the URL from the given file.
 *
 * @param file The file
 * @return The file URL
 */
@SuppressWarnings("unused")
public static URL getUrl(final File file) {
	if (file == null) {
		throw new IllegalArgumentException("File cannot be null.");
	}
	try {
		return new URL("file://"+file.getAbsolutePath());
	} catch (MalformedURLException e) {
		return null;
	}
}

private static void read(final InputStream inputStream, final OutputStream outputStream) throws IOException {
	byte[] buffer = new byte[BUFFER_SIZE];
	int readSize = 0;
	while (true) {
		readSize = inputStream.read(buffer);
		if (readSize == -1) break;
		outputStream.write(buffer, 0, readSize);
	}
}

/**
 * Read the content of the given file, assuming it's a text file.
 *
 * @param file The file to read
 * @return The file content as a {@link String}.
 * @throws IOException
 */
public static String readFileContent(final File file) throws IOException {
    try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
    {
    	read(inputStream, outputStream);
    	byte[] bytes = outputStream.toByteArray();
    	outputStream.close();
    	return new String(bytes, "UTF-8");
   }
}

/**
 * Delete an entire directory hierarchy  including all files.
 *
 * @param dir The directory to delete
 * @throws IOException
 * @throws InterruptedException
 */
public static void rmdir(final File dir) throws IOException, InterruptedException {
	File[] files = dir.listFiles();
	if (files == null) return;
	for (File file: files) {
		if (file.isDirectory() ) {
			rmdir(file);
		} else {
			deleteFile(file);
		}
	}
	deleteFile(dir);
}

/**
 * Verify if a file exists at the given path. If the file doesn't exist yet,
 * wait. If not found within <b>timeout</b> seconds, returns false.
 *
 * @param filePath The path of the target file to verify for existence.
 * @return <b>true</b> if the given file exists, <b>false</b> otherwise.
 * @throws InterruptedException
 */
public static boolean waitUntilFileExists(final File filePath, final int timeout) throws InterruptedException {
	System.out.println("		+ waiting for the file " + filePath.getAbsolutePath() + " to exist");
	long timeoutMilliseconds = timeout * 1000 + System.currentTimeMillis();
	while (!filePath.exists()) {
		if (System.currentTimeMillis() > timeoutMilliseconds) {
			return false;
		}
		Thread.sleep(1000);
	}
	return true;
}

/**
 * Verify if a file exists at the given path. If the file doesn't exist yet,
 * wait. If not found within <b>timeout</b> seconds, returns false.
 *
 * @param path The absolute path of the target file to verify for existence.
 * @return <b>true</b> if the given file exists, <b>false</b> otherwise.
 * @throws InterruptedException
 */
public static boolean waitUntilFileExists(final String path, final int timeout) throws InterruptedException {
	return waitUntilFileExists(new File(path), timeout);
}

/**
 * Write the given file with the given text.
 *
 * @param destFile The file to be written
 * @param text The text to be written
 * @return The written file
 * @throws IOException
 */
public static File writeFile(final File destFile, final String text) throws FileNotFoundException, IOException {
	try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile))) {
		outputStream.write(text.getBytes());
	}
	return destFile;
}

/**
 * Overwrite the given file with the given text.
 *
 * @param destFile The file to be overwritten
 * @param text The text to be written
 * @return The written file
 * @throws IOException
 * @throws InterruptedException
 */
public static File replaceFile(final File destFile, final String text) throws FileNotFoundException, IOException, InterruptedException {
	if(destFile.exists()) {
		deleteFile(destFile);
	}
	return writeFile(destFile, text);
}
}
