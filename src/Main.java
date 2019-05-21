import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws IOException {
        final int SEGMENT_COUNT = 5;
        final int FIELD_COUNT = 30;
        final int[] FLOAT_FIELDS = new int[] {0, 1, 2, 3, 8, 10, 12, 25};
        final int BAYES_FIELD_COUNT = FIELD_COUNT + FLOAT_FIELDS.length * 4;

        final int targetVariableIdx = Row.BIOPSY.idx;
        int totalCorrectlyPredictedCount = 0;

        File file = new File("NoReduction.csv");
        List<Float[]> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        br.readLine();
        while ((st = br.readLine()) != null) {
            String[] line = st.split(",");
            Float[] l = Arrays.stream(line).map(Float::valueOf).toArray(Float[]::new);
            lines.add(l);
        }
        br.close();

        for (int segment=0; segment<SEGMENT_COUNT; segment++) {

            int[] fieldPositiveCounts = new int[BAYES_FIELD_COUNT];
            int[] fieldNegativeCounts = new int[BAYES_FIELD_COUNT];
            float[] positiveProbability = new float[BAYES_FIELD_COUNT];

            int positiveRows = 0;
            int negativeRows = 0;

            int segmentSize = lines.size() / SEGMENT_COUNT;
            int testSegmentFromIdx = segment * segmentSize;
            int testSegmentToIdx = (segment + 1) * segmentSize;

            for (int i = 0; i < lines.size(); i++) {
                if (i >= testSegmentFromIdx && i < testSegmentToIdx) {
                    continue;
                }
                Float[] fields = lines.get(i);
                if (fields[targetVariableIdx] != 0) {
                    positiveRows++;
                    int bayesJ = 0;
                    for (int j = 0; j < FIELD_COUNT; j++) {
                        int finalJ = j;
                        if (IntStream.of(FLOAT_FIELDS).anyMatch(x -> x == finalJ)) {
                            fieldPositiveCounts[bayesJ + getFloatFieldIndex(fields[j])] += 1;
                            bayesJ += 4;
                        } else {
                            fieldPositiveCounts[bayesJ] += fields[j];
                        }
                        bayesJ++;
                    }
                } else {
                    negativeRows++;
                    int bayesJ = 0;
                    for (int j = 0; j < FIELD_COUNT; j++) {
                        int finalJ = j;
                        if (IntStream.of(FLOAT_FIELDS).anyMatch(x -> x == finalJ)) {
                            fieldNegativeCounts[bayesJ + getFloatFieldIndex(fields[j])] += 1;
                            bayesJ += 4;
                        } else {
                            fieldNegativeCounts[bayesJ] += fields[j];
                        }
                        bayesJ++;
                    }
                }
            }

            for (int i = 0; i < BAYES_FIELD_COUNT; i++) {
                float fieldIsTrueInPositives = (float) fieldPositiveCounts[i] / positiveRows;
                float fieldIsTrueInNegatives = (float) fieldNegativeCounts[i] / negativeRows;
                positiveProbability[i] = fieldIsTrueInPositives / (fieldIsTrueInPositives + fieldIsTrueInNegatives);
            }

            int correctlyPredictedCount = 0;

            for (int i = testSegmentFromIdx; i < testSegmentToIdx; i++) {
                Float[] fields = lines.get(i);
                float posMx = 1;
                float negMx = 1;
                int bayesJ = 0;
                for (int j = 0; j < FIELD_COUNT; j++) {
                    int finalJ = j;
                    if (IntStream.of(FLOAT_FIELDS).anyMatch(x -> x == finalJ)) {
                        if (Float.isNaN(positiveProbability[bayesJ + getFloatFieldIndex(fields[j])])) continue;
                        posMx *= positiveProbability[bayesJ + getFloatFieldIndex(fields[j])];
                        negMx *= (1 - positiveProbability[bayesJ + getFloatFieldIndex(fields[j])]);
                        bayesJ += 4;
                    } else if (fields[j] != 0) {
                        if (Float.isNaN(positiveProbability[bayesJ])) continue;
                        posMx *= positiveProbability[bayesJ];
                        negMx *= (1 - positiveProbability[bayesJ]);
                    }
                    bayesJ++;
                }
                float probability = posMx / (posMx + negMx);
                if (
                        (probability > 0.8 && fields[targetVariableIdx] == 1) ||
                        (probability <= 0.8 && fields[targetVariableIdx] == 0)
                ) {
                    correctlyPredictedCount++;
                }
            }
            totalCorrectlyPredictedCount += correctlyPredictedCount;

            System.out.println(
                    "Correctly predicted: " + correctlyPredictedCount + "/" + segmentSize +
                            " (" + ((float)correctlyPredictedCount/segmentSize) * 100 + "%)"
            );
        }
        System.out.println("Total:");
        System.out.println(
                "Correctly predicted: " + totalCorrectlyPredictedCount + "/" + lines.size() +
                        " (" + ((float)totalCorrectlyPredictedCount/lines.size()) * 100 + "%)"
        );
    }
    
    private static int getFloatFieldIndex(float value) {
        if (value < 0.2){
            return 0;
        } else if (value >= 0.2 && value < 0.4){
            return 1;
        } else if (value >= 0.4 && value < 0.6){
            return 2;
        } else if (value >= 0.6 && value < 0.8){
            return 3;
        } else if (value >= 0.8){
            return 4;
        }
        return 4;
    }
}
