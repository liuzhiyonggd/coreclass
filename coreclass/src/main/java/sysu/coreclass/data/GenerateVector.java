package sysu.coreclass.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import sysu.coreclass.database.tool.ConnectionFactory;

public class GenerateVector {
	public static void generate(String path) throws IOException {
		List<List<Integer>> vectorList = new ArrayList<List<Integer>>();
		List<String> classList = new ArrayList<String>();
		List<List<Integer>> class0List = new ArrayList<List<Integer>>();
		List<List<Integer>> class1List = new ArrayList<List<Integer>>();

		MongoDatabase db = ConnectionFactory.getDB();
		MongoCollection<Document> commits = db.getCollection("commit2");

		String[] type1 = { "implement", "add", "request", "new", "test", "start", "includ", "initial", "introduc",
				"creat", "increas" };
		String[] type2 = { "optimiz", "ajdust", "updat", "delet", "remov", "chang", "refactor", "replac", "modif",
				"now", "enhanc", "improv", "design", "rename", "eliminat", "duplicat", "restrutur", "simplif",
				"obsolet", "rearrang", "miss", "enhance", "improv" };
		String[] type3 = { "bug", "issue", "error", "correct", "proper", "deprecat", "broke" };
		String[] type4 = { "clean", "licens", "merge", "release", "structure", "integrat", "organiz", "copyright",
				"documentation", "manual", "javadoc", "comment", "TODO", "migrate", "repository", "code", "review",
				"polish", "upgrade", "style", "formatting" };

		BasicDBObject commitQuery = new BasicDBObject();
		String[] projects = new String[] { "hibernate", "spring", "struts", "commons-csv", "commons-io",
				"elasticsearch", "maven", "strman-java", "tablesaw" };
		for (String project : projects) {
			System.out.println(project + " start.");
			commitQuery.put("project", project);
			FindIterable<Document> commitCursor = commits.find(commitQuery);

			int commitID = 0;
			for (Document commit : commitCursor) {
				if (!(Boolean) commit.get("has_core_class")) {
					continue;
				}
				// if commit message is too long or too short,skip it.
				String message = (String) commit.get("message");
				String splitToken = " .,;:/&|`~%+=-*<>$#@!^\\()[]{}''\"\r\n";
				StringTokenizer st = new StringTokenizer(message, splitToken, false);
				if (st.countTokens() < 3 || st.countTokens() > 100) {
					continue;
				}

				int commitType1 = 0;
				int commitType2 = 0;
				int commitType3 = 0;
				int commitType4 = 0;
				for (int i = 0; i < type1.length; i++) {
					if (message != null && message.contains(type1[i])) {
						commitType1 = 1;
					}
				}
				for (int i = 0; i < type2.length; i++) {
					if (message != null && message.contains(type2[i])) {
						commitType2 = 1;
					}
				}
				for (int i = 0; i < type3.length; i++) {
					if (message != null && message.contains(type3[i])) {
						commitType3 = 1;
					}
				}
				for (int i = 0; i < type4.length; i++) {
					if (message != null && message.contains(type4[i])) {
						commitType4 = 1;
					}
				}

				MongoCollection<Document> classes = db.getCollection("class");

				BasicDBObject classQuery = new BasicDBObject();
				classQuery.put("commitID", commit.get("commit_id"));
				classQuery.put("project", commit.get("project"));
				FindIterable<Document> classCursor = classes.find(classQuery);
				int classNum = 0;
				for (Document clazz : classCursor) {
					classNum++;
				}
				// if class num of commit is too large or too small,skip it.
				if (classNum < 2 || classNum > 20) {
					continue;
				}

				int count = 0;
				for (Document clazz : classCursor) {
					if ((Boolean) clazz.get("isCore")) {
						count++;
					}
				}
				if (count > classNum * 2 / 3) {
					continue;
				}

				int changeMethodsNum = 0;
				int newMethodsNum = 0;
				int deleteMethodsNum = 0;
				int statementsNum = 0;
				int newStatementsNum = 0;
				int changeStatementsNum = 0;
				int deleteStatementsNum = 0;

				int maxInnerCount = 0;
				int maxOuterCount = 0;
				int aveInnerCount = 0;
				int aveOuterCount = 0;

				int maxStatementNum = 0;
				int aveStatementNum = 0;

				commitID++;
				for (Document clazz : classCursor) {

					classList.add((String) clazz.get("project") + "," + (Integer) clazz.get("commitID") + ","
							+ (String) clazz.get("className"));

					int classInnerCount = (Integer) clazz.get("innerCount");
					int classOuterCount = (Integer) clazz.get("outterCount");
					aveInnerCount += classInnerCount;
					aveOuterCount += classOuterCount;
					if (maxInnerCount < classInnerCount) {
						maxInnerCount = classInnerCount;
					}
					if (maxOuterCount < classOuterCount) {
						maxOuterCount = classOuterCount;
					}

					newMethodsNum += (Integer) clazz.get("newMethodNum");
					changeMethodsNum += (Integer) clazz.get("changeMethodNum");
					deleteMethodsNum += (Integer) clazz.get("deleteMethodNum");

					MongoCollection<Document> methods = db.getCollection("method");

					BasicDBObject methodQuery = new BasicDBObject();
					methodQuery.put("commitID", clazz.get("commitID"));
					methodQuery.put("project", clazz.get("project"));
					methodQuery.put("classID", clazz.get("classID"));

					FindIterable<Document> methodCursor = methods.find(methodQuery);

					int statementNum = 0;
					for (Document method : methodCursor) {
						@SuppressWarnings("unchecked")
						List<Document> statements = (List<Document>) method.get("statements");
						statementNum += statements.size();
						for (Document statement : statements) {
							String type = (String) statement.get("type");
							if (type.equals("new")) {
								newStatementsNum++;
							} else if (type.equals("change")) {
								changeStatementsNum++;
							} else {
								deleteStatementsNum++;
							}
						}
					}
					aveStatementNum += statementNum;
					if (statementNum > maxStatementNum) {
						maxStatementNum = statementNum;
					}
				}

				if (classNum == 0) {
					aveInnerCount = 0;
					aveOuterCount = 0;
					aveStatementNum = 0;
				}

				int methodsNum = changeMethodsNum + newMethodsNum + deleteMethodsNum;
				statementsNum = newStatementsNum + changeStatementsNum + deleteStatementsNum;
				for (Document clazz : classCursor) {
					int classIndex = (Integer) clazz.get("classIndex");
					List<Integer> vector = new ArrayList<Integer>();
					int classInnerCount = (Integer) clazz.get("innerCount");
					int classOuterCount = (Integer) clazz.get("outterCount");

					// 1.
					vector.add(commitID);

					// 2.
					vector.add(commitType1);

					// 3.
					vector.add(commitType2);

					// 4.
					vector.add(commitType3);

					// 5.
					vector.add(commitType4);

					// 6.
					if (classInnerCount == 0) {
						vector.add(0);
					} else if (classInnerCount == 1) {
						vector.add(1);
					} else if (classInnerCount == 2) {
						vector.add(2);
					} else if (classInnerCount == 3) {
						vector.add(3);
					} else if (classInnerCount == 4) {
						vector.add(4);
					} else if (classInnerCount == 5) {
						vector.add(5);
					} else {
						vector.add(6);
					}

					// 7.
					if (classOuterCount == 0) {
						vector.add(0);
					} else if (classOuterCount == 1) {
						vector.add(1);
					} else if (classOuterCount == 2) {
						vector.add(2);
					} else if (classOuterCount == 3) {
						vector.add(3);
					} else if (classOuterCount == 4) {
						vector.add(4);
					} else if (classOuterCount == 5) {
						vector.add(5);
					} else {
						vector.add(6);
					}

					// 8:classInnerCount:maxInnerCount
					if (maxInnerCount == 0) {
						vector.add(0);
					} else {
						vector.add(classInnerCount * 5 / maxInnerCount);
					}

					// 9:classInnerCount:aveInnerCount
					int classInnerCount_aveInnerCount = 0;
					if (aveInnerCount > 0) {
						classInnerCount_aveInnerCount = classInnerCount * classNum / aveInnerCount;
					}
					if (classInnerCount_aveInnerCount < 5) {
						vector.add(classInnerCount_aveInnerCount);
					} else {
						vector.add(5);
					}

					// 10:classOuterCount:maxOuterCount
					if (maxOuterCount == 0) {
						vector.add(0);
					} else {
						vector.add(classOuterCount * 5 / maxOuterCount);
					}

					// 11:classOuterCount:aveOuterCount
					int classOuterCount_aveOuterCount = 0;
					if (aveOuterCount > 0) {
						classOuterCount_aveOuterCount = classOuterCount * classNum / aveOuterCount;
					}
					if (classOuterCount_aveOuterCount < 5) {
						vector.add(classOuterCount_aveOuterCount);
					} else {
						vector.add(5);
					}

					// 12.ClassType
					if (((String) clazz.get("classType")).equals("change")) {
						vector.add(1);
					} else {
						vector.add(0);
					}

					// 13:classIndex
					if (classIndex < 5) {
						vector.add(classIndex);
					} else {
						vector.add(5);
					}
					// 14.isCoreType1
					if (classInnerCount == 0 && classOuterCount > 0) {
						vector.add(1);
					} else if (classInnerCount > 0 && classOuterCount > 0) {
						vector.add(2);
					} else if (classInnerCount == 0 && classOuterCount == 0) {
						vector.add(3);
					} else {
						vector.add(4);
					}

					// 15.isCoreType2
					if (classOuterCount == 0 && classInnerCount > 1) {
						vector.add(1);
					} else {
						vector.add(0);
					}

					// 16.isCoreType3
					if (classInnerCount + classOuterCount > 3) {
						vector.add(1);
					} else {
						vector.add(0);
					}

					// 17.isCoreType4
					if (classOuterCount - classInnerCount > 2) {
						vector.add(1);
					} else if (classOuterCount - classInnerCount == 2) {
						vector.add(2);
					} else if (classOuterCount - classInnerCount == 1) {
						vector.add(3);
					} else if (classOuterCount - classInnerCount == 0) {
						vector.add(4);
					} else if (classOuterCount - classInnerCount == -1) {
						vector.add(5);
					} else if (classOuterCount - classInnerCount == -2) {
						vector.add(6);
					} else {
						vector.add(7);
					}

					// 18.isCoreType5
					if (classInnerCount - classOuterCount > 2) {
						vector.add(1);
					} else {
						vector.add(0);
					}

					// 19.
					for (int i = 0; i < 1; i++) {
						double classInnerWeight = classInnerCount * 1.0d / classNum;
						if (classInnerWeight == 0) {
							vector.add(0);
						} else if (classInnerWeight <= 0.1) {
							vector.add(1);
						} else if (classInnerWeight <= 0.2) {
							vector.add(2);
						} else if (classInnerWeight <= 0.3) {
							vector.add(3);
						} else if (classInnerWeight <= 0.4) {
							vector.add(4);
						} else if (classInnerWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 20.
					double classOuterWeight = classOuterCount * 1.0d / classNum;
					if (classOuterWeight == 0) {
						vector.add(0);
					} else if (classOuterWeight <= 0.1) {
						vector.add(1);
					} else if (classOuterWeight <= 0.2) {
						vector.add(2);
					} else if (classOuterWeight <= 0.3) {
						vector.add(3);
					} else if (classOuterWeight <= 0.4) {
						vector.add(4);
					} else if (classOuterWeight <= 0.5) {
						vector.add(5);
					} else {
						vector.add(6);
					}

					// 21.
					if (classNum == 2) {
						vector.add(0);
					} else if (classNum == 3) {
						vector.add(1);
					} else if (classNum == 4) {
						vector.add(2);
					} else if (classNum == 5) {
						vector.add(3);
					} else if (classNum > 5 && classNum <= 10) {
						vector.add(4);
					} else {
						vector.add(5);
					}

					int newMethodNum = (Integer) clazz.get("newMethodNum");
					int changeMethodNum = (Integer) clazz.get("changeMethodNum");
					int deleteMethodNum = (Integer) clazz.get("deleteMethodNum");

					int methodNum = newMethodNum + changeMethodNum + deleteMethodNum;

					// 22.
					if (methodNum <= 5) {
						vector.add(methodNum);
					} else if (methodNum <= 10) {
						vector.add(6);
					} else if (methodNum <= 20) {
						vector.add(7);
					} else {
						vector.add(8);
					}

					// 23.
					if (methodsNum == 0) {
						vector.add(0);
					} else {
						double methodNumWeight = methodNum * 1.0d / methodsNum;
						if (methodNumWeight == 0) {
							vector.add(0);
						} else if (methodNumWeight <= 0.1) {
							vector.add(1);
						} else if (methodNumWeight <= 0.2) {
							vector.add(2);
						} else if (methodNumWeight <= 0.3) {
							vector.add(3);
						} else if (methodNumWeight <= 0.4) {
							vector.add(4);
						} else if (methodNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 24.
					if (newMethodNum <= 5) {
						vector.add(newMethodNum);
					} else if (newMethodNum <= 10) {
						vector.add(6);
					} else {
						vector.add(7);
					}

					// 25.
					if (newMethodsNum == 0) {
						vector.add(0);
					} else {
						double newMethodNumWeight = newMethodNum * 1.0d / newMethodsNum;
						if (newMethodNumWeight == 0) {
							vector.add(0);
						} else if (newMethodNumWeight <= 0.1) {
							vector.add(1);
						} else if (newMethodNumWeight <= 0.2) {
							vector.add(2);
						} else if (newMethodNumWeight <= 0.3) {
							vector.add(3);
						} else if (newMethodNumWeight <= 0.4) {
							vector.add(4);
						} else if (newMethodNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 26.
					if (changeMethodNum <= 5) {
						vector.add(changeMethodNum);
					} else if (changeMethodNum <= 10) {
						vector.add(6);
					} else {
						vector.add(7);
					}

					// 27.
					if (changeMethodsNum == 0) {
						vector.add(0);
					} else {
						double changeMethodNumWeight = changeMethodNum * 1.0d / changeMethodsNum;
						if (changeMethodNumWeight == 0) {
							vector.add(0);
						} else if (changeMethodNumWeight <= 0.1) {
							vector.add(1);
						} else if (changeMethodNumWeight <= 0.2) {
							vector.add(2);
						} else if (changeMethodNumWeight <= 0.3) {
							vector.add(3);
						} else if (changeMethodNumWeight <= 0.4) {
							vector.add(4);
						} else if (changeMethodNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 28.
					if (deleteMethodNum <= 3) {
						vector.add(deleteMethodNum);
					} else if (deleteMethodNum <= 5) {
						vector.add(4);
					} else if (deleteMethodNum <= 10) {
						vector.add(5);
					} else {
						vector.add(6);
					}

					// 29.
					if (deleteMethodsNum == 0) {
						vector.add(0);
					} else {
						double deleteMethodNumWeight = deleteMethodNum * 1.0d / deleteMethodsNum;
						if (deleteMethodNumWeight == 0) {
							vector.add(0);
						} else if (deleteMethodNumWeight <= 0.1) {
							vector.add(1);
						} else if (deleteMethodNumWeight <= 0.2) {
							vector.add(2);
						} else if (deleteMethodNumWeight <= 0.3) {
							vector.add(3);
						} else if (deleteMethodNumWeight <= 0.4) {
							vector.add(4);
						} else if (deleteMethodNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					MongoCollection<Document> methods = db.getCollection("method");

					BasicDBObject methodQuery = new BasicDBObject();
					methodQuery.put("commitID", clazz.get("commitID"));
					methodQuery.put("project", clazz.get("project"));
					methodQuery.put("classID", clazz.get("classID"));

					int methodsInnerCount = 0;
					int methodsOuterCount = 0;
					int changeStatementNum = 0;
					int newStatementNum = 0;
					int deleteStatementNum = 0;
					FindIterable<Document> methodCursor = methods.find(methodQuery);
					for (Document method : methodCursor) {
						methodsInnerCount += (Integer) method.get("innerCount");
						methodsOuterCount += (Integer) method.get("outerCount");

						@SuppressWarnings("unchecked")
						List<Document> statements = (List<Document>) method.get("statements");

						for (Document statement : statements) {
							String type = (String) statement.get("type");
							if (type.equals("new")) {
								newStatementNum++;
							} else if (type.equals("change")) {
								changeStatementNum++;
							} else if (type.equals("delete")) {
								deleteStatementNum++;
							}
						}
					}
					int statementNum = newStatementNum + changeStatementNum + deleteStatementNum;

					// 30.
					if (methodsInnerCount == 0) {
						vector.add(0);
					} else if (methodsInnerCount <= 3) {
						vector.add(1);
					} else if (methodsInnerCount <= 6) {
						vector.add(2);
					} else {
						vector.add(3);
					}

					// 31.
					if (methodsOuterCount == 0) {
						vector.add(0);
					} else if (methodsOuterCount <= 3) {
						vector.add(1);
					} else if (methodsOuterCount <= 6) {
						vector.add(2);
					} else {
						vector.add(3);
					}

					// 32.
					if (statementNum <= 5) {
						vector.add(statementNum);
					} else if (statementNum <= 10) {
						vector.add(6);
					} else if (statementNum <= 20) {
						vector.add(7);
					} else if (statementNum <= 30) {
						vector.add(8);
					} else if (statementNum <= 40) {
						vector.add(9);
					} else {
						vector.add(10);
					}

					// 33.
					if (statementsNum == 0) {
						vector.add(0);
					} else {
						double statementNumWeight = statementNum * 1.0d / statementsNum;
						if (statementNumWeight == 0) {
							vector.add(0);
						} else if (statementNumWeight <= 0.1) {
							vector.add(1);
						} else if (statementNumWeight <= 0.2) {
							vector.add(2);
						} else if (statementNumWeight <= 0.3) {
							vector.add(3);
						} else if (statementNumWeight <= 0.4) {
							vector.add(4);
						} else if (statementNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 34. statementNum/maxStatementNum
					if (maxStatementNum == 0) {
						vector.add(0);
					} else {
						vector.add(statementNum * 5 / maxStatementNum);
					}

					// 35. statementNum/aveStatementNum
					int statementNum_aveStatementNum = 0;
					if (aveStatementNum > 0) {
						statementNum_aveStatementNum = statementNum * classNum / aveStatementNum;
					}
					if (statementNum_aveStatementNum < 5) {
						vector.add(statementNum_aveStatementNum);
					} else {
						vector.add(5);
					}

					// 36
					if (newStatementNum == 0) {
						vector.add(0);
					} else if (newStatementNum <= 5) {
						vector.add(newStatementNum);
					} else if (newStatementNum <= 10) {
						vector.add(6);
					} else if (newStatementNum <= 20) {
						vector.add(7);
					} else {
						vector.add(8);
					}

					// 37
					if (newStatementsNum == 0) {
						vector.add(0);
					} else {
						double newStatementNumWeight = newStatementNum * 1.0d / newStatementsNum;
						if (newStatementNumWeight == 0) {
							vector.add(0);
						} else if (newStatementNumWeight <= 0.1) {
							vector.add(1);
						} else if (newStatementNumWeight <= 0.2) {
							vector.add(2);
						} else if (newStatementNumWeight <= 0.3) {
							vector.add(3);
						} else if (newStatementNumWeight <= 0.4) {
							vector.add(4);
						} else if (newStatementNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 38
					if (changeStatementNum == 0) {
						vector.add(0);
					} else if (changeStatementNum <= 5) {
						vector.add(changeStatementNum);
					} else if (changeStatementNum <= 10) {
						vector.add(6);
					} else {
						vector.add(7);
					}

					// 39
					if (changeStatementsNum == 0) {
						vector.add(0);
					} else {
						double changeStatementNumWeight = changeStatementNum * 1.0d / changeStatementsNum;
						if (changeStatementNumWeight == 0) {
							vector.add(0);
						} else if (changeStatementNumWeight <= 0.1) {
							vector.add(1);
						} else if (changeStatementNumWeight <= 0.2) {
							vector.add(2);
						} else if (changeStatementNumWeight <= 0.3) {
							vector.add(3);
						} else if (changeStatementNumWeight <= 0.4) {
							vector.add(4);
						} else if (changeStatementNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					// 40
					if (deleteStatementNum == 0) {
						vector.add(0);
					} else if (deleteStatementNum <= 3) {
						vector.add(deleteStatementNum);
					} else if (deleteStatementNum <= 10) {
						vector.add(4);
					} else {
						vector.add(5);
					}

					// 41
					if (deleteStatementsNum == 0) {
						vector.add(0);
					} else {
						double deleteStatementNumWeight = deleteStatementNum * 1.0d / deleteStatementsNum;
						if (deleteStatementNumWeight == 0) {
							vector.add(0);
						} else if (deleteStatementNumWeight <= 0.1) {
							vector.add(1);
						} else if (deleteStatementNumWeight <= 0.2) {
							vector.add(2);
						} else if (deleteStatementNumWeight <= 0.3) {
							vector.add(3);
						} else if (deleteStatementNumWeight <= 0.4) {
							vector.add(4);
						} else if (deleteStatementNumWeight <= 0.5) {
							vector.add(5);
						} else {
							vector.add(6);
						}
					}

					vector.add(((Boolean) clazz.get("isCore")) ? 1 : 0);

					if ((Boolean) clazz.get("isCore")) {
						class1List.add(vector);
					} else {
						class0List.add(vector);
					}
					vectorList.add(vector);
				}
			}
		}
		System.out.println("vectorList size:" + vectorList.size());
		System.out.println("classList size:" + classList.size());

		System.out.println("vectorList size(before):" + vectorList.size());
		for (List<Integer> vector : vectorList) {
			if (vector.get(vector.size() - 1) == 1) {
				class1List.add(vector);
			} else {
				class0List.add(vector);
			}
		}
		List<Integer> deleteIndex = new ArrayList<Integer>();
		for (int i = 0; i < vectorList.size(); i++) {
			List<Integer> vector = vectorList.get(i);
			if (vector.get(vector.size() - 1) == 1) {
				for (int j = 0; j < vectorList.size(); j++) {
					List<Integer> vector2 = vectorList.get(j);
					if (vector2.get(vector.size() - 1) == 0) {
						if (computeSimilar(vector, vector2) >= 0.995d) {
							if (!deleteIndex.contains(i)) {
								deleteIndex.add(i);
							}
							if (!deleteIndex.contains(j)) {
								deleteIndex.add(j);
							}
						}
					}
				}
			}
		}

		System.out.println("deleteIndex size:" + deleteIndex.size());
		List<List<Integer>> tempVectors = new ArrayList<List<Integer>>();
		List<String> tempClassList = new ArrayList<String>();
		for (int i = 0; i < vectorList.size(); i++) {
			if (!deleteIndex.contains(i)) {
				tempVectors.add(vectorList.get(i));
				tempClassList.add(classList.get(i));
			}
		}

		vectorList = tempVectors;
		classList = tempClassList;
		System.out.println("vectorList size (after):" + vectorList.size());
		System.out.println("class list size(after):" + classList.size());

		try {
			BufferedWriter testWriter = new BufferedWriter(
					new FileWriter(new File(path + "/" +  "test.arff")));

			testWriter.write("@relation 'class_core_vector'\r\n");
			testWriter.write("@attribute 'commitID' numeric\r\n");
			testWriter.write("@attribute 'commitType1' {0,1}\r\n");
			testWriter.write("@attribute 'commitType2' {0,1}\r\n");
			testWriter.write("@attribute 'commitType3' {0,1}\r\n");
			testWriter.write("@attribute 'commitType4' {0,1}\r\n");
			testWriter.write("@attribute 'classInnerCount' numeric\r\n");
			testWriter.write("@attribute 'classOuterCount' numeric\r\n");
			testWriter.write("@attribute 'classInnerCount:maxInnerCount' numeric\r\n");
			testWriter.write("@attribute 'classInnerCount:aveInnerCount' numeric\r\n");
			testWriter.write("@attribute 'classOuterCount:maxOuterCount' numeric\r\n");
			testWriter.write("@attribute 'classOuterCount:aveOuterCount' numeric\r\n");
			testWriter.write("@attribute 'classType' {0,1}\r\n");
			testWriter.write("@attribute 'classIndex' numeric\r\n");
			testWriter.write("@attribute 'isCoreType1' numeric\r\n");
			testWriter.write("@attribute 'isCoreType2' {0,1}\r\n");
			testWriter.write("@attribute 'isCoreType3' {0,1}\r\n");
			testWriter.write("@attribute 'isCoreType4' numeric\r\n");
			testWriter.write("@attribute 'isCoreType5' {0,1}\r\n");

			testWriter.write("@attribute 'classInnerCountWeight' numeric\r\n");
			testWriter.write("@attribute 'classOuterCountWeight' numeric\r\n");
			testWriter.write("@attribute 'classNum' numeric\r\n");

			testWriter.write("@attribute 'MethodNum' numeric\r\n");
			testWriter.write("@attribute 'methodWeight' numeric\r\n");
			testWriter.write("@attribute 'newethodNum' numeric\r\n");
			testWriter.write("@attribute 'newMethodWeight' numeric\r\n");
			testWriter.write("@attribute 'changeMethodNum' numeric\r\n");
			testWriter.write("@attribute 'changeMethodWeight' numeric\r\n");
			testWriter.write("@attribute 'deleteMethodNum' numeric\r\n");
			testWriter.write("@attribute 'deleteMethodWeight' numeric\r\n");

			testWriter.write("@attribute 'methodsInnerCount' numeric\r\n");
			testWriter.write("@attribute 'methodsOuterCount' numeric\r\n");

			testWriter.write("@attribute 'statementNum' numeric\r\n");
			testWriter.write("@attribute 'statementWeight' numeric\r\n");
			testWriter.write("@attribute 'statementNum:maxStatementNum' numeric\r\n");
			testWriter.write("@attribute 'statementNum:aveStatementNum' numeric\r\n");
			testWriter.write("@attribute 'newStatementNum' numeric\r\n");
			testWriter.write("@attribute 'newStatementWeight' numeric\r\n");
			testWriter.write("@attribute 'changeStatementNum' numeric\r\n");
			testWriter.write("@attribute 'changeStatementWeight' numeric\r\n");
			testWriter.write("@attribute 'deleteStatementNum' numeric\r\n");
			testWriter.write("@attribute 'deleteStatementWeight' numeric\r\n");
			testWriter.write("@attribute 'class' numeric\r\n");

			testWriter.write("@data\r\n");

			BufferedWriter trainWriter = new BufferedWriter(
					new FileWriter(new File(path + "/" +  "train.arff")));

			trainWriter.write("@relation 'class_core_vector'\r\n");
			trainWriter.write("@attribute 'commitID' numeric\r\n");
			trainWriter.write("@attribute 'commitType1' {0,1}\r\n");
			trainWriter.write("@attribute 'commitType2' {0,1}\r\n");
			trainWriter.write("@attribute 'commitType3' {0,1}\r\n");
			trainWriter.write("@attribute 'commitType4' {0,1}\r\n");
			trainWriter.write("@attribute 'classInnerCount' numeric\r\n");
			trainWriter.write("@attribute 'classOuterCount' numeric\r\n");
			trainWriter.write("@attribute 'classInnerCount:maxInnerCount' numeric\r\n");
			trainWriter.write("@attribute 'classInnerCount:aveInnerCount' numeric\r\n");
			trainWriter.write("@attribute 'classOuterCount:maxOuterCount' numeric\r\n");
			trainWriter.write("@attribute 'classOuterCount:aveOuterCount' numeric\r\n");
			trainWriter.write("@attribute 'classType' {0,1}\r\n");
			trainWriter.write("@attribute 'classIndex' numeric\r\n");
			trainWriter.write("@attribute 'isCoreType1' numeric\r\n");
			trainWriter.write("@attribute 'isCoreType2' {0,1}\r\n");
			trainWriter.write("@attribute 'isCoreType3' {0,1}\r\n");
			trainWriter.write("@attribute 'isCoreType4' numeric\r\n");
			trainWriter.write("@attribute 'isCoreType5' {0,1}\r\n");

			trainWriter.write("@attribute 'classInnerCountWeight' numeric\r\n");
			trainWriter.write("@attribute 'classOuterCountWeight' numeric\r\n");
			trainWriter.write("@attribute 'classNum' numeric\r\n");

			trainWriter.write("@attribute 'MethodNum' numeric\r\n");
			trainWriter.write("@attribute 'methodWeight' numeric\r\n");
			trainWriter.write("@attribute 'newethodNum' numeric\r\n");
			trainWriter.write("@attribute 'newMethodWeight' numeric\r\n");
			trainWriter.write("@attribute 'changeMethodNum' numeric\r\n");
			trainWriter.write("@attribute 'changeMethodWeight' numeric\r\n");
			trainWriter.write("@attribute 'deleteMethodNum' numeric\r\n");
			trainWriter.write("@attribute 'deleteMethodWeight' numeric\r\n");

			trainWriter.write("@attribute 'methodsInnerCount' numeric\r\n");
			trainWriter.write("@attribute 'methodsOuterCount' numeric\r\n");

			trainWriter.write("@attribute 'statementNum' numeric\r\n");
			trainWriter.write("@attribute 'statementWeight' numeric\r\n");
			trainWriter.write("@attribute 'statementNum:maxStatementNum' numeric\r\n");
			trainWriter.write("@attribute 'statementNum:aveStatementNum' numeric\r\n");
			trainWriter.write("@attribute 'newStatementNum' numeric\r\n");
			trainWriter.write("@attribute 'newStatementWeight' numeric\r\n");
			trainWriter.write("@attribute 'changeStatementNum' numeric\r\n");
			trainWriter.write("@attribute 'changeStatementWeight' numeric\r\n");
			trainWriter.write("@attribute 'deleteStatementNum' numeric\r\n");
			trainWriter.write("@attribute 'deleteStatementWeight' numeric\r\n");
			trainWriter.write("@attribute 'class' numeric\r\n");

			trainWriter.write("@data\r\n");

			List<String> testClassList = new ArrayList<String>();
			List<String> trainClassList = new ArrayList<String>();
			for (int i = 0, n = vectorList.size(); i < n; i++) {
				List<Integer> vector = vectorList.get(i);
				if (vector.get(0) % 3 == 0) {
					testClassList.add(classList.get(i));
					for (int j = 0, k = vector.size(); j < k - 1; j++) {
						testWriter.write(vector.get(j) + " ");
					}
					testWriter.write(vector.get(vector.size() - 1) + "\r\n");
				} else {
					trainClassList.add(classList.get(i));
					for (int j = 0, k = vector.size(); j < k - 1; j++) {
						trainWriter.write(vector.get(j) + " ");
					}
					trainWriter.write(vector.get(vector.size() - 1) + "\r\n");
				}
			}
			testWriter.close();
			trainWriter.close();
			FileUtils.writeLines(new File(path + "/testclassList.txt"), testClassList);
			FileUtils.writeLines(new File(path + "/trainclassList.txt"), trainClassList);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("�����ļ�����ʧ��");
		}

	}

	private static boolean containsVector(List<List<Integer>> vectorList, List<Integer> vector) {
		boolean result = false;

		for (List<Integer> v : vectorList) {
			boolean flag = false;
			for (int i = 1; i < v.size() - 1; i++) {
				if (v.get(i) != vector.get(i)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				result = true;
				break;
			}
		}

		return result;
	}

	private static double computeSimilar(List<Integer> v1, List<Integer> v2) {

		List<Double> v11 = new ArrayList<Double>();
		List<Double> v22 = new ArrayList<Double>();

		int[] attriMax = { 1, 1, 1, 1, 1, 6, 6, 5, 5, 5, 5, 1, 5, 4, 1, 1, 7, 1, 6, 6, 5, 8, 6, 7, 6, 7, 6, 6, 6, 3, 3,
				10, 6, 5, 5, 8, 6, 7, 6, 5, 6 };
		for (int i = 1; i < v1.size() - 1; i++) {
			v11.add(v1.get(i) * 1.0d / attriMax[i]);
			v22.add(v2.get(i) * 1.0d / attriMax[i]);
		}

		double similar = 0d;
		double dotMultiply = 0;
		double absV1 = 0;
		double absV2 = 0;
		for (int i = 0; i < v11.size(); i++) {
			dotMultiply += v11.get(i) * v22.get(i);

			absV1 += v11.get(i) * v11.get(i);
			absV2 += v22.get(i) * v22.get(i);
		}

		double temp = Math.sqrt(absV1 * absV2);
		if (temp != 0) {
			similar = dotMultiply / temp;
		}

		return similar;
	}

	public static void main(String[] args) throws IOException {
		GenerateVector.generate("/home/angel/work");
	}
}
