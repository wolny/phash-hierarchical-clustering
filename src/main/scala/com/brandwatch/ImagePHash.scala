package com.brandwatch

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.{File, FileInputStream, InputStream}
import javax.imageio.ImageIO

object ImagePHash {
  def apply(file: File): String = new ImagePHash().getHash(file)
}

class ImagePHash(size: Int = 32, smallerSize: Int = 8) {
  val c: Array[Double] = {
    val temp = Array.ofDim[Double](size)
    for (i <- 1 until size) {
      temp(i) = 1
    }
    temp(0) = 1 / Math.sqrt(2.0)
    temp
  }

  val cosValue: Array[Array[Array[Array[Double]]]] = {
    val temp = Array.ofDim[Double](size, size, size, size)
    for (u <- 0 until size) {
      for (v <- 0 until size) {
        for (i <- 0 until size) {
          for (j <- 0 until size) {
            temp(u)(v)(i)(j) =
              Math.cos(((2 * i + 1) / (2.0 * size)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * size)) * v * Math.PI)
          }
        }
      }
    }
    temp
  }

  def getHash(file: File): String = {
    getHash(new FileInputStream(file))
  }

  def getHash(is: InputStream): String = {
    val img: BufferedImage = ImageIO.read(is)
    val img_resizedGrayed = resizeAndGrayScale(img, size, size)
    var vals: Array[Array[Double]] = Array.ofDim[Double](size, size)

    for (x <- 0 until img_resizedGrayed.getWidth()) {
      for (y <- 0 until img_resizedGrayed.getHeight()) {
        vals(x)(y) = getBlue(img_resizedGrayed, x, y).toDouble
      }
    }

    val dctVals: Array[Array[Double]] = applyDCT(vals)
    var total: Double = 0

    for (x <- 0 until smallerSize) {
      for (y <- 0 until smallerSize) {
        total = total + dctVals(x)(y)
      }
    }

    total = total - dctVals(0)(0)

    val avg: Double = total / ((smallerSize * smallerSize) - 1)
    val hash: StringBuffer = new StringBuffer("")

    for (x <- 0 until smallerSize) {
      for (y <- 0 until smallerSize) {
        hash.append(
          if (dctVals(x)(y) > avg) {
            1
          } else {
            0
          }
        )
      }
    }

    hash.toString
  }

  private def resizeAndGrayScale(image: BufferedImage, width: Int, height: Int): BufferedImage = {
    val resizedImage: BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val g: Graphics2D = resizedImage.createGraphics()
    g.drawImage(image, 0, 0, width, height, null)
    g.dispose()
    resizedImage
  }

  private def getBlue(img: BufferedImage, x: Int, y: Int): Int = {
    return img.getRGB(x, y) & 0xff
  }

  private def applyDCT(f: Array[Array[Double]]): Array[Array[Double]] = {
    var F: Array[Array[Double]] = Array.ofDim[Double](size, size)
    for (u <- 0 until size) {
      for (v <- 0 until size) {
        var sum: Double = 0.0
        for (i <- 0 until size) {
          for (j <- 0 until size) {
            sum += cosValue(u)(v)(i)(j) * f(i)(j)
          }
        }
        sum *= ((c(u) * c(v)) / 4.0)
        F(u)(v) = sum
      }
    }
    F
  }
}
