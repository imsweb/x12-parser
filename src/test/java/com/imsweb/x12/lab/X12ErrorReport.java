package com.imsweb.x12.lab;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.imsweb.x12.reader.X12Reader;

public class X12ErrorReport {

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws IOException {
        ZipFile zip = new ZipFile(new File("P:\\csb\\seerdms\\samples\\CLAIMS DATA - UNLIMITED\\5010 Files.zip"));
        int counter = 0;

        Set<String> filesWithErrors = new HashSet<>();
        final List<String> errorList = new ArrayList<>();
        final int[] errorCounter = new int[100];

        for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
            counter++;
            ZipEntry entry = (ZipEntry)e.nextElement();

            InputStream is = zip.getInputStream(entry);

            X12Reader reader = new X12Reader(X12Reader.FileType.ANSI837_5010_X222, is);

            List<String> errors = new ArrayList<>();

            reader.getErrors().stream().filter(s -> !errors.contains(s)).forEach(errors::add);

            if (reader.getErrors().size() != 0) {

                System.out.print("File: " + entry.getName() + " -----  ");
                for (int i = 0; i < errors.size(); i++) {
                    System.out.print(errors.get(i));
                    if (i != errors.size() - 1)
                        System.out.print(", ");

                    if (errorList.contains(errors.get(i)))
                        errorCounter[errorList.indexOf(errors.get(i))]++;
                    else {
                        errorList.add(errors.get(i));
                        errorCounter[errorList.indexOf(errors.get(i))]++;
                    }

                }
                System.out.println();
                filesWithErrors.add(entry.getName());

            }
        }

        List<String> sortedErrorsByCount = new ArrayList<>();
        List<Integer> sortedCounts = new ArrayList<>();
        for (int c : errorCounter)
            if (c != 0)
                sortedCounts.add(c);
        Collections.sort(sortedCounts);
        int previousCount = 0;
        for (int i = 0; i < sortedCounts.size(); i++) {
            if (previousCount == sortedCounts.get(sortedCounts.size() - i - 1))
                continue;
            else
                previousCount = sortedCounts.get(sortedCounts.size() - i - 1);

            for (int j = 0; j < errorList.size(); j++) {
                if (errorCounter[j] == sortedCounts.get(sortedCounts.size() - i - 1)) {
                    sortedErrorsByCount.add(errorList.get(j));
                }

            }

        }

        System.out.println();
        System.out.println("Overall Summary");
        System.out.println();
        float percentage = ((float)filesWithErrors.size()) / counter * 100;
        System.out.println(String.format("%.2f", percentage) + "% (" + filesWithErrors.size() + " out of " + counter + ") of files have errors.");

        System.out.println();
        System.out.println("Individual Error Summary");
        System.out.println();
        System.out.println("Count   Error");
        System.out.println("-----   -----");
        for (int i = 0; i < sortedErrorsByCount.size(); i++)
            System.out.println(sortedCounts.get(sortedErrorsByCount.size() - i - 1) + "       " + sortedErrorsByCount.get(i));
    }
}
