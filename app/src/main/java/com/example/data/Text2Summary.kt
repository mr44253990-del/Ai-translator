package com.example.data

object Text2Summary {
    fun summarize(text: String, compressionFactor: Float = 0.5f): String {
        if (text.isBlank()) return ""
        
        // 1. Split text into sentences
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        if (sentences.size <= 2) return text
        
        // 2. Tokenize words and calculate term frequency
        val words = text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.length > 3 && !isStopWord(it) }
        
        val wordFrequencies = mutableMapOf<String, Int>()
        for (word in words) {
            wordFrequencies[word] = wordFrequencies.getOrDefault(word, 0) + 1
        }
        
        if (wordFrequencies.isEmpty()) return sentences.take((sentences.size * compressionFactor).toInt().coerceAtLeast(1)).joinToString(" ")
        
        val maxFrequency = wordFrequencies.values.maxOrNull() ?: 1
        val wordScores = wordFrequencies.mapValues { it.value.toFloat() / maxFrequency }
        
        // 3. Score sentences based on word scores
        val sentenceScores = mutableMapOf<Int, Float>()
        for (i in sentences.indices) {
            val sentenceWords = sentences[i].lowercase()
                .replace(Regex("[^a-zA-Z0-9\\s]"), "")
                .split(Regex("\\s+"))
            
            var score = 0f
            var wordCount = 0
            for (word in sentenceWords) {
                if (wordScores.containsKey(word)) {
                    score += wordScores[word] ?: 0f
                    wordCount++
                }
            }
            // Normalize score by sentence word count
            sentenceScores[i] = if (wordCount > 0) score / wordCount else 0f
        }
        
        // 4. Select top sentences
        val numSentencesToKeep = (sentences.size * compressionFactor).toInt().coerceAtLeast(1).coerceAtMost(sentences.size)
        val topSentenceIndices = sentenceScores.entries
            .sortedByDescending { it.value }
            .take(numSentencesToKeep)
            .map { it.key }
            .sorted() // Keep original chronological order of sentences
        
        return topSentenceIndices.joinToString(" ") { sentences[it] }
    }
    
    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf(
            "the", "is", "at", "which", "on", "a", "an", "and", "are", "as", "at", "be", "by", "for", 
            "from", "has", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", 
            "were", "will", "with", "this", "but", "they", "have", "had", "not", "but", "or", "as",
            "your", "my", "me", "you", "we", "us", "them", "their"
        )
        return stopWords.contains(word)
    }
}
