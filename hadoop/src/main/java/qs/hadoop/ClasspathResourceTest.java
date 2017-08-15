package qs.hadoop;

public class ClasspathResourceTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// java -cp `pwd`/*:/apache/hbase/conf qs.hadoop.ClasspathResourceTest 
		System.out.println(ClasspathResourceTest.class.getClassLoader().getResource("hbase-site.xml"));
		System.out.println(ClasspathResourceTest.class.getResource("hbase-site.xml"));
	}

}
