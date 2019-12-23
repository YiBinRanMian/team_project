package paramGeneration;

import java.util.ArrayList;
import java.util.Collection;

public class GenerateParam {
public static Collection<String> coll = new ArrayList<String>();
	
	public static void init() {
		coll.add("get");
		coll.add("list");
		coll.add("string");
		coll.add("int");
		coll.add("start");
		coll.add("end");

	}
	
    public static String generateParameter (String param) {
    	init();//初始化一些数据
    	
    	ArrayList<String> keyWords = getKeyWord(param);
    	//TODO: 返回第一个值
    	return keyWords.get(0);
    }
    
    
    /**
     * 对函数名中的关键词进行提取，例如getCurrencyList，则提取Currency
     * @param funcName: 函数名
     * @return resWords: 函数中的一些关键word，一般为名词，仍保持首字母大写。
     */
    public static ArrayList<String> getKeyWord(String funcName) {
        String[] words = funcName.split("(?<!^)(?=[A-Z])");  //以大写字母分隔，可处理首字母小写
        ArrayList<String> resWords = new ArrayList<String>();
        for(int i = 0 ;i < words.length; i ++){
        	//先将所有单词小写，再将单词进行过滤（例如get，list等等...）
        	if(coll.contains(words[i].toLowerCase()) || words[i].length()<=1) {
        		//do nothing
        	}
        	else {  //将未过滤的单词保存下来。
        		resWords.add(words[i]);
        	}
        }
    	return resWords;
    }
    
    

}
