package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringIdentifiable;

public record TitleFormatter(Letter letter, Space space) {
    public static final TitleFormatter DEFAULT = new TitleFormatter(Letter.IGNORE, Space.IGNORE);
    public static Codec<TitleFormatter> CODEC = RecordCodecBuilder
            .create(builder ->
                            builder
                                    .group(
                                            Letter.CODEC.fieldOf("letter")
                                                        .orElse(Letter.IGNORE)
                                                        .forGetter(matcher -> matcher.letter),
                                            Space.CODEC.fieldOf("space")
                                                       .orElse(Space.IGNORE)
                                                       .forGetter(matcher -> matcher.space)
                                          )
                                    .apply(builder, TitleFormatter::new));

    public String formatTitle(String title) {
        String titleLetter = switch (this.letter()) {
            case Letter.LOWER -> title.toLowerCase();
            case Letter.UPPER -> title.toUpperCase();
            case Letter.IGNORE -> title;
        };

        return switch (this.space()) {
            case Space.UNDERSCORE -> titleLetter.replace(" ", "_");
            case Space.DASH -> titleLetter.replace(" ", "-");
            case Space.IGNORE -> titleLetter;
        };
    }

    public enum Letter implements StringIdentifiable {
        LOWER("lower"),
        UPPER("upper"),
        IGNORE("ignore");

        public static final Codec<Letter> CODEC = StringIdentifiable.createCodec(Letter::values);

        private final String name;

        Letter(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public enum Space implements StringIdentifiable {
        UNDERSCORE("underscore"),
        DASH("dash"),
        IGNORE("ignore");

        public static final Codec<Space> CODEC = StringIdentifiable.createCodec(Space::values);

        private final String name;

        Space(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
