package be.intecbrussel.io_test.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class IOSort {
	/**
	 * The directory that needs to be sorted
	 */
	private static File directoryToSort;

	/**
	 * The directory with the sorted files
	 */
	private static File sortedDirectory;

	/**
	 * The file writer
	 */
	private static FileWriter summaryFileWriter;

	/**
	 * The line separator
	 */
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * The path separator
	 */
	private final static String PATH_SEPARATOR = File.separator;

	/**
	 * The length of the longest file name
	 */
	private static int longestFileNameLength;

	/**
	 * Sort the directory
	 * 
	 * @param directoryPath
	 */
	public static void sortDirectory(String directoryPath) {
		System.out.println("Get the directory to sort...");
		// set the directory to sort File
		directoryToSort = new File(directoryPath);
		System.out.println("Create the sorted directory...");
		setSortedDirectory();
		// Create the directories
		try {
			System.out.println("Creating the directories by extension name, and move the files...");
			createDirectoriesByFileExtensions(directoryToSort);
			System.out.println("Finished creating directories and moving files...");
			// Create the summary directory and file
			System.out.println("Creating the summary...");
			createSummary();
			System.out.println("Finished the summary...");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Finished");
		}
	}

	/**
	 * Set the sorted directory in the same directory as the unsorted directory
	 */
	private static void setSortedDirectory() {
		sortedDirectory = new File(
				directoryToSort.getParentFile().getPath().concat(PATH_SEPARATOR + "(" + directoryToSort.getName() + ") sorted_folder"));
		if (!sortedDirectory.exists()) {
			sortedDirectory.mkdir();
		}
	}

	/**
	 * Create the summary directory and the summary file.
	 */
	private static void createSummary() {
		// Create the summary directory
		File summaryDir = new File(sortedDirectory.getPath().concat("/summary"));
		if (!summaryDir.exists()) {
			summaryDir.mkdir();
		}
		// Create the summary file object
		File summaryFile = new File(summaryDir.getPath().concat("/summary.txt"));
		try {
			// If the file doesn't exists, create it.
			if (!summaryFile.exists()) {
				summaryFile.createNewFile();
			}
			// Create the file writer object
			summaryFileWriter = new FileWriter(summaryFile, false);
			// Set the header of the summary file
			summaryFileWriter.write("name" + " ".repeat(longestFileNameLength - 4) + "|   readable   |   writeable   |"
					+ LINE_SEPARATOR);
			setSummaryFileContents(sortedDirectory);
			summaryFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Set the contents of the summary file.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	private static void setSummaryFileContents(File directory) throws IOException {
		// there has to be files/directories in the given directory
		if (directory.listFiles() != null) {
			// Loop through all the files in the given directory
			for (File file : directory.listFiles()) {
				/*
				 * If the current file is a directory, append the directory name to the summary
				 * file and call this method again
				 */
				if (file.isDirectory() && file.list().length > 0) {
					// Add "-" for the same length as the name of the directory + 1 to cover the ":"
					summaryFileWriter.write(LINE_SEPARATOR + "-".repeat(file.getName().length()) + LINE_SEPARATOR);
					// Add the name of the directory
					summaryFileWriter.write(file.getName() + ":" + LINE_SEPARATOR);
					// Add "-" for the same length as the name of the directory + 1 to cover the ":"
					summaryFileWriter.write("-".repeat(file.getName().length() + 1) + LINE_SEPARATOR);
					// Call this method again
					setSummaryFileContents(file);
				} else {
					// Get the filename
					String filename = file.getName();
					// Get the position of the last occurring "."
					int pos = filename.lastIndexOf(".");
					if (pos > 0) {
						/*
						 * If the position of the last occurring "." is greater than 0, substract
						 * everything from that position in the filename.
						 */
						filename = filename.substring(0, pos);
					}
					/*
					 * Add the file to the summary file
					 */
					summaryFileWriter.write("-" + filename + " ".repeat(longestFileNameLength - (filename.length() + 1))
							+ "|      " + (file.canRead() ? "X" : "/") + "       |       "
							+ (file.canWrite() ? "X" : "/") + "       |" + LINE_SEPARATOR);
				}
			}
		}
	}

	/**
	 * Create the extension directories recursively
	 * 
	 * @param search
	 * @throws IOException
	 */
	private static void createDirectoriesByFileExtensions(File search) throws IOException {
		// If the given search is a directory
		if (search.isDirectory()) {
			// Loop through the directory and call this method again
			for (File file : search.listFiles()) {
				createDirectoriesByFileExtensions(file);
			}
		} else {
			/*
			 * If the given search is not a directory, we can asume that this is a file and
			 * move it to the sorted directory
			 */
			moveFile(search, createDirectory(search));
		}
	}

	/**
	 * Create the a new directory
	 * 
	 * @param file
	 * @return newDirectory
	 */
	private static File createDirectory(File file) {
		String extension;
		File newDirectory = null;

		// If the given file is not null and exists
		if (file != null && file.exists()) {
			// Get the filename
			String filename = file.getName();
			if (filename.length() > longestFileNameLength) {
				longestFileNameLength = filename.length();
			}

			/*
			 * Get the file extension of the given file, or if the file is a hidden file,
			 * set the extension as "hidden". This is to create the folders
			 */
			extension = file.isHidden() ? "hidden" : filename.substring(filename.lastIndexOf(".")).substring(1);
			// Create the new directory object
			newDirectory = new File(sortedDirectory.getPath().concat(PATH_SEPARATOR).concat(extension));

			// If the directory doesn't exists, create it
			if (!newDirectory.exists()) {
				newDirectory.mkdir();
			}
		}

		return newDirectory;
	}

	/**
	 * Move the file to another directory
	 * 
	 * @param file
	 * @param destination
	 * @throws IOException
	 */
	private static void moveFile(File file, File destination) throws IOException {
		// Get the path of the destination directory
		String destinationPath = destination.getPath();
		// If the file is a hidden file, set the destination path to the "hidden" directory
		if (file.isHidden()) {
			destinationPath = destination.getPath().substring(0, destination.getPath().lastIndexOf(PATH_SEPARATOR))
					.concat(PATH_SEPARATOR + "hidden");
		}
		// Create a new File object
		File newFile = new File(destinationPath.concat(PATH_SEPARATOR + file.getName()));
		newFile.createNewFile();
		// If the file doesn't exists in the destination directory, copy the file there
		if (!file.exists()) {
			Files.copy(file.toPath(), newFile.toPath());
		}
	}

}
