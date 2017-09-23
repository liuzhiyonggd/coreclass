package sysu.coreclass.classify;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class Tool {

	public static void computePredict(List<Integer> commitIDList,List<Integer> classValues,List<Double> classifyResults) throws IOException{
		
		int commitNum = 0;
		int num = classValues.size();
		boolean flag = true;
		int commitRight = 0;
		int class1Right = 0;
		int class0Right = 0;
		int class1False = 0;
		int class0False = 0;
		
		int coreClassNum = 0;
		int coreClass0Num = 0;
		Map<Integer,Double> commitResultMap = new HashMap<Integer,Double>();
		Map<Integer,Integer> commitClassMap = new HashMap<Integer,Integer>();
		for (int i = 0; i < num - 1; i++) {
			
			if (commitIDList.get(i).intValue()==commitIDList.get(i+1).intValue()) {
				
				commitResultMap.put(i, classifyResults.get(i));
				commitClassMap.put(i, classValues.get(i));
				if(classValues.get(i)==1){
					coreClassNum ++;
				}

			} else {
				commitResultMap.put(i, classifyResults.get(i));
				commitClassMap.put(i, classValues.get(i));
				if(classValues.get(i)==1){
					coreClassNum ++;
				}
				commitNum++;
				commitResultMap = sortMapByValue(commitResultMap);
				
				int currentIndex = 0;
				for(Map.Entry<Integer, Double> entry:commitResultMap.entrySet()){
					if(currentIndex<coreClassNum){
						if(classValues.get(entry.getKey())==1){
							class1Right++;
						}else{
							class1False++;
							flag = false;
						}
					}else{
						if(classValues.get(entry.getKey())==0){
							class0Right++;
						}else{
							class0False++;
							flag = false;
						}
					}
					currentIndex++;
				}
				
				if (flag) {
					commitRight++;
				}
				flag = true;
				
				if(coreClassNum==0){
					coreClass0Num ++;
				}
				
				coreClassNum = 0;
				commitResultMap = new HashMap<Integer,Double>();
				commitClassMap = new HashMap<Integer,Integer>();
			}
		}
		
		DecimalFormat df = new DecimalFormat("#0.000");		
		System.out.println("commit num:"+commitNum);
		System.out.println("commit right:"+commitRight);
		System.out.println("commit precision:"+df.format(commitRight*1.0d/commitNum));
		System.out.println("class 1:"+(class1Right+class0False));
		System.out.println("class 0:"+(class0Right+class1False));
		System.out.println("class 1 right:"+class1Right);
		System.out.println("class 0 right:"+class0Right);
		System.out.println("class precision:"+df.format((class1Right+class0Right)*1.0d/(class1Right+class0Right+class1False+class0False)));
	}
	
	public static Map<Integer, Double> sortMapByValue(Map<Integer, Double> oriMap) {  
	    Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();  
	    if (oriMap != null && !oriMap.isEmpty()) {  
	        List<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(oriMap.entrySet());  
	        Collections.sort(entryList,  
	                new Comparator<Map.Entry<Integer, Double>>() {  
	                    public int compare(Entry<Integer, Double> entry1,  
	                            Entry<Integer, Double> entry2) {  
	                        double value1 = 0d, value2 = 0d;  
	                        try {  
	                            value1 = entry1.getValue();  
	                            value2 = entry2.getValue();  
	                        } catch (NumberFormatException e) {  
	                            value1 = 0;  
	                            value2 = 0;  
	                        }  
	                        if(value1<value2){
	                        	return 1;
	                        }else if(value1==value2){
	                        	return 0;
	                        }else{
	                        	return -1;
	                        }
	                    }  
	                });  
	        Iterator<Map.Entry<Integer, Double>> iter = entryList.iterator();  
	        Map.Entry<Integer, Double> tmpEntry = null;  
	        while (iter.hasNext()) {  
	            tmpEntry = iter.next();  
	            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
	        }  
	    }  
	    return sortedMap;  
	}  
	
	public static void main(String[] args) throws IOException {
		
	}
}
