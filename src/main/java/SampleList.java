package ParameterPartition;

import java.util.ArrayList;
import java.util.List;

public class SampleList{
    List<Sample> sampleList=new ArrayList<Sample>();
    int sparseDimSize;
    int featureSize;
    int catSize;
    int sampleListSize;
    public SampleList(){

    }
    public SampleList(int featureSize,int catSize){
        this.featureSize=featureSize;
        this.catSize=catSize;
    }
}