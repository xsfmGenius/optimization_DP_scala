object Main {
  def main(args: Array[String]): Unit = {
    //----构造查询图----
    //1.short/6
    //节点
    //    val m = new Node("m",Some("comment"))
    //    m.setProperty("id","702061584302086")
    //    val p=new Node("p",Some("post"))
    //    val f=new Node("f",Some("forum"))
    ////    val mod=new Node("mod",Some("person"))
    //    val mod=new Node("mod")
    //    //边
    //    val edgeMP=new Edge("edgeMP",m,p,Some("replyOf"))
    //    val edgeFP=new Edge("edgeFP",f,p,Some("containerOf"))
    //    val edgeFM=new Edge("edgeFM",f,mod,Some("hasModerator"))
    //    //图
    //    val query=new PropertyGraph()
    //    query.addNode(m)
    //    query.addNode(p)
    //    query.addNode(f)
    //    query.addNode(mod)
    //    query.addEdge(edgeMP)
    //    query.addEdge(edgeFP)
    //    query.addEdge(edgeFM)

    //2.short/5
    //    val m = new Node("m", Some("comment"))
    //    m.setProperty("id", "702061584302086")
    //    val p = new Node("p", Some("person"))
    //    val edgeMP=new Edge("edgeMP",m,p,Some("hasCreator"))
    //    val query=new PropertyGraph()
    //    query.addNode(m)
    //    query.addNode(p)
    //    query.addEdge(edgeMP)

    //3.short/4
    val m = new Node("m", Some("comment"))
    m.setProperty("id", "702061584302086")
    val query=new PropertyGraph()
    query.addNode(m)

    //----动态规划查询优化
    val dy=new DynamicProgramming(query)
    dy.getBestPlan();

  }
}
