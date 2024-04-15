import com.sun.xml.internal.ws.util.InjectionPlan

object Statistics {
  val labelToTable:Map[String,String]=Map(
    "comment"->"src/main/resources/ldbc/nodes/comment-output.csv",
    "forum"->"src/main/resources/ldbc/nodes/forum-output.csv",
    "organisation"->"src/main/resources/ldbc/nodes/organisation-output.csv",
    "person"->"src/main/resources/ldbc/nodes/person-output.csv",
    "place"->"src/main/resources/ldbc/nodes/place-output.csv",
    "post"->"src/main/resources/ldbc/nodes/post-output.csv",
    "tagclass"->"src/main/resources/ldbc/nodes/tagclass-output.csv",
    "tag"->"src/main/resources/ldbc/nodes/tag-output.csv",
  )
  val labelNum:Map[String,Int]=Map(
    "comment" -> 1982324,
    "forum" -> 90137,
    "organisation" -> 7955,
    "person" -> 9916,
    "place" ->1460,
    "post" -> 1007584,
    "tagclass" -> 71,
    "tag" -> 16080,
    "all"->3115527
  )
  val index:Map[String,Set[String]]=Map(
    "comment" -> Set("browserUsed","id"),
    "forum" -> Set("id"),
    "organisation" -> Set("type"),
    "person" ->Set("id"),
    "place" ->Set("type"),
    "post" ->Set("browserUsed","language")
  )
  val costFactor:Map[String, Double]=Map(
    "scan"->1.0,
    "iseek"->0.1,
    "shj"->0.1,
    "nlj"->1.0
  )
}

trait Plan
case class AllNodeScan(node:Node) extends Plan
case class NodeByLabelScan(node:Node) extends Plan
case class NodeIndexSeek(property:String,node:Node) extends Plan
case class SortedHashJoin(startSet:Set[Node],endSet: Set[Node],connection: (Node, Node, Edge)) extends Plan
case class NestedLoopsJoin(startSet:Set[Node],endSet: Set[Node],connection: (Node, Node, Edge)) extends Plan