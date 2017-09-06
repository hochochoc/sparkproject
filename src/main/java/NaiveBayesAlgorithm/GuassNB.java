package NaiveBayesAlgorithm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;



public class GuassNB {
	private final List<List<Double>> trainSet;
	private final List<Integer> targetSet;
	public GuassNB(){
		trainSet = new ArrayList<>();
		targetSet = new ArrayList<>();
	}
	
	private static List<List<List<Double>>> splitInputSet(List<List<Double>> input, double splitRatio){
		int trainSize = (int)(input.size()*splitRatio);
//		System.out.println(input.size()*splitRatio);
		List<List<List<Double>>> set = new ArrayList<>();
		List<List<Double>> trainSet = new ArrayList<>();
		Random rand = new Random();
		List<Integer> indexTrain = new ArrayList<>();
		while(trainSet.size()<trainSize){
			int index = rand.nextInt(input.size());
			indexTrain.add(index);
			trainSet.add(input.get(index));
		}
		List<List<Double>> testSet = new ArrayList<>();
		List<Integer> indexTest = new ArrayList<>();
		for (int i=0;i<input.size();i++){
			if (!indexTrain.contains(i)){
				indexTest.add(i);
			}
		}
		for (int i=0;i<indexTest.size();i++){
			testSet.add(input.get(indexTest.get(i)));
		}
		set.add(trainSet);
		set.add(testSet);
		return set;
	}
	//inputMatrix(trainset)input(targetset)
	private static Map<Double, List<List<Double>>> seperateByClass(List<List<Double>> trainset){
		Map<Double, List<List<Double>>> seperated = new HashMap<Double,List<List<Double>>>(); 
		int index = trainset.get(0).size()-1;
		for (int i=0;i< trainset.size();i++){
			//m
			if (seperated.containsKey(trainset.get(i).get(index))){
				seperated.get(trainset.get(i).get(index)).add(trainset.get(i));
			}
			else{
				List<List<Double>> list = new ArrayList<>();
				list.add(trainset.get(i));
				seperated.put(trainset.get(i).get(index), list);
			}
		}
//		System.out.println(seperated);
		return seperated;
	}
	//tinh trung binh và chenh lech
	private static double mean(List<Double> doc){
		double sum = 0.0;
		for (int i=0;i<doc.size();i++){
			sum+=doc.get(i);
		}
		return sum/doc.size();
	}
	private static double standardDeviation(List<Double> doc){
		double average = mean(doc);
//		System.out.println("mean: "+average);
		double sum = 0.0;
		for (int i=0;i<doc.size();i++){
			sum +=Math.pow(average - doc.get(i),2);
		}
		double var = sum/(doc.size()-1);
		return Math.sqrt(var);
	}
	//calculate mean and standard deviation for each doc
	private static List<List<Double>> summarize(List<List<Double>> trainset){
		List<List<Double>> summaries = new ArrayList<>();
		for (int i=0;i<trainset.get(0).size()-1;i++){
			//n
			List<Double> x = new ArrayList<>();
			for (int j=0;j<trainset.size();j++){
				//m
				x.add(trainset.get(j).get(i));
			}
			//mean của các phân tử trừ phần tử cuối
			List<Double> summary = new ArrayList<>();
			summary.add(mean(x));
			summary.add(standardDeviation(x));
			summaries.add(summary);
		}
//		System.out.println(summaries.size()+"\n"+summaries);
		return summaries;
	}
	//tinh mean, stdev cho tap train, de dua vao do tinh probability cho vector du lieu predict
	private static Map<Double, List<List<Double>>> summarizeByClass(List<List<Double>> trainset){
		Map<Double, List<List<Double>>> seperated = seperateByClass(trainset);
		Map<Double, List<List<Double>>> summaries = new HashMap<>();
		for (double i : seperated.keySet()){
			summaries.put(i, summarize(seperated.get(i)));
		}
		return summaries;
	}
	//calculate probability of each feature
	private static double calculateProbability(double x, double mean, double standardDeviation){
		double probability = Math.exp(-Math.pow(x-mean, 2)/(2*Math.pow(standardDeviation, 2)));
		return (1/ (Math.sqrt(2*Math.PI)*standardDeviation))*probability;
	}
	//calculate probability wwith each class off input vector need predict class
	private static Map<Double, Double> calculateProbabilityWithEachClass(List<Double> inputdoc, Map<Double, List<List<Double>>> summaries){
		//summaries = [1:[3.5, 0.555],2:[1.2,0.1]]
		//probabilities = [1: 0.444, 2: 0.666]
		Map<Double, Double> probabilities = new HashMap<Double, Double>();
		for (double key: summaries.keySet()){
			double probability = 1;
			for (int i=0;i<summaries.get(key).size();i++){
				double mean = summaries.get(key).get(i).get(0);
				double stdev = summaries.get(key).get(i).get(1);
//				System.out.println(mean +","+ stdev);
				System.out.println(calculateProbability(inputdoc.get(i), mean, stdev));
				probability *= calculateProbability(inputdoc.get(i), mean, stdev);
//				System.out.println(probability);
			}
			probabilities.put(key, probability);
//			System.out.println(probabilities);
		}
		return probabilities;
	}
	private static double predict(List<Double> inputdoc, Map<Double, List<List<Double>>> summaries){
		Map<Double, Double> probabilities = calculateProbabilityWithEachClass(inputdoc, summaries);
//		System.out.println(probabilities.values());
		Map.Entry<Double, Double> maxEntry = null;
		for (Map.Entry<Double, Double> entry: probabilities.entrySet()){
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue())>0){
				maxEntry = entry;
			}
		}
		return maxEntry.getKey();
	}
	private static List<Double> getPredictions(List<List<Double>> input, Map<Double, List<List<Double>>> summaries){
		List<Double> predictions = new ArrayList<>();
		for (int i=0; i<input.size(); i++){
			double result = predict(input.get(i), summaries);
			predictions.add(result);
		}
		return predictions;
	}
	private static double getAccuracy(List<List<Double>> testSet, List<Double> predictions){
		int correct = 0;
		for (int i=0;i<testSet.size();i++){
			if (testSet.get(i).get(testSet.get(i).size()-1).compareTo(predictions.get(i))==0){
				correct +=1;
			}
		}
		System.out.println(correct);
		return (correct/(float)(testSet.size()))*100;
	}
	
	public static void main(String[] args) throws IOException {
		GuassNB nb = new GuassNB();
		List<List<Double>> trainset = new ArrayList<>();
		List<List<Double>> testset = new ArrayList<>();
		FileReader inputFile = new FileReader("/home/hocx3/workspace/data/pima-indians-diabetes.data.txt");
		BufferedReader reader = new BufferedReader(inputFile);
		String lines = reader.readLine();
		while(lines!=null){
//			System.out.println(lines);
			StringTokenizer tokens = new StringTokenizer(lines, ",");
			List<Double> doc = new ArrayList<>();
			while(tokens.hasMoreTokens()){
				double x = Double.parseDouble(tokens.nextToken());
				doc.add(x);
			}
			trainset.add(doc);
			lines = reader.readLine();
		}
//		System.out.println(trainset.size());
		trainset = nb.splitInputSet(trainset, 0.75).get(0);
		testset = nb.splitInputSet(trainset, 0.75).get(1);
		
//		System.out.println(trainset.size());
		Map<Double, List<List<Double>>> summaries = summarizeByClass(trainset);
		List<Double> predictions = getPredictions(testset, summaries);
		System.out.println(testset);
		System.out.println(predictions);
		System.out.println(getAccuracy(testset, predictions));
	}
}
