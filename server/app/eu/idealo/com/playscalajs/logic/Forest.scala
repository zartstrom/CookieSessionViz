package eu.idealo.com.playscalajs.logic


import scala.collection.immutable.Seq
import scala.collection.mutable
// import cats.Traverse

object Forest {
  case class DiEdge[N](from: N, to: N)

  // DRTree = Directed Rooted Tree
  case class DRTree[N](node: N, children: List[DRTree[N]]) { // extends Traverse[DRTree] {
    def map[M](f: N => M): DRTree[M] = {
      DRTree(f(node), children.map(_.map(f)))
    }

    lazy val height: Int = {
      children match {
        case Nil => 0
        case cs  => 1 + cs.map(_.height).max
      }
    }

    lazy val width: Int = math.max(0, children.length - 1) + children
      .map(_.width)
      .sum

    def twoStepMap[M](f: (N, List[DRTree[M]]) => M): DRTree[M] = {
      val childrenM = children.map(_.twoStepMap(f))
      DRTree(f(node, childrenM), childrenM)
    }

    def nodes: List[N] = {
      node :: children.flatMap(_.nodes)
    }

    def edges: List[DiEdge[N]] = {
      children.map(c => DiEdge(node, c.node)) ++ children.flatMap(_.edges)
    }

    def addTreeByEdge(that: DRTree[N], edge: DiEdge[N]): DRTree[N] = {
      // TODO: beware of multiple appends to the tree. I.e. if more than one child adds the tree via the edge.
      if (node == edge.from) {
        DRTree(node, that :: children)
      } else {
        DRTree(node, children.map(_.addTreeByEdge(that, edge)))
      }
    }
  }

  object DRTree {
    def singleton[N](node: N): DRTree[N] = DRTree(node, Nil)
    def build[N](nodes: List[N], edges: List[DiEdge[N]]): DRTree[N] = {
      // assume for now nodes and edges form a proper rooted directed tree
      val disjointSet: DisjointSet[N] = DisjointSet(nodes: _*)

      val nodeToTree = mutable.Map.empty[N, DRTree[N]]

      edges.foreach({ e: DiEdge[N] =>
        val startRoot = disjointSet(e.from)
        val hiTree = nodeToTree.getOrElse(startRoot, singleton(startRoot))
        // there must be e.end == disjointSet(e.end) because of assumption we have a directed rooted tree
        val loTree = nodeToTree.getOrElse(e.to, singleton(e.to))

        nodeToTree(startRoot) = hiTree.addTreeByEdge(loTree, e)

        disjointSet.unionInOrder(e.from, e.to)
      })
      val bigRoot = disjointSet.roots().head // there is exactly one because we have a rooted directed tree
      nodeToTree(bigRoot)
    }
  }

  def buildForest[N](nodes: Seq[N], edges: Seq[DiEdge[N]]): List[DRTree[N]] = {
    // Create a list of trees from given edges.
    // Uses a disjoint set for help.
    require(nodes.length > edges.length) // this must hold for a tree, even more for a list of trees
    require(edges.map(e => e.from != e.to).forall(identity))  // no self-edges
    require(edges.flatMap(e => List(e.from, e.to)).toSet.subsetOf(nodes.toSet)) // list of nodes is complete
    val disjointSet: DisjointSet[N] = DisjointSet(nodes: _*)

    val nodesToTrees = edges.foldLeft(Map.empty[N, DRTree[N]])({ (m, e) =>
      val rootOfStart = disjointSet(e.from)
      val hiTree = m.getOrElse(rootOfStart, DRTree.singleton(rootOfStart))
      // there must be e.end == disjointSet(e.end) because of assumption we have a directed rooted tree
      val loTree = m.getOrElse(e.to, DRTree.singleton(e.to))

      disjointSet.unionInOrder(e.from, e.to)
      val extendedTree = hiTree.addTreeByEdge(loTree, e)

      m ++ Map(rootOfStart -> extendedTree)
    })

    // trees made of a single node don't have an entry in nodesToTrees yet -> use .getOrElse and singleton
    val trees = disjointSet.roots.map({ t => nodesToTrees.getOrElse(t, DRTree.singleton(t))}).toList
    assert(nodes.toSet == trees.flatMap(_.nodes).toSet)
    assert(nodes.length == trees.map(_.nodes.length).sum)
    assert(nodes.length == edges.length + trees.length)
    trees
  }


}
