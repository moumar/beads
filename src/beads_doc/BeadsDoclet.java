import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class BeadsDoclet {
	public static boolean start(RootDoc root) {
		String tagName = "category";
		String outputDir = getDir(root.options());
		writeContents(outputDir, root.classes(), tagName);
		return true;
	}

	private static void writeContents(String outputDir, ClassDoc[] classes, String tagName) {
		try {
			String filename = outputDir + "/overview.html";
			System.out.println("Writing overview to \"" + filename + "\"");			
			PrintWriter file = new PrintWriter(filename);
			file.println("Hello");
			
			for (int i = 0; i < classes.length; i++) {
				Tag[] tags = classes[i].tags(tagName);
				if (tags.length > 0) {
					file.print(classes[i].name() + ":");
					for (Tag t : tags) {
						file.print("\"" + t.text() + "\" ");
					}
					file.println();
				}
			}
			file.close();
			
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			}
	}

	private static String getDir(String[][] options) {
		String dir = null;
		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i];
			if (opt[0].equals("-d")) {
				dir = opt[1];
			}
		}
		return dir;
	}

	public static int optionLength(String option) {
		if (option.equals("-d")) {
			return 2;
		}
		return 0;
	}

	public static boolean validOptions(String options[][],
			DocErrorReporter reporter) {
		boolean foundDirOption = false;
		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i];
			if (opt[0].equals("-d")) {
				if (foundDirOption) {
					reporter.printError("Only one -dg option allowed.");
					return false;
				} else {
					foundDirOption = true;
				}
			}
		}
		if (!foundDirOption) {
			reporter
					.printError("Usage: javadoc -d output dir -doclet ListTags ...");
		}
		return foundDirOption;
	}

}
