package Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: CtrForBigModel
 * @description: ArrayList工具类，可以进行clone等
 * @author: SongZhen
 * @create: 2018-11-22 10:58
 */
public class ArrayListUtil {
    public static List<Integer> intListClone(List<Integer> listForward){
        List<Integer> listClone=new ArrayList<Integer>();
        for(int i=0;i<listForward.size();i++){
             listClone.add(listForward.get(i));
        }
        return listClone;
    }
}