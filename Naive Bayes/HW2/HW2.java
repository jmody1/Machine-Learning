package HW2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class HW2 {
	

	private HashMap<String, Integer> vocabulary = new HashMap();
	private HashMap<String, Integer> countInHam = new HashMap();
	private HashMap<String, Integer> countInSpam = new HashMap();
	private HashMap<String, Double> conditionalProbabilitiesInHam = new HashMap<>();
	private HashMap<String, Double> conditionalProbabilitiesInSpam = new HashMap<>();
	private double n;
	private String hamText = getConcatenatedDocuments("ham").toLowerCase();
	private String spamText = getConcatenatedDocuments("spam").toLowerCase();
	private double prior[] = new double[2];
	private PorterStemmer stemmer = new PorterStemmer();
	
	public static void main(String args[]){
		new HW2();
	}
	
	HW2(){
		train();
		double hamAccuracy = test("ham");
		double spamAccuracy = test("spam");
		
		double totalAccuracy = (hamAccuracy + spamAccuracy) / 2d;
		
		System.out.println("Accuracy on ham: " + hamAccuracy);
		System.out.println("Accuracy on spam: " + spamAccuracy);
		System.out.println("Total accuracy: " + totalAccuracy);
		
		String path = System.getProperty("user.dir") + "/stopWords.txt";
		File stopWordsFile = new File(path);
		String line;
		ArrayList<String> words = new ArrayList<>();
		try{
			InputStream fis = new FileInputStream(stopWordsFile);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			while((line = br.readLine()) != null){
				words.add(line);
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(String word: words){
			vocabulary.remove(word);
		}
		
		
		hamAccuracy = test("ham");
		spamAccuracy = test("spam");
		
		totalAccuracy = (hamAccuracy + spamAccuracy) / 2d;
		
		System.out.println("Accuracy on ham: " + hamAccuracy);
		System.out.println("Accuracy on spam: " + spamAccuracy);
		System.out.println("Total accuracy: " + totalAccuracy);
		
		
	}
	
	public void train(){
		addToVocabulary(hamText);
		addToVocabulary(spamText);
		n = getNumberOfDocuments("ham") + getNumberOfDocuments("spam");
		prior[0] = (getNumberOfDocuments("ham")) / (n);
		prior[1] = (getNumberOfDocuments("spam")) / (n);
		
		
		
		String uniqueWords[];
		uniqueWords = vocabulary.keySet().toArray(new String[vocabulary.size()]);
		
		//System.out.println(uniqueWords.length);
		for(int i=0; i<uniqueWords.length; i++){
			int countHam = countOcurrences(uniqueWords[i], hamText);
			countInHam.put(uniqueWords[i], countHam);
			int countSpam = countOcurrences(uniqueWords[i], spamText);
			countInSpam.put(uniqueWords[i], countSpam);
		}
		
		for(int i=0; i<uniqueWords.length; i++){
			if(countInHam.get(uniqueWords[i]) != null){
				double conditionalProbabilityForHam = new Double(countInHam.get(uniqueWords[i]) + 1) / new Double(getSizeOfContent(hamText) + vocabulary.size());
				conditionalProbabilitiesInHam.put(uniqueWords[i], conditionalProbabilityForHam);
				
			}
			
			if(countInSpam.get(uniqueWords[i]) != null){
				double conditionalProbabilityForSpam = new Double(countInSpam.get(uniqueWords[i]) + 1) / new Double(getSizeOfContent(spamText) + vocabulary.size());
				conditionalProbabilitiesInSpam.put(uniqueWords[i], conditionalProbabilityForSpam);
			}
		}
	}
	
	public double test(String documentClass){
		File directory = new File(getPathOfDocument("test", documentClass));
		File files[] = directory.listFiles();
		File file;
		String line, documentContents = "";
		int count = 0;
		for(int i=0; i<files.length; i++){
			documentContents = "";
			file = files[i];
			if(file.isFile()){
				try{
					InputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);
					while((line = br.readLine()) != null){
						documentContents = documentContents + " " + line.replaceAll("[^a-zA-Z0-9]", "");
					}
				} catch(FileNotFoundException e){
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				double score[] = new double[2];
				score[0] = Math.log(prior[0]); //ham
				score[1] = Math.log(prior[1]); //spam
				
				
				String[] spaceSeperatedDocumentContents = documentContents.split(" ");
				for(int j=0; j<spaceSeperatedDocumentContents.length; j++){
					if(vocabulary.containsKey(stemmer.stem(spaceSeperatedDocumentContents[j]))){
						if(conditionalProbabilitiesInHam.get(stemmer.stem(spaceSeperatedDocumentContents[j])) != null){
							score[0] += Math.log(conditionalProbabilitiesInHam.get(stemmer.stem(spaceSeperatedDocumentContents[j])));
						}

						if(conditionalProbabilitiesInSpam.get(stemmer.stem(spaceSeperatedDocumentContents[j])) != null)
						score[1] += Math.log(conditionalProbabilitiesInSpam.get(stemmer.stem(spaceSeperatedDocumentContents[j])));
					}
				}
				

				//System.out.println(score[0] + " " + score[1]);
				
				if(score[0]>score[1]){
					if(documentClass.equals("ham")) count ++;
				} else {
					if(documentClass.equals("spam")) count ++;
				}
			}
		}
		
		return count/(new Double(files.length)) * 100d;
	}
	

	public int countOcurrences(String word, String content){
		int lastIndex = 0;
		int count = 0;
		while(lastIndex != -1){
		    lastIndex = content.indexOf(word, lastIndex);
		    if(lastIndex != -1){
		        count ++;
		        lastIndex += word.length();
		    }
		}
		return count;
	}
	
	public String[] getSpaceSplitContents(String content){
		String contents[] = {};
		ArrayList<String> strings = new ArrayList<>();
		contents = content.split(" ");
		for(int i=0; i<contents.length; i++){
			if(contents[i] != null && contents[i].length()>0)
				strings.add(contents[i]);
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	public int getSizeOfContent(String content){
		String spaceSplitContent[] = getSpaceSplitContents(content);
		return spaceSplitContent.length;
	}
	
	public void addToVocabulary(String content){
		String spaceSplitContent[] = getSpaceSplitContents(content);
		for(int i=0; i<spaceSplitContent.length; i++){
			if(spaceSplitContent.length==0) continue;
			vocabulary.put(stemmer.stem(spaceSplitContent[i]), 0);
		}
	}
	
	public int getNumberOfDocuments(String documentClass){
		File directory = new File(getPathOfDocument("train", documentClass));
		File files[] = directory.listFiles();
		return files.length;
	}
	
	public String getConcatenatedDocuments(String documentClass){
		File directory = new File(getPathOfDocument("train", documentClass));
		
		File files[] = directory.listFiles();
		File file;
		String line, documentContents = "";
		for(int i=0; i<files.length; i++){
			file = files[i];
			if(file.isFile()){
				try{
					InputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);
					while((line = br.readLine()) != null){
						documentContents = documentContents + " " + line.replaceAll("[^a-zA-Z0-9]", "");;
					}
				} catch(FileNotFoundException e){
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return documentContents;
	}
	
	public String getPathOfDocument(String function, String documentClass){
		return System.getProperty("user.dir") + "/" + function + "/" + documentClass;
	}
	
}
