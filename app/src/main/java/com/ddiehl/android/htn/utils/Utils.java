package com.ddiehl.android.htn.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import timber.log.Timber;

public class Utils {

    @NotNull
    public static String getStringFromInputStream(@NotNull InputStream i) {
        Scanner s = new Scanner(i).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Nullable
    public static String getTextFromFile(@NotNull File file) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                return sb.toString();
            } finally {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        } catch (FileNotFoundException e) {
            Timber.e("Unable to find file: %s", file.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    // http://www.mkyong.com/java/java-md5-hashing-example/
    @Nullable
    public static String getMd5HexString(@NotNull String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte byteData[] = md.digest();

            // Convert bytes to hex format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Timber.w("Unable to obtain MD5 hash");
            e.printStackTrace();
        }

        return null;
    }

    public static int getNumberOfDigits(int n) {
        if (n < 0) n *= -1;
        if (n == 0) return 0;
        return (int) Math.log10(n) + 1;
    }

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static <T, R> List<R> convert(List<T> list) {
        List<R> result = new ArrayList<>(list.size());
        for (T item : list) {
            //noinspection unchecked - that's the point
            result.add((R) item);
        }
        return result;
    }
}
