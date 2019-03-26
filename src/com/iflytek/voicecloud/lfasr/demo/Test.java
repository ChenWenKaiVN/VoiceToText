package com.iflytek.voicecloud.lfasr.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class Test {
	
	public static void main(String[] args) {
		String str = "[{\"bg\":\"0\",\"ed\":\"2180\",\"onebest\":\"科大讯飞是中国最大！\",\"si\":\"0\",\"speaker\":\"0\","
				+ "\"wordsResultList\":[{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"6\",\"wordEd\":\"114\",\"wordsName\":"
				+ "\"科大讯飞\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"118\",\"wordEd\":\"147\",\"wordsName\""
				+ ":\"是\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"148\",\"wordEd\":\"193\",\"wordsName\":\"中国\","
						+ "\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"194\",\"wordEd\":\"213\",\"wordsName\":\"最\","
						+ "\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"214\",\"wordEd\":\"218\",\"wordsName\":\"大\","
						+ "\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"0.0000\",\"wordBg\":\"218\",\"wordEd\":\"218\",\"wordsName\":\"！\","
						+ "\"wp\":\"p\"},{\"alternativeList\":[],\"wc\":\"0.0000\",\"wordBg\":\"218\",\"wordEd\":\"218\",\"wordsName\":\"\","
						+ "\"wp\":\"g\"}]},{\"bg\":\"2190\",\"ed\":\"3080\",\"onebest\":\"的智能。\",\"si\":\"1\",\"speaker\":\"0\","
						+ "\"wordsResultList\":[{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"15\",\"wordEd\":\"42\","
						+ "\"wordsName\":\"的\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"47\",\"wordEd\":\"89\","
						+ "\"wordsName\":\"智能\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"0.0000\",\"wordBg\":\"89\",\"wordEd\":\"89\","
						+ "\"wordsName\":\"。\",\"wp\":\"p\"},{\"alternativeList\":[],\"wc\":\"0.0000\",\"wordBg\":\"89\",\"wordEd\":\"89\","
						+ "\"wordsName\":\"\",\"wp\":\"g\"}]},{\"bg\":\"3090\",\"ed\":\"4950\",\"onebest\":\"语音技术提供商，\",\"si\":\"2\","
						+ "\"speaker\":\"0\",\"wordsResultList\":[{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"4\",\"wordEd\":\"46\","
						+ "\"wordsName\":\"语音\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"47\",\"wordEd\":\"92\","
						+ "\"wordsName\":\"技术\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"1.0000\",\"wordBg\":\"93\",\"wordEd\":\"164\","
						+ "\"wordsName\":\"提供商\",\"wp\":\"n\"},{\"alternativeList\":[],\"wc\":\"0.0000\",\"wordBg\":\"164\",\"wordEd\":\"164\","
						+ "\"wordsName\":\"，\",\"wp\":\"p\"}]}]";
		System.out.println(getFinalResult(str));
	}

	public static String getFinalResult(String data){
		
		JSONArray ja = JSONArray.parseArray(data);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<ja.size(); i++){
			//System.out.println(ja.get(i));			
			sb.append(JSON.parseObject(ja.get(i).toString()).get("onebest"));
			//System.out.println(JSON.parseObject(ja.get(i).toString()).get("onebest"));
		}
		return sb.toString();
	}
}
