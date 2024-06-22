package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TitleFormatter(String letter, String space) {
    public static final TitleFormatter DEFAULT = new TitleFormatter("ignore", "ignore");
    public static Codec<TitleFormatter> CODEC = RecordCodecBuilder.create(builder ->
            builder
                    .group(
                            Codec.STRING.fieldOf("letter").forGetter(matcher -> matcher.letter), // lower, upper, ignore
                            Codec.STRING.fieldOf("space").forGetter(matcher -> matcher.space) // underscore, dash, ignore
                          )
                    .apply(builder, TitleFormatter::new));

    public String formatTitle(String title) {
        String titleLetter = switch (this.letter()) {
            case "lower" -> title.toLowerCase();
            case "upper" -> title.toUpperCase();
            case "ignore" -> title;
            default -> throw new IllegalStateException("Unexpected value: " + this.letter());
        };

        return switch (this.space()) {
            case "underscore" -> titleLetter.replace(" ", "_");
            case "dash" -> titleLetter.replace(" ", "-");
            case "ignore" -> titleLetter;
            default -> throw new IllegalStateException("Unexpected value: " + this.space());
        };
    }
}
