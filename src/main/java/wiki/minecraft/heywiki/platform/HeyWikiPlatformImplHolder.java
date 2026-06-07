package wiki.minecraft.heywiki.platform;

public class HeyWikiPlatformImplHolder {
    static HeyWikiPlatform impl;

    // Dependency injection for platform-specific utilities
    public static void setImpl(HeyWikiPlatform impl) {
        HeyWikiPlatformImplHolder.impl = impl;
    }
}