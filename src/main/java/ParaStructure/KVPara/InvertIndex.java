package ParaStructure.KVPara;

import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.Sample;

import java.util.ArrayList;
import java.util.List;

public class InvertIndex {
    public List<String> invertIndex=new ArrayList<String>();

    public static InvertIndex buildIvertIndex(Sample sample, PartitionList partitionList){
        int[] cat=sample.cat;
        InvertIndex invertIndex=new InvertIndex();
        for(int i=0;i<cat.length;i++){
            String index= getIndexOfDim(cat[i],partitionList);
            if(index.equals("s-1")==false){
                invertIndex.invertIndex.add(getIndexOfDim(cat[i],partitionList));
            }
        }
        return invertIndex;
    }

    public static String getIndexOfDim(int cat,PartitionList partitionList){

        for(int j=0;j<partitionList.partitionList.size();j++){
            Partition partition=partitionList.partitionList.get(j);
            if(partition.partition.contains(cat)){
                return "c"+j;
            }
        }

        return "s"+cat;

    }
}
