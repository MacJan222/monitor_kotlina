package com.example.helloworld

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class FilteringLPF(
    private var filterLength: Int,
    private val samplingHertz: Int,
    private val cutoffAngular: Int,
    private val displayLength: Int,
    private val peakSearchSize: Int,

    private val peakThreshold: Int
) {
    private val currentAcquisitionIndex = displayLength
    var filteredData: MutableList<Float> = MutableList(this.displayLength) { 0.toFloat() }
    private var unfilteredChunk: MutableList<Int> = MutableList(this.filterLength) { 0 }
    var peakIndexesTop: MutableList<Int> = mutableListOf()
    var peakIndexesBot: MutableList<Int> = mutableListOf()
    var peakValuesTop: MutableList<Float> = mutableListOf()
    var peakValuesBot: MutableList<Float> = mutableListOf()
    private var filter: MutableList<Float> = mutableListOf()

    init {
        if (this.filterLength % 2 == 0) {
            filterLength -= 1
        }
        filter = makeFilter()
    }

    private fun makeHanningWindow(arrayIn: FloatArray): FloatArray {
        for (i in arrayIn.indices) {
            arrayIn[i] = (0.5 * (1.0 - cos(2.0 * Math.PI * i / (arrayIn.size - 1)))).toFloat()
        }
        return arrayIn
    }

    private fun makeResponseRect(arrayIn: IntArray): FloatArray {
        val arrayOut = FloatArray(arrayIn.size) { i -> (sin((this.cutoffAngular *
                arrayIn[i] / this.samplingHertz).toDouble()) / (arrayIn[i] * Math.PI)).toFloat() }
        val zeroK: Int = ((this.filterLength - 1) / 2)
        arrayOut[zeroK] = (this.cutoffAngular / (this.samplingHertz * Math.PI)).toFloat()

        return arrayOut
    }

    private fun makeFilter(): MutableList<Float> {
        val k = IntArray(filterLength) { (it * 1) - (filterLength / 2) }
        val hanningWindow = makeHanningWindow(FloatArray(filterLength))
        val impulseResponseRect = makeResponseRect(k)
        val filterArray = FloatArray(filterLength)
        for (i in hanningWindow.indices) { filterArray[i] = impulseResponseRect[i] * hanningWindow[i] }
        return filterArray.toMutableList()
    }

    fun processLPF(singleInput: Int): Float {
        this.unfilteredChunk.removeAt(0)
        this.unfilteredChunk += singleInput
        this.filteredData.removeAt(0)
        val lpfList = MutableList(this.filterLength) { 0.0F }
        for (i in lpfList.indices) { lpfList[i] = this.unfilteredChunk[i].toFloat() * this.filter[i] }
        val lpfResponse = lpfList.sum()
        this.filteredData += lpfResponse

        return lpfResponse
    }

    fun peakDetection() {

        fun isTopPeak(candidateIndex: Int, candidateValue: Float, lastPeakIndex: Int): Boolean {

            val searchedSliceList = this.filteredData.slice(
                this.filteredData.size - this.peakSearchSize until this.filteredData.size
            )

            // check if candidate has highest value in neighborhood
            if(candidateValue != searchedSliceList.max()) {
                return false
            }

            // check if peak absolute value is bigger than threshold
            val minFromRange = this.filteredData.slice(lastPeakIndex..candidateIndex).min()
            if(abs(candidateValue - minFromRange) < this.peakThreshold) {
                return false
            }

            // add index and value to peak lists
            this.peakIndexesTop += candidateIndex
            this.peakValuesTop += candidateValue

            return true
        }

        fun isBotPeak(candidateIndex: Int, candidateValue: Float, lastPeakIndex: Int): Boolean {

            val searchedSliceList = this.filteredData.slice(
                this.filteredData.size - this.peakSearchSize until this.filteredData.size)

            // check if candidate has smallest value in neighborhood
            if(candidateValue != searchedSliceList.min()) {
                return false
            }

            // check if peak absolute value is bigger than threshold
            val maxFromRange = this.filteredData.slice(lastPeakIndex..candidateIndex).max()
            if(abs(candidateValue - maxFromRange) < this.peakThreshold) {
                return false
            }

            // add index and value to peak lists
            this.peakIndexesBot += candidateIndex
            this.peakValuesBot += candidateValue

            return true
        }

        val peakCandidateIndex = this.currentAcquisitionIndex - this.peakSearchSize / 2

        val lastTopIdx = if(this.peakIndexesTop.size == 0) { 0 }
        else { this.peakIndexesTop.last() }

        val lastBotIdx = if(this.peakIndexesBot.size == 0) { 0 }
        else { this.peakIndexesBot.last() }


        isTopPeak(peakCandidateIndex, this.filteredData[peakCandidateIndex], lastTopIdx)
        isBotPeak(peakCandidateIndex, this.filteredData[peakCandidateIndex], lastBotIdx)

        // when all operations are done, shift all indexes left as they change with new data sample
        for (i in 0 until peakIndexesTop.size) { this.peakIndexesTop[i] -= 1 }
        for (i in 0 until peakIndexesBot.size) { this.peakIndexesBot[i] -= 1 }

        // remove peaks with negative indexes
        if(this.peakIndexesTop.size > 0) {
            if(this.peakIndexesTop[0] < 0) {
                this.peakIndexesTop.removeAt(0)
                this.peakValuesTop.removeAt(0)
            }
        }
        if(this.peakIndexesBot.size > 0) {
            if(this.peakIndexesBot[0] < 0) {
                this.peakIndexesBot.removeAt(0)
                this.peakValuesBot.removeAt(0)
            }
        }

        // remove double inhale and double exhale peaks
        if(this.peakIndexesTop.size > 1 && this.peakIndexesBot.size > 0) {
            if(this.peakIndexesTop[this.peakIndexesTop.size - 1] - this.peakIndexesTop[this.peakIndexesTop.size - 2] <=
                this.peakIndexesTop.last() - this.peakIndexesBot.last()) {
                this.peakIndexesTop.removeAt(this.peakIndexesTop.size - 1)
                this.peakValuesTop.removeAt(this.peakIndexesTop.size - 1)
            }
        }

        if(this.peakIndexesBot.size > 1 && this.peakIndexesTop.size > 0) {
            if(this.peakIndexesBot[this.peakIndexesBot.size - 1] - this.peakIndexesBot[this.peakIndexesBot.size - 2] <=
                this.peakIndexesBot.last() - this.peakIndexesTop.last()) {
                this.peakIndexesBot.removeAt(this.peakIndexesBot.size - 1)
                this.peakValuesBot.removeAt(this.peakIndexesBot.size - 1)
            }
        }
        return
    }

}


