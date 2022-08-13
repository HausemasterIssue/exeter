package me.friendly.api.properties;

public class EnumProperty<T extends Enum> extends Property<T> {
    public EnumProperty(T value, String... aliases) {
        super(value, aliases);
    }

    public String getFixedValue() {
        String n = value.name();
        return n.charAt(0) + n.toLowerCase()
                .replaceFirst(Character.toString(n.charAt(0)).toLowerCase(), "")
                .replaceAll("_", " ");
    }

    /**
     * Sets the value from the value param.
     *
     * @param value value input
     */
    @Override
    public void setValue(String value) {
        Enum[] array = this.getValue().getClass().getEnumConstants();
        int length = array.length;
        for (int i = 0; i < length; ++i) {
            if (!array[i].name().equalsIgnoreCase(value)) continue;
            this.value = (T)array[i];
        }
    }

    /**
     * Increments the enum
     */
    public void increment() {
        T[] consts = (T[]) value.getClass().getEnumConstants();
        int i = currentIndex() + 1;

        if (i > consts.length - 1) {
            i = 0;
        }

        value = (T) consts[i];
    }

    /**
     * Decrements the enum
     */
    public void decrement() {
        T[] consts = (T[]) value.getClass().getEnumConstants();
        int i = currentIndex() - 1;

        if (i < 0) {
            i = consts.length - 1;
        }

        value = (T) consts[i];
    }

    private int currentIndex() {
        return value.ordinal();
    }
}

