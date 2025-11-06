package az.dsa.chatbot.model;

public enum Mode {
    CONTACT("contact"),
    CONSULT("consult"),
    QUERY("query"),
    NONE("none");
    
    private final String value;
    
    Mode(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static Mode fromString(String text) {
        if (text == null) return NONE;
        
        for (Mode mode : Mode.values()) {
            if (mode.value.equalsIgnoreCase(text)) {
                return mode;
            }
        }
        return NONE;
    }
}