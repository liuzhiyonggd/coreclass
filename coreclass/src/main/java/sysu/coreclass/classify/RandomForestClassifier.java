package sysu.coreclass.classify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class RandomForestClassifier implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private RandomForest classifier;
	private Instances instancesTrain;
	private Instances instancesTest;
	
	private List<Integer> testCommitIDList = new ArrayList<Integer>();
    private List<Integer> testClassIndexList = new ArrayList<Integer>();
	
	public RandomForestClassifier(String trainPath,String testPath) {
		classifier = new RandomForest();
		File trainFile = new File(trainPath);
		ArffLoader atf = new ArffLoader();
		
		File testFile = new File(testPath);
		ArffLoader atf2 = new ArffLoader();

		try {
			atf.setFile(trainFile);
			instancesTrain = atf.getDataSet();
			instancesTrain.deleteAttributeAt(0);
			instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);
			
			atf2.setFile(testFile);
			instancesTest = atf2.getDataSet();
			for(int i=0;i<instancesTest.numInstances();i++){
				Instance ins = instancesTest.get(i);
				testCommitIDList.add((int)ins.value(0));
				testClassIndexList.add((int)ins.value(instancesTest.numAttributes()-1));
			}
			instancesTest.deleteAttributeAt(0);
			instancesTest.setClassIndex(instancesTest.numAttributes()-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ArffLoader setFile is failed. by RamdomForest.");
		}
		init();
		train();
	}

	private void init() {
//		classifier.setBagSizePercent(30);
//		classifier.setNumFeatures(6);
		classifier.setNumIterations(300);
	}

	private void train() {
		try {
			classifier.buildClassifier(instancesTrain);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("build classifier error.");
		}
	}

	public List<Double> classify(String classifyPath) throws Exception {
		
		List<Double> result = new ArrayList<Double>();
		
		File classifyFile = new File(classifyPath);
		ArffLoader atf = new ArffLoader();
		atf.setFile(classifyFile);
		Instances classifyInstances = atf.getDataSet();
		int num = classifyInstances.numInstances();
		for (int i = 0; i < num; i++) {
			Double d = classifier.classifyInstance(classifyInstances.instance(i));
			result.add(d);
		}
		
		return result;

	}

	public void getPrecision() throws Exception {
		List<Double> result = new ArrayList<Double>();

		ArffLoader atf = new ArffLoader();

			int num = instancesTest.numInstances();
			for (int i = 0; i < num; i++) {
				Double d = classifier.classifyInstance(instancesTest.instance(i));
				result.add(d);
			}
			
        Tool.computePredict(testCommitIDList, testClassIndexList, result);
	}
	
	public void saveModel() {
		File file = new File("model.dat");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file );
			oos = new ObjectOutputStream(fos);  
			oos.writeObject(this);
			 oos.flush();  
			    
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(oos!=null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
		
	public static void main(String[] args) throws Exception {
		
		RandomForestClassifier rs = new RandomForestClassifier("/home/angel/work/data/train_300.arff","/home/angel/work/data/test_300.arff");
		
		rs.getPrecision();
		rs.saveModel();

	}

}
