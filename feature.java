import java.io.*;
import java.util.*;

public class feature {
    private static ArrayList<String> dictionary = new ArrayList<>();

    public static void main(String[] args) {
//        String trainingInput = args[1];
//        String validationInput = args[2];
//        String testingInput = args[3];
//        String dictInput = args[4];
//        String formattedTrainingOuput = args[5];
//        String formattedValidationOutput = args[6];
//        String formattedTestingOutput = args[7];
//        String featureFlag = args[8];
        String trainingInput = args[0];
        String validationInput = args[1];
        String testingInput = args[2];
        String dictInput = args[3];
        String formattedTrainingOuput = args[4];
        String formattedValidationOutput = args[5];
        String formattedTestingOutput = args[6];
        String featureFlag = args[7];

        dictionary = createDictionary(dictInput);
        System.out.println("Dictionary size: " + dictionary.size());

//        HashMap<Integer, Integer> formattedTrainingReviews = new HashMap<>();
//        HashMap<Integer, Integer> formattedValidationReviews = new HashMap<>();
//        HashMap<Integer, Integer> formattedTestingReviews = new HashMap<>();
        ArrayList<HashMap<Integer, Integer>> formattedTrainingReviews = new ArrayList<>();
        ArrayList<HashMap<Integer, Integer>> formattedValidationReviews = new ArrayList<>();
        ArrayList<HashMap<Integer, Integer>> formattedTestingReviews = new ArrayList<>();

        Pair<ArrayList<Integer>, ArrayList<String>> trainingLabelsAndReviews = readTSV(trainingInput);
        ArrayList<Integer> trainingLabels = trainingLabelsAndReviews.getKey();
        ArrayList<String> trainingReviews = trainingLabelsAndReviews.getValue();
        System.out.println("Training Labels size: " + trainingLabels.size());
        System.out.println("Training Reviews Size: " + trainingReviews.size());

        Pair<ArrayList<Integer>, ArrayList<String>> validationLabelsAndReviews = readTSV(validationInput);
        ArrayList<Integer> validationLabels = validationLabelsAndReviews.getKey();
        ArrayList<String> validationReviews = validationLabelsAndReviews.getValue();
        System.out.println("Validation Labels size: " + validationLabels.size());
        System.out.println("Validation Reviews Size: " + validationReviews.size());

        Pair<ArrayList<Integer>, ArrayList<String>> testingLabelsAndReviews = readTSV(testingInput);
        ArrayList<Integer> testingLabels = testingLabelsAndReviews.getKey();
        ArrayList<String> testingReviews = testingLabelsAndReviews.getValue();
        System.out.println("Testing Labels size: " + testingLabels.size());
        System.out.println("Testing Reviews Size: " + testingReviews.size());

        if(featureFlag.equals("1")) {
            for(int i = 0; i < trainingReviews.size(); i++) {

            }
            formattedTrainingReviews = formatModel1(trainingReviews);
            formattedValidationReviews = formatModel1(validationReviews);
            formattedTestingReviews = formatModel1(testingReviews);
        }
        if(featureFlag.equals("2")) {
            formattedTrainingReviews = formatModel2(trainingReviews);
            formattedValidationReviews = formatModel2(validationReviews);
            formattedTestingReviews = formatModel2(testingReviews);
        }
        System.out.println("Formatted training reviews size: " + formattedTrainingReviews.size());
        System.out.println("Formatted validation reviews size: " + formattedValidationReviews.size());
        System.out.println("Formatted testing reviews size: " + formattedTestingReviews.size());
        writeTSV(formattedTrainingOuput, trainingLabels, formattedTrainingReviews);
        writeTSV(formattedValidationOutput, validationLabels, formattedValidationReviews);
        writeTSV(formattedTestingOutput, testingLabels, formattedTestingReviews);
    }

