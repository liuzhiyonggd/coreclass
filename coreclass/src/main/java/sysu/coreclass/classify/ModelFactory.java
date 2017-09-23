package sysu.coreclass.classify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ModelFactory {
	
	
	public static RandomForestClassifier loadModel(String path) {
		File file = new File(path);
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		RandomForestClassifier classifier = null;
		try {
			fis = new FileInputStream(file); 
			ois = new ObjectInputStream(fis);
			try {
				classifier = (RandomForestClassifier) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(ois!=null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return classifier;
		    
	}

}
