package paramGeneration;
import org.apache.jena.query.*;

/**
* 给定一个形参，根据语义返回一个实参集合
* @ZHQ
* @version 0.1
*/
public class DBpediaTest {
	public static void main(String[] args) {
		String queryString=
				"PREFIX  ont: <http://dbpedia.org/ontology/>"+
						//TODO:关于SPARQL语法部分还不熟悉，需要思考如何优化...是结果更准确
						"select ?y " +
						"where {?x "+"ont:city"+" ?y" +
						"} " +
						"LIMIT 1";
//				"SELECT ?p"+
//				"WHERE {"+
//				"?p rdf:type* ont:Country ."+
//				"} LIMIT 100";
				 
		//创建一个查询实例
		Query query = QueryFactory.create(queryString);
		//初始化queryExecution factory
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql",query);
		
		//TODO:有的查询能返回结果，有的返回的空结果？？但在网站上测试都是可以返回结果的。
		
		try {
		    ResultSet results = qexec.execSelect();
		    while (results.hasNext()) {
				QuerySolution qs = results.next();
				System.out.println(qs.get("?y"));
				//TODO:结果是例如：( ?Concept = <http://www.openlinksw.com/schemas/virtrdf#QuadStorage> )这样的URI
				//需要对URI结果进行进一步处理，提取单词
			}
		}
		finally {
		   qexec.close();
		   System.out.println("finished!");
		}
	}
}
