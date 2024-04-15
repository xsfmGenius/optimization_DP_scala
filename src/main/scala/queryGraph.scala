import scala.collection.mutable

// 边
class Edge(val name:String,val source: Node, val target: Node, val label: Option[String]=None) {
  private val properties: mutable.Map[String, Any] = mutable.HashMap.empty
  //获取和添加属性
  def getProperty(key: String): Option[Any] = properties.get(key)
  def setProperty(key: String, value: Any): Unit = properties.update(key, value)
  def getProperties: mutable.Map[String, Any] = properties
}

// 节点
class Node(val name:String,val label: Option[String] = None) {
  private val properties: mutable.Map[String, Any] = mutable.HashMap.empty
  //获取和添加属性
  def getProperty(key: String): Option[Any] = properties.get(key)
  def setProperty(key: String, value: Any): Unit = properties.update(key, value)
  def getProperties: mutable.Map[String, Any] = properties
}

// 图
class PropertyGraph {
  private val adjacencyList: mutable.Map[Node, mutable.Set[Edge]] = mutable.HashMap.empty
  private val reverseAdjacencyList: mutable.Map[Node, mutable.Set[Edge]] = mutable.HashMap.empty
  //添加节点和边
  def addNode(node: Node): Unit = {
    adjacencyList.getOrElseUpdate(node, mutable.HashSet.empty)
    reverseAdjacencyList.getOrElseUpdate(node, mutable.HashSet.empty)
  }
  def addEdge(edge: Edge): Unit = {
    adjacencyList.getOrElseUpdate(edge.source, mutable.HashSet.empty).add(edge)
    reverseAdjacencyList.getOrElseUpdate(edge.target, mutable.HashSet.empty).add(edge)
  }
  //获取出边和入边
  def getOutEdges(node: Node): mutable.Set[Edge] = adjacencyList.getOrElse(node, mutable.HashSet.empty)
  def getInEdges(node: Node): mutable.Set[Edge] = reverseAdjacencyList.getOrElse(node, mutable.HashSet.empty)
  // 获取邻居节点
  def getNeighbors(node: Node): mutable.Set[Node] = {
    val neighbors = mutable.HashSet.empty[Node]
    for (edge <- getOutEdges(node)) {
      neighbors.add(edge.target)
    }
    neighbors
  }
  def getReverseNeighbors(node: Node): mutable.Set[Node] = {
    val reverseNeighbors = mutable.HashSet.empty[Node]
    for (edge <- getInEdges(node)) {
      reverseNeighbors.add(edge.source)
    }
    reverseNeighbors
  }
  //获取连接两点的边
  def getEdge(start: Node, end: Node): Option[Edge] = {
    for (edge <- getOutEdges(start)) {
      if (edge.target == end) {
        return Some(edge)
      }
    }
    None
  }
  //获取所有节点
  def getAllNodes: Set[Node] = adjacencyList.keys.toSet
}
