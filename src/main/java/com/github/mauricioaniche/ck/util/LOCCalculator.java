package com.github.mauricioaniche.ck.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LOCCalculator {

	private static Logger log = Logger.getLogger(LOCCalculator.class);
	
	public static int calculate(String sourceCode) {
		try {
			InputStream is = IOUtils.toInputStream(sourceCode);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			return getNumberOfLines(reader);
		} catch (IOException e) {
			log.error("Error when counting lines", e);
			return 0;
		}
	}



	// Code extracted from https://gist.github.com/shiva27/1432290

	/**
	 * This class  counts the number of source code lines by excluding comments, in a Java file
	 * The pseudocode is as below
	 *
	 * Initial: Set count = 0, commentBegan = false
	 * Start: Read line
	 * Begin: If line is not null, goto Check, else goto End
	 * Check: If line is a trivial line(after trimming, either begins with // or is ""), goto Start
	 *        If commentBegan = true
	 *             if comment has not ended in line
	 *                goto Start
	 *              else
	 *                line = what remains in the line after comment ends
	 *                commenBegan = false
	 *                if line is trivial
	 *                   goto Start
	 * 		  If line is a valid source code line, count++
	 *        If comment has begun in the line, set commentBegan = true
	 *        goto Start
	 * End: print count
	 */
	private static int getNumberOfLines(BufferedReader bReader)
			throws IOException {
		int count = 0;
		boolean commentBegan = false;
		String line = null;

		while ((line = bReader.readLine()) != null) {
			line = line.trim();
			if (isTrivialLine(line)) {
				continue;
			}

			if (commentBegan && !commentEnded(line)) {
				continue;
			}

			if (commentBegan) {
				line = line.substring(line.indexOf("*/") + 2).trim();
				commentBegan = false;
			}

			if (isTrivialLine(line)) {
				continue;
			}

			if (isSourceCodeLine(line)) {
				count++;
			}

			if (commentBegan(line)) {
				commentBegan = true;
			}
		}

		return count;
	}

	private static boolean isTrivialLine(String line) {
    	return ("".equals(line) || line.startsWith("//"));
	}

	/**
	 *
	 * @param line
	 * @return This method checks if in the given line a comment has begun and has not ended
	 */
	private static boolean commentBegan(String line) {
		// If line = /* */, this method will return false
		// If line = /* */ /*, this method will return true
		int index = line.indexOf("/*");
		if (index < 0) {
			return false;
		}
		int quoteStartIndex = line.indexOf("\"");
		if (quoteStartIndex != -1 && quoteStartIndex < index) {
			while (quoteStartIndex > -1) {
				line = line.substring(quoteStartIndex + 1);
				int quoteEndIndex = line.indexOf("\"");
				line = line.substring(quoteEndIndex + 1);
				quoteStartIndex = line.indexOf("\"");
			}
			return commentBegan(line);
		}
		return !commentEnded(line.substring(index + 2));
	}

	/**
	 *
	 * @param line
	 * @return This method checks if in the given line a comment has ended and no new comment has not begun
	 */
	private static boolean commentEnded(String line) {
		// If line = */ /* , this method will return false
		// If line = */ /* */, this method will return true
		int index = line.indexOf("*/");
		if (index < 0) {
			return false;
		} else {
			String subString = line.substring(index + 2).trim();
			if ("".equals(subString) || subString.startsWith("//")) {
				return true;
			}
			if(commentBegan(subString))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}

	/**
	 *
	 * @param line
	 * @return This method returns true if there is any valid source code in the given input line. It does not worry if comment has begun or not.
	 * This method will work only if we are sure that comment has not already begun previously. Hence, this method should be called only after {@link #commentBegan(String)} is called
	 */
	private static boolean isSourceCodeLine(String line) {
		line = line.trim();

		if (isTrivialLine(line)) {
			return false;
		}

		if (line.length() == 1) {
			return true;
		}

		int index = line.indexOf("/*");
		if (index != 0) {
			return true;
		}

		while (line.length() > 0) {
			line = line.substring(index + 2);
			int endCommentPosition = line.indexOf("*/");

			if (endCommentPosition < 0) {
				return false;
			}

			if (endCommentPosition == line.length() - 2) {
				return false;
			} 

			String subString = line.substring(endCommentPosition + 2).trim();
			
			if (isTrivialLine(subString)) {
				return false;
			}

			if (subString.startsWith("/*")) {
				line = subString;
				continue;
			}

			return true;
		}

		return true;
	}
}
