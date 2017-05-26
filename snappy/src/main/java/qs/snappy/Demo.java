package qs.snappy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyInputStream;

public class Demo {
	
	public static <T> List<T> newList() {
		return new ArrayList<T>();
	}

	public static void main(String[] args) throws IOException {
		List<String> list = newList();
		RandomAccessFile raf = new RandomAccessFile("snappy.out", "r");
		ArchiveInputStream ais = new ArchiveInputStream(raf, raf.length());
		
		FileOutputStream fos = new FileOutputStream(new File("snappy.uncompressed"));
		
		SnappyInputStream sis = new SnappyInputStream(ais);
		
		byte[] buffer = new byte[1*1024];
		
        int readCount = 0;
        while((readCount = sis.read(buffer, 0, 1*1024)) != -1) {
        	fos.write(buffer, 0, readCount);
        }
		
        sis.close();
        fos.close();
	}

}
