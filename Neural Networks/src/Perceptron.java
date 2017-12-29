

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HW3 {
	
	static final int HAM = -1, SPAM = 1;
	static int ITERATIONS = 100;
	
	static final PorterStemmer stemmer = new PorterStemmer();
	
	static String hamDir, spamDir;
	
	static double learningRate = 0.9;
	
	static boolean skipStopWords = false;
	
	/**
	 * 
	 * @param args spam ham true|false learningRate iterations
	 */
	public static void main(String args[]){
		spamDir = args[0];
		hamDir = args[1];
		skipStopWords = new Boolean(args[2]);
		learningRate = args[3]!=null?new Double(args[3]):learningRate;
		ITERATIONS = args[4]!=null?new Integer(args[4]):ITERATIONS;
		
		double weights[];
		int counts[][];
		
		//for stopwords
		String path = System.getProperty("user.dir") + "/stopWords.txt";
		File stopWordsFile = new File(path);
		
		String line;
		List<String> stopWords = new ArrayList<>();
		String contents = "";
		try{
			InputStream fis = new FileInputStream(stopWordsFile);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			while((line = br.readLine()) != null){
				stopWords.add(line.toLowerCase());
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//training
		List<Document> documents = new ArrayList<>();
		List<String> vocabulary;
		Set<String> vocabularyHash = new HashSet<String>();
		readDocuments("train", HAM, documents);
		readDocuments("train", SPAM, documents);
		for(Document document : documents){
			vocabularyHash.addAll(document.words);
		}
		vocabulary = new ArrayList<String>(vocabularyHash);
		counts = new int[documents.size()][vocabulary.size() + 2];
		for(int i=0; i<documents.size(); i++){
			Document document = documents.get(i);
			counts[i][0] = 1;
			counts[i][vocabulary.size() + 1] = document.classCode;
			for(int j=1; j<vocabulary.size(); j++){
				counts[i][j] = Collections.frequency(document.words, vocabulary.get(j - 1));
			}
		}
		weights = new double[vocabulary.size() + 1];
		for(int i=0; i<weights.length; i++){
			weights[i] = randomNumber(0, 1);
		}
		for(int i=0; i<ITERATIONS; i++){
			for(int j=0; j<documents.size(); j++){
				Document document = documents.get(j);
				double sum = 0, result;
				for(int k=0; k<vocabulary.size()+1; k++){
					sum += counts[j][k] * weights[k];
				}
				result = (sum>0?SPAM:HAM);
				
				for(int k=0; k<vocabulary.size()+1; k++){
					if(result!=document.classCode){
						weights[k] += learningRate * (document.classCode - result) * counts[j][k];
					}
				}
			}
		}
		
		//testing
		List<Document> testDocuments = new ArrayList<>();
		readDocuments("test", HAM, testDocuments);
		readDocuments("test", SPAM, testDocuments);
		double successes = 0;
		for(Document document : testDocuments){
			//int count[] = new int[vocabulary.size() + 1];
			int sum = 0, result;
			sum += weights[0];
			for(int i=0; i<vocabulary.size(); i++){
				if(skipStopWords && stopWords.contains(vocabulary.get(i))) { //checking for stopwords
					continue;
				}
				sum += weights[i+1] * Collections.frequency(document.words, vocabulary.get(i));
			}
			result = (sum>0?SPAM:HAM);
			if(result == document.classCode) successes++;
		}
		
		System.out.println("Accuracy: " + (successes/testDocuments.size()) * 100); //calculating accuracy
		
		
		

		
		
		
	}
	//Reading the documents one by one and getting the unique words in the vocabulary
	public static List<Document> readDocuments(String function, int classCode, List<Document> documents){
		File directory = new File(getPathOfDocument(function, (classCode==HAM?"ham":"spam")));
		File files[] = directory.listFiles();
		File file;
		for(int i=0; i<files.length; i++){
			file = files[i];
			if(file.isFile()){
				try {
					List<String> words = new ArrayList<>();
					String line;
					String documentContents = "";
					InputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);
					while((line = br.readLine()) != null){
						documentContents = documentContents + " " + line.replaceAll("[^a-zA-Z0-9]", "");;
					}
					words = getSpaceSplitContents(documentContents);
					for(int j=0; j<words.size(); j++){
						words.set(j, stemmer.stem(words.get(j)).toLowerCase());
					}
					Document document = new Document();
					document.setWords(words);
					document.setClassCode(classCode);
					documents.add(document);
					
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return documents;
	}
	//Returns an array of words
	public static List<String> getSpaceSplitContents(String content){
		String contents[] = {};
		ArrayList<String> strings = new ArrayList<>();
		contents = content.split(" ");
		for(int i=0; i<contents.length; i++){
			if(contents[i] != null && contents[i].length()>0)
				strings.add(contents[i]);
		}
		return strings;
	}
	//Gets the path of the directory
	public static String getPathOfDocument(String function, String documentClass){
		return System.getProperty("user.dir") + "/" + function + "/" + documentClass;
	}
	// Generating the random values for weight
	public static double randomNumber(int min , int max) {
		DecimalFormat df = new DecimalFormat("#.####");
		double d = min + Math.random() * (max - min);
		String s = df.format(d);
		double x = Double.parseDouble(s);
		return x;
	}
	
}
