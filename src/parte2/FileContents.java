package parte2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

public class FileContents implements Serializable{

	String filename;
	byte[] contents;
	
	public FileContents(File file) {
		this.filename = file.getName();
		try {
			this.contents = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFilename() {
		return filename;
	}

	public byte[] getContents() {
		return contents;
	}
	
	
}
