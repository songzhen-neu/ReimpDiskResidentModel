import java.io.IOException;

public class LogisticRegression {
    static int featureSize= 13;
    static int catSize= 26;
    static int sparseDimSize=0;


    public static void main(String[] args) throws IOException {
        String fileName="data/10000.txt";
        SampleList sampleList=new SampleList();
        sampleList=DataProcess.readData(fileName,featureSize,catSize); //用来读取train.csv的数据
        ModelPartition.modelPartition(sampleList);
        sampleList=DataProcess.linerNormalizition(sampleList);
    }
}
