package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

/**
 * Represents a title formatter.
 *
 * <p>A title formatter is used to create alternative for page titles in URLs.
 * For example, "Hello World" can be formatted to "hello_world" or "hello-world".
 *
 * @param letter The letter formatting.
 * @param space  The space formatting.
 */
public record TitleFormat(Letter letter, Space space) {
    public static final TitleFormat DEFAULT = new TitleFormat(Letter.IGNORE, Space.IGNORE);
    public static final Codec<TitleFormat> CODEC = RecordCodecBuilder
            .create(builder ->
                            builder.group(
                                           Letter.CODEC.fieldOf("letter")
                                                       .orElse(Letter.IGNORE)
                                                       .forGetter(matcher -> matcher.letter),
                                           Space.CODEC.fieldOf("space")
                                                      .orElse(Space.IGNORE)
                                                      .forGetter(matcher -> matcher.space)
                                         )
                                   .apply(builder, TitleFormat::new));

    /**
     * Formats a title according to the letter and space formatting.
     *
     * @param title The title to format.
     * @return The formatted title.
     */
    public String formatTitle(String title) {
        String titleLetter = switch (this.letter()) {
            case LOWER -> title.toLowerCase();
            case UPPER -> title.toUpperCase();
            case IGNORE -> title;
        };

        return switch (this.space()) {
            case UNDERSCORE -> titleLetter.replace(" ", "_");
            case DASH -> titleLetter.replace(" ", "-");
            case IGNORE -> titleLetter;
        };
    }

    /**
     * Represents a letter formatting.
     *
     * @see Space
     */
    public enum Letter implements StringRepresentable {
        /**
         * Formats the letters to lower case.
         * {@snippet lang = "java":
         * var formatter = TitleFormat(Letter.LOWER, Space.IGNORE);
         * String formatted = formatter.formatTitle("Hello World");
         * // formatted = "hello world"
         *}
         */
        LOWER("lower"),

        /**
         * Formats the letters to upper case.
         * {@snippet lang = "java":
         * var formatter = TitleFormat(Letter.UPPER, Space.IGNORE);
         * String formatted = formatter.formatTitle("Hello World");
         * // formatted = "HELLO WORLD"
         *}
         */
        UPPER("upper"),

        /**
         * Ignores the letter formatting.
         * {@snippet lang = "java":
         * var formatter = TitleFormat(Letter.IGNORE, Space.IGNORE);
         * String formatted = formatter.formatTitle("Hello World");
         * // formatted = "Hello World"
         *}
         */
        IGNORE("ignore");

        public static final Codec<Letter> CODEC = StringRepresentable.fromValues(Letter::values);

        private final String name;

        Letter(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    /**
     * Represents a space formatting.
     *
     * @see Letter
     */
    public enum Space implements StringRepresentable {
        /**
         * Formats the spaces to underscores.
         * {@snippet lang = "java":
         * var formatter = TitleFormat(Letter.IGNORE, Space.UNDERSCORE);
         * String formatted = formatter.formatTitle("Hello World");
         *  // formatted = "Hello_World"
         *}
         */
        UNDERSCORE("underscore"),

        /**
         * Formats the spaces to dashes.
         * {@snippet lang = "java":
         * var formatter = TitleFormat(Letter.IGNORE, Space.DASH);
         * String formatted = formatter.formatTitle("Hello World");
         *  // formatted = "Hello-World"
         *}
         */
        DASH("dash"),

        /**
         * Ignores the space formatting.
         * {@snippet lang = "java":
         * var formatter = TitleFormat(Letter.IGNORE, Space.IGNORE);
         * String formatted = formatter.formatTitle("Hello World");
         *  // formatted = "Hello World"
         *}
         */
        IGNORE("ignore");

        public static final Codec<Space> CODEC = StringRepresentable.fromValues(Space::values);

        private final String name;

        Space(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
