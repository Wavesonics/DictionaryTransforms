fun <T : Comparable<T>> Array<T>.exchangeSort() {
    for (i in indices) {
        for (j in i + 1 until size) {
            if (this[i] > this[j]) {
                // Swap elements
                val temp = this[i]
                this[i] = this[j]
                this[j] = temp
            }
        }
    }
}
