package com.brandwatch

import java.io._

import net.liftweb.json._
import smile.clustering._
import smile.plot._
import smile.util._

import scala.collection.mutable.{HashMap, MultiMap, Set}

object HCluster {
  val cutHeight = 2.0

  def loadData(dir: File): Array[Image] = {
    println("Loading dataset from %s ...".format(dir.getAbsolutePath))

    val dataSetJson = new File(dir, "dataset.json")
    implicit val formats = DefaultFormats

    def deserialize(dataSetJson: File): Array[Image] = {
      val reader = new FileReader(dataSetJson)
      Serialization.read[Array[Image]](reader)
    }

    def serialize(images: Array[Image]) = {
      Serialization.write(images, new FileWriter(dataSetJson)).flush()
    }

    if (dataSetJson.exists) {
      deserialize(dataSetJson)
    } else {
      val images: Array[Image] = dir.listFiles
        .filter(f => f.isFile && f.getName.endsWith("jpg"))
        .flatMap(imageFromFile)

      serialize(images)
      images
    }
  }

  def train(dataSet: Array[Image], linkage: String): HierarchicalClustering = {
    println("#dataset=%d; building proximity matrix ...".format(dataSet.length))

    def d(x: Image, y: Image): Double = Distance.distBin(x.pHash, y.pHash)

    val proximityMatrix = proximity(dataSet, d(_, _), half = true)


    println("#dataset=%d; training with '%s' linkage ...".format(dataSet.length, linkage))
    hclust(proximityMatrix, linkage)
  }

  def getClusters(dataSet: Array[Image], clustering: HierarchicalClustering, cutHeight: Double): Array[Set[String]] = {
    val mm = new HashMap[Int, Set[String]] with MultiMap[Int, String]

    clustering.partition(cutHeight)
      .zipWithIndex
      .foreach {
        case (label, i) => mm.addBinding(label, dataSet(i).name)
      }

    mm.toSeq
      .sortBy(_._2.size)
      .reverse
      .map(t => t._2)
      .toArray
  }

  def printDistance(img1: File, img2: File): Unit = {
    println("Distance: %d".format(Distance.distBin(ImagePHash(img1), ImagePHash(img2))))
  }

  def computeClusters(imageDir: File, linkage: String): Array[Set[String]] = {
    val dataSet = loadData(imageDir)

    val hcComplete = train(dataSet, linkage)

    // use low value for the 'cutHeight' cause lager cluster will have low quality (many outliers)
    getClusters(dataSet, hcComplete, cutHeight)
  }

  private def imageFromFile(f: File): Option[Image] = {
    try {
      Some(Image(f.getName, ImagePHash(f)))
    } catch {
      case e: Throwable => {
        println(s"Error computing pHash: ${e.getMessage}")
        None
      }
    }
  }

  private def showDendrogram(clustering: HierarchicalClustering): Unit = {
    dendrogram(clustering)
  }

  private def writeClustering(clustering: HierarchicalClustering, imageDir: File, linkage: String) = {
    val file = new File(imageDir, "%s.bin".format(linkage))
    val oos = new ObjectOutputStream(new FileOutputStream(file))
    oos.writeObject(clustering)
    oos.close()
  }

  private def readClustering(imageDir: File, linkage: String): HierarchicalClustering = {
    val file = new File(imageDir, "%s.bin".format(linkage))
    val ois = new ObjectInputStream(new FileInputStream(file))
    val clustering = ois.readObject.asInstanceOf[HierarchicalClustering]
    ois.close()
    clustering
  }

  //  def main(args: Array[String]): Unit = {
  //    if (args.isEmpty) {
  //      println("Usage: scala HCluster <imageDirectory>")
  //      System.exit(1)
  //    }
  //
  //    // val agglomerationMethods = Array("single", "complete", "ward")
  //
  //    val imageDir = new File(args(0))
  //
  //    computeClusters(imageDir, "complete").foreach(println(_))
  //  }

  case class Image(name: String, pHash: String)

}
