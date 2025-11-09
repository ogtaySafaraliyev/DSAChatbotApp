package az.dsa.chatbot.util;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FuzzyMatcher {
    
    /**
     * Calculate Levenshtein distance between two strings
     * Used for typo tolerance
     */
    public int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        
        int[] costs = new int[s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        
        return costs[s2.length()];
    }
    
    /**
     * Calculate similarity ratio (0.0 to 1.0)
     */
    public double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        
        int distance = levenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        
        if (maxLen == 0) return 1.0;
        
        return 1.0 - ((double) distance / maxLen);
    }
    
    /**
     * Check if strings are similar within threshold
     * @param threshold 0.8 means 80% similarity required
     */
    public boolean isSimilar(String s1, String s2, double threshold) {
        return similarity(s1, s2) >= threshold;
    }
    
    /**
     * Find best match from a list of options
     * Returns null if no match above threshold
     */
    public String findBestMatch(String query, List<String> options, double threshold) {
        String bestMatch = null;
        double bestScore = threshold;
        
        for (String option : options) {
            double score = similarity(query, option);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = option;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Common Azerbaijani typo corrections
     */
    public String correctCommonTypos(String text) {
        if (text == null) return text;
        
        Map<String, String> typoMap = new HashMap<>();
        typoMap.put("piton", "python");
        typoMap.put("pyton", "python");
        typoMap.put("paython", "python");
        typoMap.put("maşın", "machine");
        typoMap.put("masin", "machine");
        typoMap.put("machina", "machine");
        typoMap.put("öyrənmə", "learning");
        typoMap.put("oyrənmə", "learning");
        typoMap.put("lerning", "learning");
        typoMap.put("təhlil", "analitika");
        typoMap.put("tehlil", "analitika");
        typoMap.put("analiz", "analitika");
        typoMap.put("təlim", "kurs");
        typoMap.put("telim", "kurs");
        
        String corrected = text.toLowerCase();
        for (Map.Entry<String, String> entry : typoMap.entrySet()) {
            corrected = corrected.replace(entry.getKey(), entry.getValue());
        }
        
        return corrected;
    }
}