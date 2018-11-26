package KMeansCluster;

import Global.Global;
import ParaStructure.Sample.Sample;
import ParaStructure.Sample.SampleList;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: CtrForBigModel
 * @description: 这个类的每一个元素都表示一个cat属性
 * @author: SongZhen
 * @create: 2018-11-19 17:03
 */
public class CatAsRowList {
    public List<CatAsRow> catAsRowList=new ArrayList<CatAsRow>();


    public static CatAsRowList getCatAsRowMatrix(SampleList sampleList, List<Integer> prunedSparseDim,int samplePrunedSize){
        /**
        *@Description: 获取以cat属性为行的这样一个矩阵
        *@Param: [sampleList, prunedSparseDim]
        *@return: KMeansCluster.CatAsRowList
        *@Author: SongZhen
        *@date: 下午5:13 18-11-19
        */

        // 初始化catAsRowList，他元素的数量和剪枝后的稀疏维度的数量是一样的
        CatAsRowList catAsRowList=new CatAsRowList();
        for(int i=0;i<prunedSparseDim.size();i++){
            CatAsRow catAsRow=new CatAsRow();
            catAsRowList.catAsRowList.add(catAsRow);
        }

        for(int i=0;i<samplePrunedSize;i++){
            // 这句话表示，每隔这么多采样一次，均匀采样
            Sample sample=sampleList.sampleList.get(i*(Global.maxSampleListSize/Global.samplePrunedSize));
            for(int j=0;j<sample.cat.length;j++){
                int[] cat=sample.cat;
                // 这条语句的含义是：添加第一条数据的id=1到catAsRow里的第cat[j]维度,这里有一个映射，将维度映射到0开始的连续整数维度中
                if(prunedSparseDim.contains(cat[j])){
                    int index=prunedSparseDim.indexOf(cat[j]);
                    catAsRowList.catAsRowList.get(index).catAsRow.add(i);
                }

            }
        }

        return  catAsRowList;

    }
}