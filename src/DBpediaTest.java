import org.apache.jena.query.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* 给定一个形参，根据语义返回一个实参集合
* @ZHQ
* @version 0.1
*/
public class DBpediaTest {
	public static void main(String[] args) {
		String name = "startDate";
		String queryString =
				"PREFIX prop: <http://dbpedia.org/property/>" +
						"PREFIX res: <http://dbpedia.org/resource/>" +
						"PREFIX ont: <http://dbpedia.org/ontology/>" +
						"PREFIX category: <http://dbpedia.org/resource/Category:>" +
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
						"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
						"PREFIX geo: <http://www.georss.org/georss/>" +

						//TODO:关于SPARQL语法部分还不熟悉，需要思考如何优化...是结果更准确
						"select ?y " +
						"where {?x ont:" + name + " ?y." +
						"} " +
						"LIMIT 100";

		//创建一个查询实例
		Query query = QueryFactory.create(queryString);
		//初始化queryExecution factory
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				System.out.println(qs.toString());
				if (!processQueryLineTest1(qs.toString()).equals("")) {
					System.out.println(processQueryLineTest1(qs.toString()));
				}
				//匹配失败
				else if (!processQueryLineTest2(qs.toString()).equals("")){
					System.out.println(processQueryLineTest2(qs.toString()));
				}
				//TODO:结果是例如：( ?Concept = <http://www.openlinksw.com/schemas/virtrdf#QuadStorage> )这样的URI
				//需要对URI结果进行进一步处理，提取单词

			}
		} finally {
			qexec.close();
			System.out.println("finished!");
		}
	}
	//提取 ?y=" " 格式中的instance
	public static String processQueryLineTest1(String line){
		String pattern = "(?<=\").*?(?=\")";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(line);
		while (m.find()){
			return m.group(0);

		}
		return "";
	}
	public static String processQueryLineTest2(String line){
		String pattern = "(?<=resource/).*?(?=>)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(line);
		while (m.find()){
			return m.group(0);

		}
		return "";
	}
}
