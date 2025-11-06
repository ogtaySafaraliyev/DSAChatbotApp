package az.dsa.chatbot.model;

public enum Intent {
    CONTACT("contact"),      // User wants to contact
    CONSULT("consult"),      // User wants consultation
    QUERY("query"),          // User has specific question
    TRAINER("trainer"),      // User asks about trainers
    GREETING("greeting"),    // User says hello
    UNCLEAR("unclear");      // Cannot determine intent
    
    private final String value;
    
    Intent(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static Intent fromString(String text) {
        for (Intent intent : Intent.values()) {
            if (intent.value.equalsIgnoreCase(text)) {
                return intent;
            }
        }
        return UNCLEAR;
    }
}