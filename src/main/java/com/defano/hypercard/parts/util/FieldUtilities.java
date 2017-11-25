package com.defano.hypercard.parts.util;

import com.defano.hypertalk.utils.Range;

import javax.swing.text.*;
import java.util.Arrays;

public class FieldUtilities {

    /**
     * Given one or more line numbers (counting from 1), returns a character range that identifies the starting position
     * of the first line, and the ending position of the last line.
     *
     * @param component The text component whose contents should be ranged
     * @param lines     One or more lines to range.
     * @return The range of characters, or an empty range in the lines is null/empty.
     */
    public static Range getLinesRange(JTextComponent component, int... lines) {
        if (lines == null || lines.length == 0) {
            return new Range();
        }

        Arrays.sort(lines);
        int startLine = lines[0];
        int endLine = lines[lines.length - 1];

        return new Range(getLineRange(component, startLine).start, getLineRange(component, endLine).end);
    }

    /**
     * Given a line number (counting from 1), returns a character range that identifies the first character position
     * and the last.
     *
     * @param component The text component whose contents should be ranged
     * @param line      The line number to range, counting from 1
     * @return A range identifying the starting and ending character position of the line.
     */
    public static Range getLineRange(JTextComponent component, int line) {

        try {
            Element root = component.getDocument().getDefaultRootElement();
            int start = root.getElement(line - 1).getStartOffset();
            int end = Utilities.getRowEnd(component, start);

            return Range.inclusive(start, end);
        } catch (Exception e) {
            return new Range();
        }
    }

    public static Range getLinesRange(String text, int... lines) {
        if (lines == null || lines.length == 0) {
            return new Range();
        }

        Arrays.sort(lines);
        int startLine = lines[0];
        int endLine = lines[lines.length - 1];

        return new Range(getLineRange(text, startLine).start, getLineRange(text, endLine).end);
    }

    /**
     * Given a line number (counting from 1), returns a character range that identifies the first character position
     * and the last.
     *
     * @param text The text whose contents should be ranged
     * @param line The line number to range, counting from 1
     * @return A range identifying the starting and ending character position of the line.
     */
    public static Range getLineRange(String text, int line) {
        int thisLine = 1;
        int start = 0;

        for (int charIndex = 0; charIndex < text.length(); charIndex++) {

            if (text.charAt(charIndex) == '\n') {
                if (thisLine == line) {
                    return new Range(start, charIndex);
                } else {
                    thisLine++;
                    start = charIndex + 1;
                }
            }
        }

        return new Range(start, text.length());
    }

    /**
     * Given a character index and a string a text, returns the line number of the given character.
     *
     * @param charIndex The index of the character whose line number should be returned
     * @param text
     * @return The line number of the index, counting from 1. If the character index is greater than the length of the
     * text, returns the number of lines in the field.
     */
    public static int getLineOfChar(int charIndex, String text) {
        return text.substring(0, Math.min(text.length(), charIndex + 1)).split("\\n").length;
    }

    public static int getWrappedLineOfChar(JTextComponent comp, int charIdx) {
        int charCount = 0;
        int lineCount = 0;

        View document = comp.getUI().getRootView(comp).getView(0);

        if (document != null) {

            // Walk each paragraph in document
            for (int paragraphIdx = 0; paragraphIdx < document.getViewCount(); paragraphIdx++) {
                View paragraph = document.getView(paragraphIdx);

                // Walk each line in paragraph
                for (int lineIdx = 0; lineIdx < paragraph.getViewCount(); lineIdx++) {
                    View line = paragraph.getView(lineIdx);

                    // Walk each char in the line
                    charCount += line.getEndOffset() - line.getStartOffset();

                    if (charCount >= charIdx) {
                        return lineCount;
                    }

                    lineCount++;
                }
            }
        }

        return 0;
    }
}
