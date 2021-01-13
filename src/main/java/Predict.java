import org.datavec.image.loader.ImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.common.util.ArrayUtil;
import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Predict {

  private final int WIDTH;
  private final int HEIGHT;

  private MultiLayerNetwork model;
  private NativeImageLoader loader;

  public Predict(String path, int width, int height) {
    WIDTH = width;
    HEIGHT = height;
    try {
      model = KerasModelImport.importKerasSequentialModelAndWeights(path);
      loader = new NativeImageLoader(HEIGHT, WIDTH);
    }
    catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
      e.printStackTrace();
    }
  }

  public double predict(String imgPath) {
    INDArray features = null;
    try {
      features = loader.asMatrix(new File(imgPath));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    if (features == null) {
      return -1.0;
    }
    double[][][][] raw = to4DDoubles(features);
    double[][][][] convRaw = new double[1][HEIGHT][WIDTH][3];
    for (int w = 0; w < 100; w++) {
      for (int h = 0; h < 100; h++) {
        for (int c = 0; c < 3; c++) {
          convRaw[0][h][w][c] = raw[0][2 - c][h][w];
        }
      }
    }
    double[] flat = ArrayUtil.flattenDoubleArray(convRaw);
    for (int i = 0; i < flat.length; i++) {
      flat[i] /= 255.0;
    }
    INDArray converted = Nd4j.create(flat, 1, 100, 100, 3);
    return model.output(converted).getDouble(0);
  }

  private double[][][][] to4DDoubles(INDArray source) {
    long[] shape = source.shape();
    int[] dims = castLongsToInts(shape);
    double[][][][] arr = new double[dims[0]][dims[1]][dims[2]][dims[3]];
    NdIndexIterator iter = new NdIndexIterator(shape);
    while (iter.hasNext()) {
      long[] inds = iter.next();
      int[] i = castLongsToInts(inds);
      arr[i[0]][i[1]][i[2]][i[3]] = source.getDouble(inds);
    }
    return arr;
  }

  private int[] castLongsToInts(long[] source) {
    int[] arr = new int[source.length];
    for (int i = 0; i < source.length; i++) {
      arr[i] = (int) source[i];
    }
    return arr;
  }

}
