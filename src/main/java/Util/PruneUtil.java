package Util;

import Global.Global;
import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.SampleList;
import com.yahoo.sketches.quantiles.DoublesSketch;
import com.yahoo.sketches.quantiles.UpdateDoublesSketch;

import java.util.ArrayList;
import java.util.List;

public class PruneUtil {
    public static List<Integer> prune(SampleList sampleList, float pruneRate){
        /*先遍历数据将数据每个维度出现的次数做成一个数组*/
        int sparseDimSize=sampleList.sparseDimSize;
        int[] countSparseDimFreq=new int[sparseDimSize];
        List<Integer> prunedSparseDim=new ArrayList<Integer>();


        for(int i=0;i<sampleList.sampleListSize;i++){
            for(int j=0;j<sampleList.catSize;j++){
                int[] cat=sampleList.sampleList.get(i).cat;
                if(cat[j]!=-1){
                    countSparseDimFreq[cat[j]]++;
                }
            }
        }

        UpdateDoublesSketch updateDoublesSketch=DoublesSketch.builder().build();
        for(int i=0;i<sparseDimSize;i++){
            updateDoublesSketch.update(countSparseDimFreq[i]);
        }


        Double freqThreshold=updateDoublesSketch.getQuantile(1.0-pruneRate);

        // 这里表示不使用剪枝率，让大家的threshold都一样
        if(!Global.usePruneRate){
            freqThreshold=Global.freqThreshold;
        }


        System.out.println("threshold:"+freqThreshold);
        for(int i=0;i<countSparseDimFreq.length;i++){
            if(countSparseDimFreq[i]>=freqThreshold){
                prunedSparseDim.add(i);
            }
        }

        return prunedSparseDim;
    }

    public static List<Integer> removeOnceItemOfBPL(PartitionList bestPartitionList){
        List<Integer> prunedDimWithNoOnceItem=new ArrayList<Integer>();
        for(Partition partition:bestPartitionList.partitionList){
            if(partition.partition.size()>(Global.minPartitionSize-1)){
                for(int cat:partition.partition){
                    prunedDimWithNoOnceItem.add(cat);
                }
            }
        }

        return prunedDimWithNoOnceItem;
    }
}
