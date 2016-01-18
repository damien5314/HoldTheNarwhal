package com.ddiehl.android.htn.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.HoldTheNarwhal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.UUID;

public class Utils {

  @NonNull
  public static String getStringFromInputStream(@NonNull InputStream i) {
    Scanner s = new Scanner(i).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @Nullable
  public static String getTextFromFile(@NonNull File file) throws IOException {
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
        br.close();
      }
    } catch (FileNotFoundException e) {
      HoldTheNarwhal.getLogger().e("Unable to find file: " + file.getAbsolutePath());
      e.printStackTrace();
    }
    return null;
  }

  @NonNull
  public static String getRandomString() {
    return UUID.randomUUID().toString();
  }

  // http://www.mkyong.com/java/java-md5-hashing-example/
  @Nullable
  public static String getMd5HexString(@NonNull String s) {
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
      HoldTheNarwhal.getLogger().w("Unable to obtain MD5 hash");
      e.printStackTrace();
    }

    return null;
  }

  public static int getNumberOfDigits(int n) {
    if (n < 0) n *= -1;
    if (n == 0) return 0;
    return (int) Math.log10(n) + 1;
  }
}
