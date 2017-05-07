package com.brandwatch

import java.io.{File, FileInputStream, FileNotFoundException}
import java.util.concurrent.atomic.AtomicInteger
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, FlowPane, HBox}
import javafx.stage.Stage

import scala.collection.JavaConverters._
import scala.collection.mutable


object ClusterDisplay {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[ClusterDisplay], args: _*)
  }
}

class ClusterDisplay extends Application {
  var border: BorderPane = _
  var imageDir: File = _
  var clusters: Array[mutable.Set[String]] = _
  val currentCluster: AtomicInteger = new AtomicInteger(0)

  @throws[Exception]
  override def start(primaryStage: Stage): Unit = {
    val args = getParameters.getRaw.asScala
    if (args.isEmpty) {
      println("Usage: java -jar phash-hierarchical-clustering.jar <imageDirectory>")
      System.exit(1)
    }

    imageDir = new File(args.head)
    clusters = HCluster.computeClusters(imageDir, "complete")

    border = new BorderPane
    border.setTop(addHBox)
    primaryStage.setScene(new Scene(border, 640, 480))
    primaryStage.show()
  }

  private def addFlowPane(dir: File, imageFiles: mutable.Set[String]) = {
    val flow = new FlowPane
    flow.setPadding(new Insets(15, 15, 15, 15))
    flow.setHgap(15)
    flow.setVgap(4)
    flow.setHgap(4)
    flow.setPrefWrapLength(200) // preferred width allows for two columns

    flow.setStyle("-fx-background-color: DAE6F3;")

    imageFiles.map(imageFile => try {
      val image = new Image(new FileInputStream(new File(dir, imageFile)), 200, 0, true, true)
      val imageView = new ImageView(image)
      imageView.setFitWidth(200)
      imageView
    } catch {
      case e: FileNotFoundException => throw new RuntimeException(e)
    }).foreach(img => flow.getChildren.addAll(img))

    flow
  }

  private def addHBox = {
    val hbox = new HBox
    hbox.setPadding(new Insets(15, 12, 15, 12))
    hbox.setSpacing(10)
    hbox.setStyle("-fx-background-color: #336699;")

    def addCluster(clusterNo: Int) = {
      println("Cluster #%d: %s".format(clusterNo, clusters(clusterNo)))
      val imageFiles = clusters(clusterNo)
      val flowPane = addFlowPane(imageDir, imageFiles)
      border.setCenter(flowPane)
    }

    val buttonPrev = new Button("Prev")
    buttonPrev.setPrefSize(100, 20)
    buttonPrev.setOnAction(event => {
      if (currentCluster.get() > 0) {
        addCluster(currentCluster.decrementAndGet())
      }
    })

    val buttonNext = new Button("Next")
    buttonNext.setPrefSize(100, 20)
    buttonNext.setOnAction(event => {
      addCluster(currentCluster.getAndIncrement())
    })

    hbox.getChildren.addAll(buttonPrev, buttonNext)
    hbox
  }
}

