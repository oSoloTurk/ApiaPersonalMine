package me.osoloturk.personalmine.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import me.osoloturk.personalmine.APM;

public class CompressTools {
	
	public static String zip(final String str) {
		if((str == null) || (str.length() == 0)) {
			return "";
		}
		try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			try(GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
				gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
			}
			return toText(byteArrayOutputStream.toByteArray());
		} catch(IOException e) {
			APM.getInstance().getLogger().warning("Unable to compress your data please share the error with us");
			e.printStackTrace();
		}
		return "";
	}
	
	public static String unzip(final String compressedText) {
		if((compressedText == null) || (compressedText.isEmpty())) {
			return "";
		}
		byte[] compressedByteArray = toByteArray(compressedText);
		if(!isZipped(compressedByteArray))
			return compressedText;
		try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedByteArray)) {
			try(GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
				try(InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8)) {
					try(BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
						StringBuilder output = new StringBuilder();
						String line;
						while((line = bufferedReader.readLine()) != null) {
							output.append(line);
						}
						return output.toString();
					}
				}
			}
		} catch(IOException e) {
			APM.getInstance().getLogger().warning("Unable to compress your data please share the error with us");
			e.printStackTrace();
		}
		return "";
	}
	
	public static String toText(final byte[] compressedArray) {
		String text = "";
		for(int i = 0; i < compressedArray.length; i++) {
			text += compressedArray[i];
			if(i + 1 != compressedArray.length)
				text += ",";
		}
		return text;
	}
	
	public static byte[] toByteArray(final String compressedText) {
		String[] splitedCompresedText = compressedText.split(",");
		int length = Arrays.stream(splitedCompresedText).map(word -> word.length()).collect(Collectors.summingInt(Integer::intValue));
		byte[] compressed = new byte[length];
		int index = 0;
		for(int word = 0; word < splitedCompresedText.length; word++) {
			for(byte _byte : splitedCompresedText[word].getBytes()) {
				compressed[index++] = _byte;
			}
		}
		return compressed;
	}
	
	public static boolean isZipped(final byte[] compressed) {
		return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
	}
}
