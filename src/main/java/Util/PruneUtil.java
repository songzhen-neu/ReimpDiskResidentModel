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
    public static List<Integer> prune(SampleList sampleList, float pruneRate,double threshold){
        /**
        *@Description: 通过统计各个维度发生的频率，按照频率大小，得到剪枝之后的维度
        *@Param: [sampleList, pruneRate,
         * samplePrunedSize：剪枝所用到的数据集大小]
        *@return: java.util.List<java.lang.Integer>
        *@Author: SongZhen
        *@date: 下午3:11 18-11-19
        */
        // 先遍历数据将数据每个维度出现的次数做成一个数组
        // sparseDimSize是所有数据集可能出现的维度
        int samplePrunedSize=sampleList.samplePrunedSize;
        int sparseDimSize=sampleList.sparseDimSize;
        int[] countSparseDimFreq=new int[sparseDimSize];
        Double freqThreshold;
        List<Integer> prunedSparseDim=new ArrayList<Integer>();


        for(int i=0;i<samplePrunedSize;i++){
            for(int j=0;j<sampleList.sampleList.get(i*(Global.maxSampleListSize/Global.samplePrunedSize)).cat.length;j++){
                int[] cat=sampleList.sampleList.get(i*(Global.maxSampleListSize/Global.samplePrunedSize)).cat;
                if(cat[j]!=-1){
                    countSparseDimFreq[cat[j]]++;
                }
            }
        }

        UpdateDoublesSketch updateDoublesSketch=DoublesSketch.builder().build();
        for(int i=0;i<sparseDimSize;i++){
            updateDoublesSketch.update(countSparseDimFreq[i]);
        }


        freqThreshold=updateDoublesSketch.getQuantile(1.0-pruneRate);

        // 这里表示不使用剪枝率，让大家的threshold都一样
        if(!Global.usePruneRate){
            freqThreshold=threshold;
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

    public static int getIndexOfPrunedDim(int dim,List<Integer> prunedSparseDim){
        return prunedSparseDim.indexOf(dim);
    }
}
