package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.SearchFilters;
import az.dsa.chatbot.dto.SearchResult;
import az.dsa.chatbot.entity.Faq;
import az.dsa.chatbot.entity.Text;
import az.dsa.chatbot.entity.Training;
import az.dsa.chatbot.repository.FaqRepository;
import az.dsa.chatbot.repository.TextRepository;
import az.dsa.chatbot.repository.TrainingRepository;
import az.dsa.chatbot.service.SearchService;
import az.dsa.chatbot.util.FuzzyMatcher;
import az.dsa.chatbot.util.TrainingTextMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

	private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

	@Autowired
	private FaqRepository faqRepository;

	@Autowired
	private TextRepository textRepository;

	@Autowired
	private TrainingRepository trainingRepository;

	@Autowired
	private FuzzyMatcher fuzzyMatcher;

	@Autowired
	private TrainingTextMapper trainingTextMapper;

	// Category keywords mapping
	private static final Map<String, List<String>> CATEGORY_KEYWORDS = new HashMap<>();

	static {
		CATEGORY_KEYWORDS.put("Data Analytics",
				Arrays.asList("analytics", "analitika", "tableau", "power bi", "excel", "sql", "spss"));

		CATEGORY_KEYWORDS.put("Machine Learning",
				Arrays.asList("machine learning", "ml", "maşın öyrənməsi", "python machine", "r machine"));

		CATEGORY_KEYWORDS.put("Deep Learning",
				Arrays.asList("deep learning", "neural", "nlp", "computer vision", "transformers", "ai"));

		CATEGORY_KEYWORDS.put("Data Engineering",
				Arrays.asList("engineering", "sql", "database", "databaza", "pl/sql", "t-sql", "warehouse"));

		CATEGORY_KEYWORDS.put("AI Development",
				Arrays.asList("frontend", "backend", "django", "react", "development", "n8n"));
	}

	// Common stop words in Azerbaijani
	private static final Set<String> STOP_WORDS = Set.of("və", "ilə", "üçün", "bir", "bu", "o", "ki", "nə", "necə",
			"hansı", "haqqında", "üzrə", "kimi", "da", "də");

	@Override
	public List<SearchResult> searchFAQ(String query) {
		if (query == null || query.trim().isEmpty()) {
			return Collections.emptyList();
		}

		logger.debug("Searching FAQ for: {}", query);

		List<String> keywords = extractKeywords(query);
		List<SearchResult> results = new ArrayList<>();

		// Search for each keyword
		for (String keyword : keywords) {
			List<Faq> faqs = faqRepository.searchByKeyword(keyword);

			for (Faq faq : faqs) {
				// Calculate relevance score
				String searchableContent = faq.getQuestion() + " " + faq.getAnswer();
				double score = calculateRelevance(query, searchableContent);

				SearchResult result = new SearchResult();
				result.setSource("FAQ");
				result.setId(faq.getId());
				result.setTitle(faq.getQuestion());
				result.setContent(faq.getAnswer());
				result.setRelevanceScore(score);
				result.setRawData(faq);

				results.add(result);
			}
		}

		// Remove duplicates and sort by relevance
		List<SearchResult> uniqueResults = results.stream().collect(Collectors.toMap(r -> r.getId(), r -> r,
				(existing, replacement) -> existing.getRelevanceScore() > replacement.getRelevanceScore() ? existing
						: replacement))
				.values().stream().sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
				.collect(Collectors.toList());

		logger.debug("Found {} unique FAQ results", uniqueResults.size());
		return uniqueResults;
	}

	@Override
	public List<SearchResult> searchText(String query) {
		if (query == null || query.trim().isEmpty()) {
			return Collections.emptyList();
		}

		logger.debug("Searching Text for: {}", query);

		List<String> keywords = extractKeywords(query);
		List<SearchResult> results = new ArrayList<>();

		for (String keyword : keywords) {
			List<Text> texts = textRepository.searchByKeyword(keyword);

			for (Text text : texts) {
				// Calculate relevance score
				String searchableContent = text.getTitle() + " " + text.getDescription() + " " + text.getInformation();
				double score = calculateRelevance(query, searchableContent);

				SearchResult result = new SearchResult();
				result.setSource("TEXT");
				result.setId(text.getId());
				result.setTitle(text.getTitle());
				result.setContent(text.getDescription()); // Use description as preview
				result.setRelevanceScore(score);
				result.setRawData(text);

				results.add(result);
			}
		}

		// Remove duplicates and sort
		List<SearchResult> uniqueResults = results.stream().collect(Collectors.toMap(r -> r.getId(), r -> r,
				(existing, replacement) -> existing.getRelevanceScore() > replacement.getRelevanceScore() ? existing
						: replacement))
				.values().stream().sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
				.collect(Collectors.toList());

		logger.debug("Found {} unique Text results", uniqueResults.size());
		return uniqueResults;
	}

	@Override
	public List<SearchResult> searchAll(String query, int limit) {
		logger.debug("Searching all tables for: {}", query);

		List<SearchResult> allResults = new ArrayList<>();

		// Search in FAQ
		allResults.addAll(searchFAQ(query));

		// Search in Text
		allResults.addAll(searchText(query));

		// Search in Training
		allResults.addAll(searchTraining(query));

		// Sort by relevance and limit
		List<SearchResult> topResults = allResults.stream()
				.sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore())).limit(limit)
				.collect(Collectors.toList());

		logger.info("Found {} total results, returning top {}", allResults.size(), topResults.size());

		return topResults;
	}

	@Override
	public List<String> extractKeywords(String query) {
		if (query == null || query.trim().isEmpty()) {
			return Collections.emptyList();
		}

		// Normalize and split
		String normalized = query.toLowerCase().replaceAll("[.,!?;:]", " ").trim();

		String[] words = normalized.split("\\s+");

		// Filter stop words and short words
		List<String> keywords = Arrays.stream(words).filter(word -> word.length() > 2)
				.filter(word -> !STOP_WORDS.contains(word)).distinct().collect(Collectors.toList());

		logger.debug("Extracted keywords: {}", keywords);
		return keywords;
	}

	// updates on calculateRelevance method after phase 2.2
	@Override
	public double calculateRelevance(String query, String content) {
		if (query == null || content == null) {
			return 0.0;
		}

		String queryLower = query.toLowerCase();
		String contentLower = content.toLowerCase();

		// Extract keywords from query
		List<String> keywords = extractKeywords(query);
		if (keywords.isEmpty()) {
			return 0.0;
		}

		double score = 0.0;

		// 1. Exact phrase match (highest score)
		if (contentLower.contains(queryLower)) {
			score += 15.0;
		}

		// 2. Count keyword matches
		int matchCount = 0;
		int positionScore = 0;

		for (String keyword : keywords) {
			if (contentLower.contains(keyword)) {
				matchCount++;

				// Bonus for position (earlier = better)
				int position = contentLower.indexOf(keyword);
				if (position >= 0) {
					if (position == 0) {
						positionScore += 5; // Starts with keyword
					} else if (position < 50) {
						positionScore += 3; // Near beginning
					} else {
						positionScore += 1; // Somewhere in content
					}
				}
			} else {
				// Try fuzzy matching for this keyword
				String[] contentWords = contentLower.split("\\s+");
				for (String word : contentWords) {
					double similarity = fuzzyMatcher.similarity(keyword, word);
					if (similarity >= 0.7) { // 70% similar
						matchCount++;
						score += similarity * 2; // Partial credit
						break;
					}
				}
			}
		}

		score += positionScore;

		// 3. Keyword coverage ratio
		double coverage = (double) matchCount / keywords.size();
		score += coverage * 8.0;

		// 4. Bonus for multiple keyword matches
		if (matchCount > 1) {
			score += matchCount * 2.0;
		}

		// 5. Bonus for all keywords matched
		if (matchCount == keywords.size()) {
			score += 5.0;
		}

		return score;
	}

	// methods after phase 2.2 **************

	@Override
	public List<SearchResult> searchTraining(String query) {
		if (query == null || query.trim().isEmpty()) {
			return Collections.emptyList();
		}

		logger.debug("Searching Training for: {}", query);

		// Correct common typos first
		String corrected = fuzzyMatcher.correctCommonTypos(query);

		List<String> keywords = extractKeywords(corrected);
		List<SearchResult> results = new ArrayList<>();

		for (String keyword : keywords) {
			List<Training> trainings = trainingRepository.searchByKeyword(keyword);

			for (Training training : trainings) {
				// Only show active trainings
				if (training.getIsActive() == null || !training.getIsActive()) {
					continue;
				}

				// Calculate relevance
				String searchableContent = training.getTitle();
				double score = calculateRelevance(query, searchableContent);

				// Bonus for exact training name match
				if (searchableContent.toLowerCase().contains(keyword.toLowerCase())) {
					score += 5.0;
				}

				SearchResult result = new SearchResult();
				result.setSource("TRAINING");
				result.setId(training.getId());
				result.setTitle(training.getTitle());
				result.setContent("Təlim ID: " + training.getId());
				result.setRelevanceScore(score);
				result.setRawData(training);

				results.add(result);
			}
		}

		// Remove duplicates and sort
		List<SearchResult> uniqueResults = results.stream().collect(Collectors.toMap(r -> r.getId(), r -> r,
				(existing, replacement) -> existing.getRelevanceScore() > replacement.getRelevanceScore() ? existing
						: replacement))
				.values().stream().sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
				.collect(Collectors.toList());

		logger.debug("Found {} unique Training results", uniqueResults.size());
		return uniqueResults;
	}

	@Override
	public List<SearchResult> fuzzySearch(String query, int limit) {
		logger.debug("Fuzzy searching for: {}", query);

		// Step 1: Correct common typos
		String corrected = fuzzyMatcher.correctCommonTypos(query);

		// Step 2: Regular search with corrected query
		List<SearchResult> results = searchAll(corrected, limit * 2);

		// Step 3: If not enough results, try fuzzy matching
		if (results.size() < limit) {
			List<String> keywords = extractKeywords(query);

			for (String keyword : keywords) {
				// Try fuzzy matching against all training titles
				List<Training> allTrainings = trainingRepository.findAllActive();

				for (Training training : allTrainings) {
					// Check if already in results
					boolean alreadyExists = results.stream()
							.anyMatch(r -> r.getId().equals(training.getId()) && "TRAINING".equals(r.getSource()));

					if (alreadyExists)
						continue;

					// Check fuzzy similarity
					double similarity = fuzzyMatcher.similarity(keyword, training.getTitle());

					if (similarity >= 0.6) { // 60% similarity threshold
						SearchResult result = new SearchResult();
						result.setSource("TRAINING");
						result.setId(training.getId());
						result.setTitle(training.getTitle());
						result.setContent("Fuzzy match");
						result.setRelevanceScore(similarity * 10); // Convert to 0-10 scale
						result.setRawData(training);

						results.add(result);
					}
				}
			}
		}

		// Sort and limit
		List<SearchResult> topResults = results.stream()
				.sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore())).limit(limit)
				.collect(Collectors.toList());

		logger.debug("Fuzzy search returned {} results", topResults.size());
		return topResults;
	}

	@Override
	public List<SearchResult> searchByPriceRange(Integer minPrice, Integer maxPrice) {
		logger.debug("Searching by price range: {} - {}", minPrice, maxPrice);

		List<Text> texts = textRepository.findAll();
		List<SearchResult> results = new ArrayList<>();

		for (Text text : texts) {
			if (text.getMoney() == null)
				continue;

			boolean inRange = (minPrice == null || text.getMoney() >= minPrice)
					&& (maxPrice == null || text.getMoney() <= maxPrice);

			if (inRange) {
				SearchResult result = new SearchResult();
				result.setSource("TEXT");
				result.setId(text.getId());
				result.setTitle(text.getTitle());
				result.setContent(String.format("Qiymət: %d AZN", text.getMoney()));
				result.setRelevanceScore(5.0);
				result.setRawData(text);

				results.add(result);
			}
		}

		logger.debug("Found {} results in price range", results.size());
		return results;
	}

	// updates after phase 2.3

	@Override
	public List<SearchResult> searchByCategory(String category) {
		if (category == null || category.trim().isEmpty()) {
			return Collections.emptyList();
		}

		logger.debug("Searching by category: {}", category);

		List<String> keywords = CATEGORY_KEYWORDS.getOrDefault(category, Collections.singletonList(category));

		List<SearchResult> results = new ArrayList<>();

		for (String keyword : keywords) {
			results.addAll(searchAll(keyword, 20));
		}

		// Remove duplicates and sort
		List<SearchResult> uniqueResults = results.stream().collect(Collectors.toMap(
				r -> r.getSource() + "_" + r.getId(), r -> r,
				(existing, replacement) -> existing.getRelevanceScore() > replacement.getRelevanceScore() ? existing
						: replacement))
				.values().stream().sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
				.collect(Collectors.toList());

		logger.debug("Found {} results for category: {}", uniqueResults.size(), category);
		return uniqueResults;
	}

	@Override
	public List<SearchResult> getPopularTrainings(int limit) {
		logger.debug("Getting popular trainings (limit: {})", limit);

		List<Training> activeTrainings = trainingRepository.findAllActive();
		List<SearchResult> results = new ArrayList<>();

		// Popular training IDs (based on order field or manually defined)
		List<Long> popularIds = Arrays.asList(5L, 4L, 14L, 8L, 1L); // Excel, Python, ML, Tableau, T-SQL

		for (Long trainingId : popularIds) {
			Training training = activeTrainings.stream().filter(t -> t.getId().equals(trainingId)).findFirst()
					.orElse(null);

			if (training != null) {
				Text text = trainingTextMapper.getTextForTraining(training);

				SearchResult result = new SearchResult();
				result.setSource("TRAINING");
				result.setId(training.getId());
				result.setTitle(training.getTitle());
				result.setRelevanceScore(10.0 - results.size()); // Decreasing score
				result.setRawData(training);

				if (text != null) {
					result.setContent(text.getDescription());
				}

				results.add(result);

				if (results.size() >= limit)
					break;
			}
		}

		// Fill remaining with active trainings if needed
		if (results.size() < limit) {
			for (Training training : activeTrainings) {
				if (results.stream().anyMatch(r -> r.getId().equals(training.getId()))) {
					continue;
				}

				Text text = trainingTextMapper.getTextForTraining(training);

				SearchResult result = new SearchResult();
				result.setSource("TRAINING");
				result.setId(training.getId());
				result.setTitle(training.getTitle());
				result.setRelevanceScore(5.0);
				result.setRawData(training);

				if (text != null) {
					result.setContent(text.getDescription());
				}

				results.add(result);

				if (results.size() >= limit)
					break;
			}
		}

		logger.debug("Returning {} popular trainings", results.size());
		return results;
	}

	@Override
	public List<SearchResult> searchWithFilters(String query, SearchFilters filters) {
		if (query == null || query.trim().isEmpty()) {
			return Collections.emptyList();
		}

		logger.debug("Searching with filters - Query: {}, Filters: {}", query, filters != null ? "applied" : "none");

		List<SearchResult> results = new ArrayList<>();

		// Determine which sources to search
		boolean searchFaq = filters == null || filters.getSource() == null
				|| "FAQ".equalsIgnoreCase(filters.getSource());
		boolean searchText = filters == null || filters.getSource() == null
				|| "TEXT".equalsIgnoreCase(filters.getSource());
		boolean searchTraining = filters == null || filters.getSource() == null
				|| "TRAINING".equalsIgnoreCase(filters.getSource());

		// Search each source
		if (searchFaq) {
			results.addAll(searchFAQ(query));
		}

		if (searchText) {
			results.addAll(searchText(query));
		}

		if (searchTraining) {
			results.addAll(searchTraining(query));
		}

		// Apply filters
		if (filters != null) {
			results = applyFilters(results, filters);
		}

		// Sort by relevance
		results.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));

		logger.debug("Found {} results after filtering", results.size());
		return results;
	}

	private List<SearchResult> applyFilters(List<SearchResult> results, SearchFilters filters) {
		return results.stream().filter(result -> {
			// Price filter (only for TEXT source)
			if ("TEXT".equals(result.getSource()) && result.getRawData() instanceof Text) {
				Text text = (Text) result.getRawData();

				if (filters.getMinPrice() != null && text.getMoney() != null) {
					if (text.getMoney() < filters.getMinPrice()) {
						return false;
					}
				}

				if (filters.getMaxPrice() != null && text.getMoney() != null) {
					if (text.getMoney() > filters.getMaxPrice()) {
						return false;
					}
				}
			}

			// Active filter (only for TRAINING source)
			if ("TRAINING".equals(result.getSource()) && result.getRawData() instanceof Training) {
				Training training = (Training) result.getRawData();

				if (Boolean.TRUE.equals(filters.getActiveOnly())) {
					if (training.getIsActive() == null || !training.getIsActive()) {
						return false;
					}
				}
			}

			// Category filter
			if (filters.getCategory() != null && !filters.getCategory().isEmpty()) {
				String title = result.getTitle().toLowerCase();
				List<String> categoryKeywords = CATEGORY_KEYWORDS.getOrDefault(filters.getCategory(),
						Collections.singletonList(filters.getCategory().toLowerCase()));

				boolean matchesCategory = categoryKeywords.stream().anyMatch(title::contains);

				if (!matchesCategory) {
					return false;
				}
			}

			return true;
		}).collect(Collectors.toList());
	}

	/**
	 * Detect category from query
	 */
	public String detectCategory(String query) {
		if (query == null)
			return null;

		String lower = query.toLowerCase();

		for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
			for (String keyword : entry.getValue()) {
				if (lower.contains(keyword)) {
					return entry.getKey();
				}
			}
		}

		return null;
	}

	/**
	 * Detect query type from user message Returns: TRAINING, TRAINER, GRADUATE,
	 * BOOTCAMP, PRICE, SCHEDULE, GENERAL
	 */
	public String detectQueryType(String query) {
		if (query == null)
			return "GENERAL";

		String lower = query.toLowerCase();

		// Trainer query
		if (lower.contains("təlimçi") || lower.contains("müəllim") || lower.contains("trainer")
				|| lower.contains("kim tədris")) {
			return "TRAINER";
		}

		// Graduate query
		if (lower.contains("məzun") || lower.contains("graduate") || lower.contains("uğur")
				|| lower.contains("iş tapmış")) {
			return "GRADUATE";
		}

		// Bootcamp structure
		if (lower.contains("bootcamp") || lower.contains("struktur") || lower.contains("necə işləyir")
				|| lower.contains("proqram")) {
			return "BOOTCAMP";
		}

		// Price query
		if (lower.contains("qiymət") || lower.contains("nə qədər") || lower.contains("pul") || lower.contains("azn")
				|| lower.contains("manat")) {
			return "PRICE";
		}

		// Schedule query
		if (lower.contains("tarix") || lower.contains("vaxt") || lower.contains("nə vaxt") || lower.contains("başlayır")
				|| lower.contains("cədvəl") || lower.contains("saat")) {
			return "SCHEDULE";
		}

		// Training query (default)
		return "TRAINING";
	}

	/**
	 * Search trainings with detailed information
	 */
	public List<SearchResult> searchTrainingsDetailed(String query) {
		List<SearchResult> trainingResults = searchTraining(query);

		// Enrich with Text details
		for (SearchResult result : trainingResults) {
			if (result.getRawData() instanceof Training) {
				Training training = (Training) result.getRawData();
				Text text = trainingTextMapper.getTextForTraining(training);

				if (text != null) {
					// Store both
					Map<String, Object> enriched = new HashMap<>();
					enriched.put("training", training);
					enriched.put("text", text);
					result.setRawData(enriched);

					// Update content with more details
					StringBuilder content = new StringBuilder();
					if (text.getDescription() != null) {
						content.append(text.getDescription()).append("\n");
					}
					if (text.getMoney() != null) {
						content.append("Qiymət: ").append(text.getMoney()).append(" AZN");
					}
					result.setContent(content.toString());
				}
			}
		}

		return trainingResults;
	}

	/**
	 * Get bootcamp structure information
	 */
	public String getBootcampStructure() {
		return "🎯 **Bootcamp Strukturu:**\n\n" + "DSA Academy-də bootcamplar aşağıdakı kimi təşkil olunur:\n\n"
				+ "📚 **1. Data Analytics Bootcamp**\n" + "   • Excel ilə Data Analytics\n"
				+ "   • SQL və Data Management\n" + "   • Tableau Business Intelligence\n" + "   • Power BI\n\n"
				+ "🤖 **2. Machine Learning Bootcamp**\n" + "   • Python Programming\n"
				+ "   • Machine Learning Fundamentals\n" + "   • Deep Learning və AI\n\n"
				+ "💻 **3. Data Engineering Bootcamp**\n" + "   • SQL Advanced\n" + "   • Database Design\n"
				+ "   • Big Data Technologies\n\n" + "⏱️ **Müddət:** Hər bootcamp 3-6 ay\n"
				+ "💰 **Qiymət:** 250 AZN - 2000 AZN\n"
				+ "📜 **Sertifikat:** Hər bootcamp üçün beynəlxalq sertifikat\n\n"
				+ "📞 Ətraflı məlumat: 051 341 43 40";
	}

	/**
	 * Format price information
	 */
	public String formatPriceInfo(List<SearchResult> results) {
		if (results == null || results.isEmpty()) {
			return "💰 **Qiymət məlumatları:**\n\n" + "Təlimlərimizin qiymətləri 250 AZN - 2000 AZN aralığındadır.\n\n"
					+ "📞 Konkret təlim qiymətləri üçün: 051 341 43 40";
		}

		StringBuilder response = new StringBuilder();
		response.append("💰 **Qiymət məlumatları:**\n\n");

		for (SearchResult result : results) {
			Object rawData = result.getRawData();
			Integer price = null;

			if (rawData instanceof Text) {
				price = ((Text) rawData).getMoney();
			} else if (rawData instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) rawData;
				Text text = (Text) data.get("text");
				if (text != null) {
					price = text.getMoney();
				}
			}

			if (price != null) {
				response.append(String.format("• %s - %d AZN\n", result.getTitle(), price));
			}
		}

		response.append("\n📞 Ətraflı məlumat: 051 341 43 40");
		return response.toString();
	}

}