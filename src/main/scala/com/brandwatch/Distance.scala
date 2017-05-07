package com.brandwatch

import java.math.BigInteger

import smile.math.distance.HammingDistance

object Distance {
  def distHex(x: String, y: String) = {
    def hexToBin(s: String) = new BigInteger(s, 16).toString(2)
      .reverse.padTo(64, "0").reverse.mkString

    distBin(hexToBin(x), hexToBin(y))
  }

  def distBin(x: String, y: String): Int = HammingDistance.d(x.getBytes, y.getBytes)
}
