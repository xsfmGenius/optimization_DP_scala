import scala.collection.mutable
import scala.io.Source
import scala.util.Random
import scala.math.BigDecimal

//用来记录查询计划数据
case class Record(var plan:Plan, var cost:BigDecimal, var cardinal:Long)

class DynamicProgramming (val query:PropertyGraph){
  private val records: mutable.Map[Set[Node], Record] = mutable.HashMap.empty
  //动态规划
  def getBestPlan(): Unit = {
    //获取单个节点最优计划
    val nodes = query.getAllNodes
    nodes.foreach { node =>
      records.put(Set(node), AccessPlans(node))
    }
    //println(records)
    //获取最优连接顺序
    for (i <- 2 to nodes.size) {
      nodes.subsets(i).foreach { set =>
        val joinPlan = mergePlans(set)
        if (joinPlan.plan!=null) {
          records.put(set,joinPlan)
        }
      }
    }
    print(records(nodes))
    //println(records)
  }

  //单个节点最优计划
  def AccessPlans(node: Node): Record = {
    if (node.label.isDefined) {
      node.getProperties.foreach { case (key, value) =>
        //有索引，NodeIndexSeek
        if (Statistics.index(node.label.get).contains(key)) {
          val cost:BigDecimal = Statistics.labelNum(node.label.get) * Statistics.costFactor("iseek")
          val plan = NodeIndexSeek(key, node)
          val cardinal: Long = Sampling(node.label, node.getProperties)
          return Record(plan, cost, cardinal)
        }
      }
      //有label无索引，NodeByLabelScan
      val cost: BigDecimal = Statistics.labelNum(node.label.get) * Statistics.costFactor("scan")
      val plan = NodeByLabelScan(node)
      val cardinal: Long = Sampling(node.label, node.getProperties)
      Record(plan, cost, cardinal)
    }
    //无label，AllNodeScan
    else {
      val cost: BigDecimal = Statistics.labelNum("all") * Statistics.costFactor("scan")
      val plan = AllNodeScan(node)
      val cardinal: Long = Sampling(node.label, node.getProperties)
      Record(plan, cost, cardinal)
    }
  }

  //多个节点最优计划
  def mergePlans(nodeSet: Set[Node]):Record= {
    var cost: BigDecimal = BigDecimal(Long.MaxValue)
    var plan: Plan = null
    var cardinal: Long = Long.MaxValue
    nodeSet.subsets.foreach { startSet =>
      val endSet = nodeSet -- startSet
      if (records.contains(startSet) && records.contains(endSet)) {
        //println(startSet,endSet)
        val connections = checkConnection(startSet, endSet, query)
        connections.foreach {connection=>
          val SortedHashJoinCost:BigDecimal = records(startSet).cardinal + records(endSet).cardinal +Statistics.costFactor("shj") * records(startSet).cardinal * records(endSet).cardinal+records(startSet).cost+records(endSet).cost
          val NestedLoopsJoinCost:BigDecimal = records(startSet).cardinal * records(endSet).cardinal*Statistics.costFactor("nlj")+records(startSet).cost+records(endSet).cost
          if(NestedLoopsJoinCost < cost||SortedHashJoinCost<cost){
            if(SortedHashJoinCost<NestedLoopsJoinCost){
              cost=SortedHashJoinCost
              plan=SortedHashJoin(startSet,endSet,connection)
              cardinal=joinEstimation(startSet,endSet,connection)
            }
            else{
              cost = NestedLoopsJoinCost
              plan = NestedLoopsJoin(startSet, endSet, connection)
              cardinal = joinEstimation(startSet, endSet, connection)
            }
          }
        }
      }
    }
    Record(plan, cost, cardinal)
  }

  //检查有无连接
  def checkConnection(startSet: Set[Node], endSet: Set[Node], query: PropertyGraph):Set[(Node,Node,Edge)]={
    val connections=mutable.Set[(Node,Node,Edge)]()
    startSet.foreach{startNode=>
      val neighbors=query.getNeighbors(startNode)
      endSet.foreach{endNode=>
        if(neighbors.contains(endNode)) {
          connections+= ((startNode, endNode, query.getEdge(startNode, endNode).get))
        }
      }
    }
    connections.toSet
  }

  //抽样，基数估计
  def Sampling(label: Option[String]=None,properties: mutable.Map[String, Any]):Long={
    //确定标签（文件）
    if(label.isDefined){
      //不确定属性,返回文件行数
      if(properties.isEmpty){
        val csvFilePath=Statistics.labelToTable(label.get)
        //        var lineCount=Source.fromFile(csvFilePath).getLines().length
        //        return lineCount-1
        return Statistics.labelNum(label.get)
      }
      //确定属性
      else{
        val csvFilePath = Statistics.labelToTable(label.get)
        val lines = Source.fromFile(csvFilePath).getLines()
        val headers = lines.next().split(",").toList
        //包含所有属性，抽样
        if (properties.keys.forall(headers.contains)) {
          val num = if (Statistics.labelNum(label.get) < 2000) Statistics.labelNum(label.get) else Statistics.labelNum(label.get) / 10
          val sample=Random.shuffle(lines.toList).take(num)
          val matchingLines=sample.count { line =>
            val values = line.split(",")
            properties.forall { case (key, value) =>
              values(headers.indexOf(key)) == value.toString
            }
          }
          return if (Statistics.labelNum(label.get) < 2000) Math.max(matchingLines,1) else Math.max(matchingLines*10,1)
        }
        //不包含所有属性，返回0
        else{
          return 0
        }
      }
    }
    //不确定标签（文件），需要搜索所有文件
    else{
      if(properties.isEmpty){
        return Statistics.labelNum("all")
      }
      else return Statistics.labelNum("all")/100
    }
  }

  //连接基数估计
  def joinEstimation(startSet: Set[Node],endSet: Set[Node],connection: (Node, Node, Edge)):Long={
    val startNum=if(connection._1.label.isDefined) Statistics.labelNum(connection._1.label.get) else Statistics.labelNum("all")
    val endNum=if(connection._2.label.isDefined) Statistics.labelNum(connection._2.label.get) else Statistics.labelNum("all")
    val estimation1=records(startSet).cardinal*(endNum.toDouble/startNum)
    val estimation2=records(endSet).cardinal*(startNum.toDouble/endNum)
    math.min((estimation1+1).toInt,(estimation2+1).toInt)
  }
}
