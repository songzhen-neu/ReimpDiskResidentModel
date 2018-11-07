package ParaStructure.Sample;

import java.util.ArrayList;
import java.util.List;

public class SampleList{
    public List<Sample> sampleList=new ArrayList<Sample>();
    public int sparseDimSize;
    public int featureSize;
    public int catSize;
    public int sampleListSize;
    public SampleList(){

    }
    public SampleList(int featureSize,int catSize){
        this.featureSize=featureSize;
        this.catSize=catSize;
    }
}