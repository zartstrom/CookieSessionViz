package logic

import org.markgrafendamm.playscalajs.logic.Forest._
import org.scalatest._


class ForestSpec extends FlatSpec with Matchers {
  "A Forest" should "be built correctly" in {
    val edges = List(DiEdge(1, 2), DiEdge(3, 4), DiEdge(3, 5))

    val trees = buildForest(List(1, 2, 3, 4, 5, 6), edges)

    trees.length should be (3)
    trees.flatMap(_.nodes).toSet should be ((1 to 6).toSet)
  }
}
