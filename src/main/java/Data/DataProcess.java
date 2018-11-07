package Data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ParaStructure.Sample.Sample;
import ParaStructure.Sample.SampleList;

public class DataProcess {


    public static SampleList readData(String fileName, int featureSize, int catSize) throws  IOException{ //这个库feature_size是10，cat_size是12
        BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String line=null;
        line =br.readLine();
        Map<String,Integer> catMap=new HashMap<String, Integer>();
        SampleList sampleList=new SampleList();

        int car_index=0;
        Runtime rt=Runtime.getRuntime();
        long freemem=rt.freeMemory()/1024/1024;

        /*以下是离散特征在前连续特征在后的代码*/

//        while((line = br.readLine())!=null){
//            String[] lineSplit=line.split(",");  //在调试的时候，由于这个在下文没有调用，所有就没有给空间存储，其实就相当于废代码不编译
//            float[] feature=new float[feature_size];
//            int[] cat= new int[cat_size];
//            boolean click= Boolean.parseBoolean(lineSplit[1]);
//
//            for(int i=2;i<2+cat_size;i++){  //为category feature构建sparse feature
//                if(catMap.containsKey(lineSplit[i])){
//                    cat[i-2]=catMap.get(lineSplit[i]);
//                }
//                else{
//                    cat[i-2]=car_index;
//                    catMap.put(lineSplit[i],car_index);
//                    car_index++;
//                }
//            }
//
//            for(int i=2+cat_size;i<2+cat_size+feature_size;i++){
//                feature[i-2-cat_size]=Float.parseFloat(lineSplit[i]);
//            }

        /*下面是连续特征在前，离散特征在后的代码*/
        while((line = br.readLine())!=null){
            String[] lineSplit=line.split(",");  //在调试的时候，由于这个在下文没有调用，所有就没有给空间存储，其实就相当于废代码不编译
            float[] feature=new float[featureSize];
            int[] cat= new int[catSize];

            /*给cat初始值全为-1，来表示上来所有cat属性都为missing value*/
            for(int i=0;i<catSize;i++){
                cat[i]=-1;
            }

            boolean click= Boolean.parseBoolean(lineSplit[1]);

            for(int i=2;i<2+featureSize;i++){
                if(lineSplit[i].equals("")){
                    feature[i-2]=0f;    // 缺失值用0代替
                }
                else {
                    feature[i-2]=Float.parseFloat(lineSplit[i]);
                }
            }

            for(int i=2+featureSize;i<lineSplit.length;i++){   // 这里使用lineSplit的length而不是2+feature_size+cat_size的原因是，后面为空的元素，split函数不会将其赋值给string数组。所以会越界
                if(lineSplit[i].equals("")){    // 这里考虑到中间的特征为空的情况
                    cat[i-2-featureSize]=-1;
                }
                else{
                    if(catMap.containsKey(lineSplit[i])){
                        cat[i-2-featureSize]=catMap.get(lineSplit[i]);
                    }
                    else{
                        cat[i-2-featureSize]=car_index;
                        catMap.put(lineSplit[i],car_index);
                        car_index++;
                    }
                }

            }

            Sample sample=new Sample(feature,cat,click);

            sampleList.sampleList.add(sample);
            sampleList.sparseDimSize=car_index;



        }
        sampleList.catSize=catSize;
        sampleList.featureSize=featureSize;
        sampleList.sampleListSize=sampleList.sampleList.size();
//        System.out.println(catMap.size());
        return sampleList;
    }


    public static SampleList linerNormalization(SampleList sampleList){  // 这里标准化是为了提升训练精度。那么也就是(a-amin)/(amax-amin)，一定落在(0,1)区间内
        /*首先应该获取每一个属性栏最大和最小的参数，这里应该只对feature属性来做，因为cat属性只表示这意味出不出现，不表示具体值
        * 规范化这里究竟是否要真的使用max是有争议的，因为，可能噪声导致了部分数据值异常大，而影响了整体精度
        *
        *
        * */
        int featureSize=sampleList.featureSize;
        float[] max=new float[featureSize];
        float[] min=new float[featureSize];
        float[] dis=new float[featureSize];


        for(int i=0;i<featureSize;i++){
            max[i]= Float.MIN_VALUE;
            min[i]=Float.MAX_VALUE;
        }


        for(int i=0;i<sampleList.sampleList.size();i++){
            for(int j=0;j<featureSize;j++){
                float[] feature=sampleList.sampleList.get(i).feature;
                if(feature[j]>max[j]){
                    max[j]=feature[j];

                }
                if(feature[j]<min[j]){
                    min[j]=feature[j];
                }
            }
//            System.out.println(sampleList.sampleList.get(i).feature[1]);
        }

        for(int i=0;i<featureSize;i++){
            dis[i]=max[i]-min[i];
        }

        for(int i=0;i<sampleList.sampleList.size();i++){
            for(int j=0;j<featureSize;j++){
                float[] feature=sampleList.sampleList.get(i).feature;
                feature[j]=(feature[j]-min[j])/dis[j];
            }
        }

        return sampleList;
    }


}