    private static ArrayList<String> createDictionary(String fileName) {
        ArrayList<String> toReturn = new ArrayList<>();
        BufferedReader reader;
        String input;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            while((input = reader.readLine()) != null) {
                String[] dict = input.split(" ", 2);
                toReturn.add(Integer.parseInt(dict[1]), (dict[0]));
            }
        } catch (NullPointerException e) {
            System.err.println("Null pointer error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
        return toReturn;
    }

    private static Pair<ArrayList<Integer>, ArrayList<String>> readTSV(String fileName) {
        ArrayList<Integer> labels = new ArrayList<>();
        ArrayList<String> reviews = new ArrayList<>();

        BufferedReader reader;
        String input;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            while((input = reader.readLine()) != null) {
                String[] line = input.split("\t");
                labels.add(Integer.parseInt(line[0]));
                reviews.add(line[1]);
            }
        } catch (NullPointerException e) {
            System.err.println("Null pointer error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
        return new Pair<>(labels, reviews);
    }
    private static void writeTSV(String fileName, ArrayList<Integer> labels, ArrayList<HashMap<Integer, Integer>> formattedReviews) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter((new FileWriter(fileName)));
            String s;
            HashMap<Integer, Integer> temp;
            StringBuilder sb = new StringBuilder();
            for(int line=0; line < labels.size(); line++) {
                sb.append(labels.get(line));
//                for(Pair<Integer, Integer> p : formattedReviews.get(i)) {
//                    sb.append(p.getKey().toString());
//                    sb.append(':');
//                    sb.append(p.getValue().toString());
//                    sb.append('\t');
//                }
                temp = formattedReviews.get(line);
                temp.forEach((k,v) -> {
                    sb.append('\t');
                    sb.append(k);
                    sb.append(':');
                    sb.append(v);
                });
                sb.append('\n');
                s = sb.toString();
//                writer.append(s);
//                writer.flush();
                writer.write(s);
                sb.setLength(0);
            }
            writer.close();
            System.out.println("Finished writing TSV " + fileName);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static int lookup(String word) {
        return dictionary.indexOf(word);
    }

    private static ArrayList<HashMap<Integer, Integer>> formatModel1(ArrayList<String> reviews) {
        ArrayList<HashMap<Integer, Integer>> toReturn = new ArrayList<>();
        for(int i=0; i<reviews.size(); i++) {
            toReturn.add(null);
        }
        for(int line = 0; line < reviews.size(); line++) {
            HashMap<Integer, Integer> temp = new HashMap<>();
            ArrayList<String> words = new ArrayList<>(Arrays.asList(reviews.get(line).split(" ")));
            for(String word : words) {
                if (dictionary.contains(word)) {
                    temp.put(lookup(word), 1);
                }
            }
            toReturn.set(line, temp);
        }
        return toReturn;
    }

    private static ArrayList<HashMap<Integer, Integer>> formatModel2(ArrayList<String> reviews) {
        ArrayList<HashMap<Integer, Integer>> toReturn = new ArrayList<>();
        for(int i=0; i<reviews.size(); i++) {
            toReturn.add(null);
        }
        for(int line = 0; line < reviews.size(); line++) {
            HashMap<Integer, Integer> temp = new HashMap<>();
            ArrayList<String> words = new ArrayList<>(Arrays.asList(reviews.get(line).split(" ")));
            for (String word : words) {
                if (dictionary.contains(word)) {
//                    toReturn.merge(lookup(word), 1, Integer::sum);
                    temp.merge(lookup(word), 1, Integer::sum);
                }
            }
            temp.entrySet().removeIf(entries->entries.getValue().compareTo(3) > 0);
            temp.replaceAll((k,v) -> 1);

            toReturn.set(line, temp);
            //            toReturn.get(line).forEach((k, v) -> {
//                if (v >= 4) {
//                    toReturn.remove(k);
//                }
//                else {
//                    toReturn.replace(k, newV);
//                }
//            });
        }
//        for(int line = 0; line < toReturn.size(); line++) {
//            HashMap<Integer, Integer> temp2 = toReturn.get(line);
//            for(Map.Entry<Integer, Integer> entry : temp2.entrySet()) {
//                if(entry.getValue() >= 4) {
//                    temp2.remove(entry.getKey());
//                } else {
//                    temp2.replace(entry.getKey(), 1);
//                }
//            }
//            toReturn.set(line, temp2);
//        }
        return toReturn;

    }

    public static class Pair<F, S> extends java.util.AbstractMap.SimpleImmutableEntry<F, S> {
        public Pair(F f, S s) {
            super(f, s);
        }
    }
}
