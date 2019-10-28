package edu.rpi.tw.twks.abc;

import java.util.Arrays;

public final class MoreFilenameUtils {
    private final static int[] fileNameIllegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    static {
        Arrays.sort(fileNameIllegalChars);
    }

    private MoreFilenameUtils() {
    }

    public final static String cleanFileName(final String badFileName) {
        final StringBuilder cleanName = new StringBuilder();
        final int len = badFileName.codePointCount(0, badFileName.length());
        for (int i = 0; i < len; i++) {
            final int c = badFileName.codePointAt(i);
            if (Arrays.binarySearch(fileNameIllegalChars, c) < 0) {
                cleanName.appendCodePoint(c);
            }
        }
        return cleanName.toString();
    }
}
