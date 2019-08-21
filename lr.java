import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class lr {
    public static void main(String[] args) {
        String formattedTraingingInput = args[0];
        String formattedValidationInput = args[1];
        String formattedTestingInput = args[2];
        String dictInput = args[3];
        String trainingOutput = args[4];
        String testingOutput = args[5];
        String metricsOutput = args[6];
        String numberOfEpochs = args[7];

        feature.Pair<ArrayList<Integer>, ArrayList<HashMap<Integer, Integer>>> trainingLabelsAndReviews = readTSV(formattedTraingingInput);
        feature.Pair<ArrayList<Integer>, ArrayList<HashMap<Integer, Integer>>> validationLabelsAndReviews = readTSV(formattedValidationInput);
        feature.Pair<ArrayList<Integer>, ArrayList<HashMap<Integer, Integer>>> testingLabelsAndReviews = readTSV(formattedTestingInput);

        ArrayList<Integer> trainingLabels = trainingLabelsAndReviews.getKey();
        ArrayList<HashMap<Integer, Integer>> trainingReviews = trainingLabelsAndReviews.getValue();

        ArrayList<Integer> validationLabels = validationLabelsAndReviews.getKey();
        ArrayList<HashMap<Integer, Integer>> validationReviews = validationLabelsAndReviews.getValue();

        ArrayList<Integer> testingLabels = testingLabelsAndReviews.getKey();
        ArrayList<HashMap<Integer, Integer>> testingReviews = testingLabelsAndReviews.getValue();

        double learningRate = 0.1;

        int vocabSize = getDictionarySize(dictInput);

        double[] parameters = new double[(vocabSize + 1)];
        for(int i= 0; i < parameters.length; i++) {
            parameters[i] = 0;
        }

        double validationNegativeLogLikelihood = 0;
        double trainingNegativeLogLikelihood = 0;
        ArrayList<Double> vNLLS = new ArrayList<>();
        ArrayList<Double> tNLLS = new ArrayList<>();
        //numbers of times to go through the data
        for(int a =1; a <= Integer.parseInt(numberOfEpochs); a++) {
            //looping through each example i in the training data
            for(int i = 0; i < trainingReviews.size(); i++) {
                singleSGDStep(parameters, learningRate, trainingReviews.get(i), trainingLabels.get(i));
            }
            trainingNegativeLogLikelihood = calculateNegativeLogLikelihood(trainingReviews, trainingLabels, parameters);
            validationNegativeLogLikelihood = calculateNegativeLogLikelihood(validationReviews, validationLabels, parameters);
            tNLLS.add(trainingNegativeLogLikelihood);
            vNLLS.add(validationNegativeLogLikelihood
            );
        }
        printToFile("trainingNLLsM1.csv", tNLLS);
        printToFile("validationNLLsM1.csv", vNLLS);

        ArrayList<Integer> predictedTrainingLabels = new ArrayList<>(trainingLabels);
        double predictedLabel = 0;
        double activation = 0;
        for(int i = 0; i < trainingReviews.size(); i++) {
            activation = dotProduct(parameters, trainingReviews.get(i));
//            activation = dotProduct(parameters, trainingReviews.get(i)) + (parameters[parameters.length-1]);
            if(activation > 0) {
                predictedLabel = 1;
            } else if (activation <= 0) {
                predictedLabel = 0;
            }
            predictedTrainingLabels.set(i, (int) predictedLabel);
        }

        double trainingError = calculateError(trainingLabels, predictedTrainingLabels);
        printLabels(predictedTrainingLabels, trainingOutput);

        ArrayList<Integer> predictedTestingLabels = new ArrayList<>(testingLabels);
        for (int i = 0; i < testingReviews.size(); i++) {
            activation = dotProduct(parameters, testingReviews.get(i));
//            activation = dotProduct(parameters, testingReviews.get(i)) + (parameters[parameters.length-1]);
            if(activation > 0) {
                predictedLabel = 1;
            } else if (activation <= 0) {
                predictedLabel = 0;
            }
            predictedTestingLabels.set(i, (int) predictedLabel);
        }

        double testingError = calculateError(testingLabels, predictedTestingLabels);
        printLabels(predictedTestingLabels, testingOutput);

        printMetrics(trainingError, testingError, metricsOutput);
    }

    private static void printToFile(String fileName, ArrayList<Double> NLL) {
        BufferedWriter writer;
        StringBuilder sb = new StringBuilder();
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            for(int epoch = 0; epoch < NLL.size(); epoch++) {
                sb.append(epoch);
                sb.append(", ");
                sb.append(NLL.get(epoch).toString());
                sb.append('\n');
            }
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void singleSGDStep(double[] parameters, double learningRate, HashMap<Integer, Integer> features, int label){
        //for the x-i, y-i data point: (i is example, j is feature)
        //theta-j equals theta-j plus (0.1 times x-i-j times (y-i minus (e^(theta transpose dot x-i)/ (1+ (theta transpose dot x-i)))))
        double thetaj = 0;
        int xij = 0;
        int j = 0;
        double bias;

        double activation = dotProduct(parameters, features);
        //

        for (Map.Entry<Integer, Integer> entry : features.entrySet()) {
            j = entry.getKey();
            xij = entry.getValue();
            thetaj = parameters[j];
            //maybe need to decide whether to add or subtract based on label minus predictedLabel: if diff>0 add; else subtract
//            thetaj += learningRate * xij * (label - (Math.exp(parameters.transpose().dot(features)) / (1 + Math.exp(parameters.transpose().dot(features)))));
            thetaj += (learningRate * xij * (label - (Math.exp(activation) / (1 + Math.exp(activation)))));
            parameters[j] = thetaj;
        }
        bias = parameters[parameters.length-1];
        bias += (learningRate * (label - (Math.exp(activation) / (1 + Math.exp(activation)))));
        parameters[parameters.length-1] = bias;
    }

    private static double calculateNegativeLogLikelihood(ArrayList<HashMap<Integer, Integer>> features, ArrayList<Integer> labels, double[] parameters) {
        //sum of (negative y-i times (theta transpose dot x-i) plus log (1 plus e ^(theta transpose dot x-i)))
        double negativeLogLikelihood = 0;
        int yi = 0;
        HashMap<Integer, Integer> xi = new HashMap<>();
        double activation;
        for(int i = 0; i < features.size(); i++) {
            activation = dotProduct(parameters, xi);
            yi = labels.get(i);
            xi = features.get(i);
//            negativeLogLikelihood+= ((-1*yi)*(parameters.transpose().dot(xi)) + Math.log(1 + Math.exp(parameters.transpose().dot(xi))));
            negativeLogLikelihood+= ((-1*yi)*(activation) + Math.log(1 + Math.exp(activation)));
        }

        return negativeLogLikelihood/features.size();
    }

    private static double calculateError(ArrayList<Integer> labels, ArrayList<Integer> predictedLabels) {
        double error = 0;
        int predictedLabel;
        int label;
        for(int i = 0; i < labels.size(); i++) {
            predictedLabel = predictedLabels.get(i);
            label = labels.get(i);
            if(predictedLabel != label) {
                error+=1;
            }
        }
        System.out.println("Number wrong is " + error + "; size is " + labels.size());
        return (error / (double)labels.size());
    }


    private static void printMetrics(Double trainError, Double testError, String metricsFile) {
        BufferedWriter writer;
        DecimalFormat df = new DecimalFormat("0.000000");
        try {
            writer = new BufferedWriter(new FileWriter(metricsFile));
            StringBuilder sb = new StringBuilder();
            sb.append("error(train): ");
            sb.append(df.format(trainError));
            sb.append("\nerror(test): ");
            sb.append(df.format(testError));
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void printLabels(ArrayList<Integer> predictedLabels, String outputFile) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            StringBuilder sb = new StringBuilder();
            for(Integer i : predictedLabels) {
                sb.append(i.toString());
                sb.append("\n");
                writer.write(sb.toString());
                sb.setLength(0);
            }
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static feature.Pair<ArrayList<Integer>, ArrayList<HashMap<Integer, Integer>>> readTSV(String fileName) {
        ArrayList<Integer> labels = new ArrayList<>();
        ArrayList<HashMap<Integer, Integer>> reviews = new ArrayList<>();

        BufferedReader reader;
        String input;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            while((input = reader.readLine()) != null) {
                HashMap<Integer, Integer> reviewData = new HashMap<>();
                String[] line = input.split("\t");
                labels.add(Integer.parseInt(line[0]));
                for(int i = 1; i < line.length; i++) {
                    String[] review = line[i].split(":");
                    reviewData.put(Integer.parseInt(review[0]), Integer.parseInt(review[1]));
                }
                reviews.add(reviewData);
            }
        } catch (NullPointerException e) {
            System.err.println("Null pointer error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
        return new feature.Pair<>(labels, reviews);
    }

    private static double dotProduct(double[] parameters, HashMap<Integer, Integer> features) {
        double result = 0.0;
        int xij = 0;
        int j = 0;
        for(Map.Entry<Integer, Integer> entry : features.entrySet()) {
            j = entry.getKey();
            xij = entry.getValue();
            result += (xij * parameters[j]);
        }
        result += parameters[parameters.length-1];
        return result;
    }

    private static int getDictionarySize(String fileName) {
        int toReturn = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            while((reader.readLine()) != null) {
                toReturn++;
            }
        } catch (NullPointerException e) {
            System.err.println("Null pointer error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
        return toReturn;
    }
}

