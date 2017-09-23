package sysu.coreclass.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import sysu.coreclass.classify.ModelFactory;
import sysu.coreclass.classify.RandomForestClassifier;

public class TestClassifier {
	private RandomForestClassifier classifier;
	
	@Before
	public void before() {
		classifier = ModelFactory.loadModel("model.dat");
	}
	
	@Test
	public void test() {
        assertNotNull(classifier);
        try {
			classifier.getPrecision();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
